package com.sila.modules.category.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.AccessDeniedException;
import com.sila.config.exception.BadRequestException;
import com.sila.modules.category.dto.CategoryRequest;
import com.sila.modules.category.dto.CategoryResponse;
import com.sila.modules.category.model.Category;
import com.sila.modules.category.repository.CategoryRepository;
import com.sila.modules.chat.dto.MessageResponse;
import com.sila.modules.food.repository.FoodRepository;
import com.sila.modules.profile.model.User;
import com.sila.modules.resturant.repository.RestaurantRepository;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.modules.upload.KeyImageProperty;
import com.sila.modules.upload.services.CloudinaryService;
import com.sila.share.Utils;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.pagination.EntityResponseHandler;
import com.sila.share.pagination.PageableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryImp implements CategoryService {
    final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final CloudinaryService cloudinaryService;
    private final RestaurantService restaurantService;

    public static CategoryResponse mapToCategoryResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .items(!CollectionUtils.isEmpty(category.getFoodList()) ? category.getFoodList().size() : 0)
                .restaurant(category.getRestaurant().getName())
                .url(category.getUrl())
                .publicId(category.getPublicId())
                .name(category.getName())
                .build();
    }

    @Override
    public Category create(CategoryRequest request) {
        User user = UserContext.getUser();
        var restaurant = restaurantService.findRestaurantByOwner(user);
        if (categoryRepository.existsByNameAndRestaurant(request.getName(), restaurant)) {
            throw new BadRequestException("Category name already exists for this restaurant");
        }
        var image = Boolean.TRUE.equals(request.getRemoveBg()) ? cloudinaryService.uploadFileRemoveBG(request.getImage()) : cloudinaryService.uploadFile(request.getImage());

        Category category = Category.builder()
                .name(request.getName())
                .restaurant(restaurant)
                .url(image.get("secureUrl"))
                .publicId(image.get("publicId"))
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public Category update(CategoryRequest request, Long categoryId) {
        if (UserContext.getUser().getRole().equals(ROLE.USER)) {
            throw new AccessDeniedException("Customer do not have permission to change category");
        }
        Category category = getById(categoryId);
        if (request.getImage() != null) {
            var image = cloudinaryService.uploadFile(request.getImage());
            Utils.setValueSafe(image.get(KeyImageProperty.url.toString()), category::setUrl);
            Utils.setValueSafe(image.get(KeyImageProperty.publicId.toString()), category::setPublicId);
        }


        Utils.setValueSafe(request.getName(), category::setName);

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getsByResId(Long restaurantId) {
        return categoryRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public MessageResponse delete(Long categoryId) {
        var category = getById(categoryId);
        var foods = foodRepository.findAllByCategoryId(category.getId());
        if (!foods.isEmpty()) {
            throw new BadRequestException("Please remove food from category before delete category");
//            foodService.deleteByCategoryId(categoryId);
        }
        categoryRepository.deleteById(categoryId);
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Category Id : " + categoryId + " successfully!");
        return messageResponse;
    }

    @Override
    public List<CategoryResponse> all() {
        var categories = categoryRepository.findAll();
        return categories.stream().map(c -> modelMapper.map(c, CategoryResponse.class)).toList();
    }

    @Override
    public EntityResponseHandler<CategoryResponse> gets(PaginationRequest request) {
        var user = UserContext.getUser();

        Long restaurantId = null;
        if (user.getRole().equals(ROLE.OWNER)) {
            var restaurantExit = restaurantService.findRestaurantByOwner(user);
            restaurantId = user.getRole() == ROLE.OWNER ? restaurantExit.getId() : null;
        }
        Pageable pageable = PageableUtil.fromRequest(request);
        Page<Category> categoryPage = categoryRepository.findByFilters(
                request.getSearch(),
                restaurantId,
                pageable
        );

        return new EntityResponseHandler<>(categoryPage.map(CategoryImp::mapToCategoryResponse));
    }

    @Override
    public String deleteAllCategories() {
        var images = categoryRepository.findAll().stream().map(Category::getPublicId).toList();
        cloudinaryService.deleteImages(images);
        log.info("Successfully deleted all images belong categories from cloudinary. Images : " + images + " deleted successfully.");
        categoryRepository.deleteAll();
        return "Deleted all categories successfully";
    }

    @Override
    public Long count() {
        return categoryRepository.count();
    }

    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new BadRequestException("category not found"));
    }

    private Long parseRestaurantId(String filterBy) {
        try {
            return filterBy != null ? Long.parseLong(filterBy) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
