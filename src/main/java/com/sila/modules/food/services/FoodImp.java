package com.sila.modules.food.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.BadRequestException;
import com.sila.modules.category.model.Category;
import com.sila.modules.category.repository.CategoryRepository;
import com.sila.modules.food.FoodSpecification;
import com.sila.modules.food.FoodType;
import com.sila.modules.food.dto.FoodRequest;
import com.sila.modules.food.dto.FoodResponse;
import com.sila.modules.food.model.Food;
import com.sila.modules.food.model.ImageFood;
import com.sila.modules.food.repository.FoodRepository;
import com.sila.modules.order.repository.OrderItemRepository;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.modules.resturant.repository.RestaurantRepository;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.modules.upload.services.CloudinaryService;
import com.sila.share.Utils;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.pagination.EntityResponseHandler;
import com.sila.share.pagination.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.sila.modules.food.FoodSpecification.*;

@Service
@RequiredArgsConstructor
public class FoodImp implements FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantService restaurantService;

    @Override
    public Food create(FoodRequest foodRequest, Category category, Restaurant restaurant, List<MultipartFile> imageFiles) {
        double price = foodRequest.getPrice();
        double foodDiscount = foodRequest.getDiscount(); // ensure this is passed in request
        double restaurantDiscount = restaurant.getRestaurantDiscount();
        double totalDiscount = Math.min(foodDiscount + restaurantDiscount, 100.0);

        double discountedPrice = price - (price * totalDiscount / 100.0);
        double finalPrice = Math.round(discountedPrice * 100.0) / 100.0;

        Food food = Food.builder()
                .name(foodRequest.getName())
                .description(foodRequest.getDescription())
                .price(price)
                .discount(foodDiscount)
                .priceWithDiscount(finalPrice) // ✅ important
                .available(foodRequest.isAvailable())
                .foodtype(FoodType.valueOf(foodRequest.getFoodType()))
                .creationDate(new Date())
                .category(category)
                .restaurant(restaurant)
                .build();

        food.setPriceWithDiscount(food.getPriceWithDiscountCalculated());

        List<ImageFood> imageEntities = cloudinaryService.uploadImagesToCloudinary(
                imageFiles,
                food,
                (url, publicId) -> {
                    ImageFood image = new ImageFood();
                    image.setUrl(url);
                    image.setPublicId(publicId);
                    return image;
                },
                ImageFood::setFood
        );

        food.setImages(imageEntities);
        Food savedFood = foodRepository.save(food);
        restaurant.getFoods().add(savedFood);

        return savedFood;
    }

    @Override
    public Food update(FoodRequest foodReq, Long foodId) {
        var user = UserContext.getUser();
        Restaurant restaurant = restaurantService.findRestaurantByOwner(user);
        Food foodToUpdate = getById(foodId);
        if (!foodToUpdate.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BadRequestException("Food isn't belong to restaurant");
        }
        Category category = categoryRepository.findById(foodReq.getCategoryId()).orElseThrow(() -> new BadRequestException("category not found"));

        Food food = getById(foodId);
        Utils.setValueSafe(foodReq.getName(), food::setName);
        Utils.setValueSafe(foodReq.getDiscount(), food::setDiscount);
        Utils.setValueSafe(category, food::setCategory);
        Utils.setValueSafe(foodReq.getPrice(), food::setPrice);
        Utils.setValueSafe(foodReq.getDescription(), food::setDescription);
        Utils.setValueSafe(FoodType.valueOf(foodReq.getFoodType()), food::setFoodtype);
        Utils.setValueSafe(foodReq.isAvailable(), food::setAvailable);

        // Recalculate and set
        food.setPriceWithDiscount(food.getPriceWithDiscountCalculated());


        cloudinaryService.updateEntityImages(
                food,
                foodReq.getImages(),
                Food::getImages,
                Food::setImages,
                (url, publicId) -> {
                    ImageFood image = new ImageFood();
                    image.setUrl(url);
                    image.setPublicId(publicId);
                    return image;
                },
                ImageFood::setFood,
                ImageFood::getPublicId
        );

        return foodRepository.save(food);
    }


    @Override
    public void delete(Long id) {
        Food food = getById(id);
        var orderItems = orderItemRepository.findAllByFood(food);
        if (!orderItems.isEmpty()) {
            throw new BadRequestException("Food has been order! finished order before delete");
        }
        food.setRestaurant(null);
        foodRepository.deleteById(id);
    }

    @Override
    @Transactional
    public String deleteByCategoryId(Long categoryId) {
        var foods = foodRepository.findAllByCategoryId(categoryId);
        for (var food : foods) {
            orderItemRepository.deleteAllByFood(food);
        }
        foodRepository.deleteAllByCategoryId(categoryId);
        return "All food have categoryId : " + categoryId + " was deleted";
    }

    @Override
    public Food getById(Long foodId) {
        return foodRepository.findById(foodId).orElseThrow(() -> new BadRequestException("Not found food with this id"));
    }

    @Override
    public Food updateStatus(Long id) {
        Food updateFood = getById(id);
        updateFood.setAvailable(!updateFood.isAvailable());
        return foodRepository.save(updateFood);
    }

    @Override
    public EntityResponseHandler<FoodResponse> gets(PaginationRequest request) {
        var user = UserContext.getUser();
        Pageable pageable = PageableUtil.fromRequest(request);
        Specification<Food> spec = Specification.where(null);

        if (Objects.nonNull(request.getFilterBy())) {
            spec = spec.and(filterCategory(request.getFilterBy()));
        }
        if (Objects.nonNull(request.getPrice())) {
            spec = spec.and(filterByPrice(request.getPrice()));
        }
        if (Objects.nonNull(request.getFoodType())) {
            spec = spec.and(filterByFoodType(request.getFoodType()));
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            spec = spec.and(filterByPriceRange(request.getMinPrice(), request.getMaxPrice()));
        }

        if (user.getRole() == ROLE.OWNER) {
            var restaurant = restaurantService.findRestaurantByOwner(user);
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("restaurant"), restaurant)
            );
        }
        if (Objects.nonNull(request.getSearch())) {
            spec = spec.and(addSearchSpecification(spec, request.getSearch()));

        }
        Page<FoodResponse> page = foodRepository
                .findAll(spec, pageable)
                .map(FoodResponse::toResponse);

        return new EntityResponseHandler<>(page);
    }

    @Override
    public Long count() {
        return foodRepository.count();
    }

    @Override
    public Long count(Long restaurantId) {
        var restaurant = restaurantService.getById(restaurantId);
        return foodRepository.countAllByRestaurant(restaurant);
    }

    @Override
    public EntityResponseHandler<FoodResponse> getsByResId(Long restaurantId, PaginationRequest request) {
        Pageable pageable = PageableUtil.fromRequest(request);
        var foodPage = foodRepository.findAll(FoodSpecification.filterFoodByRestaurantId(restaurantId, request.getFilterBy()), pageable);
        return new EntityResponseHandler<>(foodPage.map(FoodResponse::toResponse));
    }

}
