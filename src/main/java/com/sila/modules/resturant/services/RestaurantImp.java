package com.sila.modules.resturant.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.AccessDeniedException;
import com.sila.config.exception.BadRequestException;
import com.sila.config.exception.NotFoundException;
import com.sila.modules.address.dto.AddressResponse;
import com.sila.modules.address.model.Address;
import com.sila.modules.address.repository.AddressRepository;
import com.sila.modules.category.repository.CategoryRepository;
import com.sila.modules.chat.dto.MessageResponse;
import com.sila.modules.favorite.dto.FavoriteResponse;
import com.sila.modules.favorite.model.Favorite;
import com.sila.modules.favorite.repository.FavoriteRepository;
import com.sila.modules.order.repository.OrderRepository;
import com.sila.modules.profile.dto.res.UserResponse;
import com.sila.modules.profile.model.User;
import com.sila.modules.resturant.RestaurantSpecification;
import com.sila.modules.resturant.dto.ContactInformationResponse;
import com.sila.modules.resturant.dto.RestaurantRequest;
import com.sila.modules.resturant.dto.RestaurantResponse;
import com.sila.modules.resturant.model.ContactInformation;
import com.sila.modules.resturant.model.ImageRestaurant;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.modules.resturant.repository.RestaurantRepository;
import com.sila.modules.upload.services.CloudinaryService;
import com.sila.share.Utils;
import com.sila.share.dto.req.SearchRequest;
import com.sila.share.dto.res.ImageDetailsResponse;
import com.sila.share.enums.ROLE;
import com.sila.share.pagination.EntityResponseHandler;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class RestaurantImp implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final FavoriteRepository favoriteRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager; // Add EntityManager

    public static RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        List<ImageDetailsResponse> imageDetails = restaurant.getImages().stream()
                .map(image -> new ImageDetailsResponse(image.getUrl(), image.getPublicId()))
                .toList();

        AddressResponse response = null;
        if (restaurant.getAddress() != null) {
            response = AddressResponse.builder()
                    .name(restaurant.getAddress().getName())
                    .id(restaurant.getAddress().getId())
                    .street(restaurant.getAddress().getStreet())
                    .city(restaurant.getAddress().getCity())
                    .country(restaurant.getAddress().getCountry())
                    .zip(restaurant.getAddress().getZip())
                    .state(restaurant.getAddress().getState())
                    .currentUsage(restaurant.getAddress().getCurrentUsage())
                    .build();
        }

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisineType(restaurant.getCuisineType())
                .open(restaurant.isOpen())
                .openingHours(restaurant.getOpeningHours())
                .registrationDate(restaurant.getRegistrationDate())
                .address(response)
                .contactInformation(ContactInformationResponse.builder()
                        .email(restaurant.getContactInformation().getEmail())
                        .phone(restaurant.getContactInformation().getPhone())
                        .build())
                .imageUrls(imageDetails)
                .ownerName(restaurant.getOwner().getFullName())
                .ownerId(restaurant.getOwner().getId())
                .deliveryFee(restaurant.getDeliveryFee())
                .discount(restaurant.getRestaurantDiscount())
                .build();
    }

    @Override
    @Transactional
    public RestaurantResponse create(RestaurantRequest restaurantReq) {
        var user = UserContext.getUser();
        if (restaurantRepository.existsByOwnerId(user.getId())) {
            throw new BadRequestException("User already owns a restaurant!");
        }

        Address address = addressRepository.save(Address.builder()
                .name(restaurantReq.getAddress().getName()) // ✅ Add this line
                .street(restaurantReq.getAddress().getStreet())
                .city(restaurantReq.getAddress().getCity())
                .country(restaurantReq.getAddress().getCountry())
                .state(restaurantReq.getAddress().getState())
                .zip(restaurantReq.getAddress().getZip())
                .user(user).currentUsage(restaurantReq.getAddress().getCurrentUsage())
                .build());

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantReq.getName())
                .openingHours(restaurantReq.getOpeningHours())
                .description(restaurantReq.getDescription())
                .registrationDate(LocalDateTime.now())
                .owner(user)
                .cuisineType(restaurantReq.getCuisineType())
                .address(address)
                .contactInformation(restaurantReq.getContactInformation())
                .open(restaurantReq.getOpen()) // don't forget this!
                .build();

        var imageEntities = cloudinaryService.uploadImagesToCloudinary(
                restaurantReq.getImages(),
                restaurant,
                (url, publicId) -> {
                    ImageRestaurant image = new ImageRestaurant();
                    image.setUrl(url);
                    image.setPublicId(publicId);
                    return image;
                },
                ImageRestaurant::setRestaurant
        );
        restaurant.setImages(imageEntities);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(savedRestaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse update(RestaurantRequest updateRestaurant, Long restaurantId) throws Exception {
        var userId = UserContext.getUser().getId();
        Restaurant restaurant = handleFindRestaurantById(restaurantId);
        if (!isUserOwnerOfRestaurant(userId, restaurantId)) {
            throw new AccessDeniedException("You are not the owner of this restaurant.");
        }

        cloudinaryService.updateEntityImages(
                restaurant,
                updateRestaurant.getImages(),
                Restaurant::getImages,
                Restaurant::setImages,
                (url, publicId) -> {
                    ImageRestaurant image = new ImageRestaurant();
                    image.setUrl(url);
                    image.setPublicId(publicId);
                    return image;
                },
                ImageRestaurant::setRestaurant,
                ImageRestaurant::getPublicId
        );

        Utils.setValueSafe(updateRestaurant.getOwnerName(), s -> restaurant.getOwner().setFullName(s));

        Utils.setValueSafe(updateRestaurant.getName(), restaurant::setName);
        Utils.setValueSafe(updateRestaurant.getDescription(), restaurant::setDescription);
        Utils.setValueSafe(updateRestaurant.getCuisineType(), restaurant::setCuisineType);
        Utils.setValueSafe(updateRestaurant.getOpeningHours(), restaurant::setOpeningHours);
        Utils.setValueSafe(updateRestaurant.getContactInformation(), restaurant::setContactInformation);
        Utils.setValueSafe(updateRestaurant.getOpen(), restaurant::setOpen);

        Utils.setValueSafe(updateRestaurant.getDiscount(), restaurant::setRestaurantDiscount);
        Utils.setValueSafe(updateRestaurant.getDeliveryFee(), restaurant::setDeliveryFee);

        // Handle Address
        if (updateRestaurant.getAddress() != null) {
            Address address = updateRestaurant.getAddress();
            // Merge the Address to make it managed
            Address managedAddress = entityManager.merge(address);
            restaurant.setAddress(managedAddress);
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(savedRestaurant);
    }

    @Override
    @Transactional
    public MessageResponse delete(Long id) {
        var restaurant = getById(id);
        var images = restaurant.getImages().stream().map(ImageRestaurant::getPublicId).collect(Collectors.toList());
        // First delete categories related to the restaurant
        cloudinaryService.deleteImages(images);

        categoryRepository.deleteByRestaurantId(id); // custom method you'll need

        favoriteRepository.deleteByRestaurantId(id);
        // Now delete the restaurant
        restaurantRepository.deleteById(id);

        return MessageResponse.builder().message("Deleted restaurant id: " + id + " successfully!").status(HttpStatus.OK.value()).build();
    }

    @Override
    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new BadRequestException("Restaurant not found with id " + id));
    }

    @Override
    public RestaurantResponse getByUserLogin() {
        var user = UserContext.getUser();
        Restaurant restaurant = findRestaurantByOwner(user);
        if (Objects.isNull(restaurant)) {
            if (user.getRole() == ROLE.OWNER) {
                throw new BadRequestException("user don't have restaurant");
            } else
                throw new BadRequestException("this role can't have restaurant");
        }
        return mapToRestaurantResponse(restaurant);
    }

    @Override
    public EntityResponseHandler<RestaurantResponse> search(Pageable pageable, SearchRequest searchReq) {
        var restaurantPage = restaurantRepository.findAll(RestaurantSpecification.filterRestaurant(searchReq), pageable);
        return new EntityResponseHandler<>(restaurantPage.map(RestaurantImp::mapToRestaurantResponse));
    }

    @Override
    public List<UserResponse> getUsersWhoOrderedFromRestaurant(Long restaurantId) {
        var users = orderRepository.findUsersByRestaurantId(restaurantId);
        return users.stream().map(u -> this.modelMapper.map(u, UserResponse.class)).toList();
    }

    @Override
    public Long count() {
        return restaurantRepository.count();
    }

    @Override
    public Restaurant findRestaurantByOwner(User user) {
        return restaurantRepository.findByOwnerId(user.getId()).orElseThrow(() ->
                new NotFoundException("Restaurant not found with this owner"));
    }

    @Override
    public void autoCreateRestaurantAsDefault(User user) {
        var IsCreatedRestaurant = restaurantRepository.existsByOwnerId(user.getId());
        if (!IsCreatedRestaurant) {
            Address address = addressRepository.save(Address.builder()
                    .name("WORK") // ✅ Add this line
                    .street("STREET")
                    .city("PHNOM PENH")
                    .country("CAMBODIA")
                    .state("CENTER")
                    .zip("0000")
                    .user(user)
                    .currentUsage(Boolean.TRUE)
                    .build());

            ContactInformation contactInformation = ContactInformation.builder()
                    .email(user.getEmail())
                    .phone("0123456789")
                    .build();

            Restaurant restaurant = Restaurant.builder()
                    .name("DEFAULT")
                    .openingHours("...")
                    .description("DESCRIPTION")
                    .registrationDate(LocalDateTime.now())
                    .owner(user)
                    .cuisineType("NONE")
                    .address(address)
                    .contactInformation(contactInformation)
                    .open(false) // don't forget this!
                    .build();

            restaurantRepository.save(restaurant);

        }
    }

    @Override
    public List<FavoriteResponse> addFav(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new RuntimeException("Restaurant not found"));
        Favorite fav = Favorite.builder().name(restaurant.getName()).description(restaurant.getDescription()).restaurant(restaurant).owner(UserContext.getUser()).build();
        if (favoriteRepository.existsFavoriteByRestaurantId(restaurantId)) {
            favoriteRepository.deleteByRestaurantId(restaurantId);
        } else {
            restaurant.setFavorites(List.of(fav));
            favoriteRepository.save(fav);
        }

        return restaurant.getFavorites().stream().map(favorite -> modelMapper.map(favorite, FavoriteResponse.class)).toList();
    }

    public boolean isUserOwnerOfRestaurant(Long userId, Long restaurantId) throws Exception {
        Restaurant restaurant = getById(restaurantId);
        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this restaurant.");
        }
        return true;
    }

    public Restaurant handleFindRestaurantById(Long restaurantId) {
        Optional<Restaurant> restaurantExit = restaurantRepository.findById(restaurantId);
        if (restaurantExit.isEmpty()) {
            throw new NotFoundException("Not Found Restaurant with id : " + restaurantId);
        }
        return restaurantExit.get();
    }
}
