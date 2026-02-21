package com.sila.modules.order.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.BadRequestException;
import com.sila.modules.cart.model.CartItem;
import com.sila.modules.cart.repository.CartItemRepository;
import com.sila.modules.cart.repository.CartRepository;
import com.sila.modules.order.dto.OrderItemResponse;
import com.sila.modules.order.dto.OrderResponse;
import com.sila.modules.order.model.Order;
import com.sila.modules.order.model.OrderItem;
import com.sila.modules.order.repository.OrderItemRepository;
import com.sila.modules.order.repository.OrderRepository;
import com.sila.modules.payment.PAYMENT_STATUS;
import com.sila.modules.profile.dto.res.UserResponse;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.modules.resturant.services.RestaurantImp;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.pagination.EntityResponseHandler;
import com.sila.share.pagination.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderImp implements OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final CartItemRepository cartItemRepository;
    private final RestaurantService restaurantService;

    @Override
    public EntityResponseHandler<OrderResponse> getAll(PaginationRequest request) {

        var user = UserContext.getUser();

        Pageable pageable = PageableUtil.fromRequest(request);

        Page<Order> orders;
        if (user.getRole() == ROLE.USER) {
            orders = orderRepository.findAllByUser(user, pageable);
        } else if (user.getRole() == ROLE.OWNER) {
            var restaurant = restaurantService.findRestaurantByOwner(user);
            orders = orderRepository.findAllByRestaurant(restaurant, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return new EntityResponseHandler<>(orders
                .map(this::convertToOrderResponse));
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(Long cartId) {
        // Get the authenticated user
        final var user = UserContext.getUser();
        var cart = cartRepository.findByIdAndUserAndItemsNotEmpty(cartId, user).orElseThrow(() ->
                new BadRequestException("cart is not belong to user or cart not found")
        );

        // Create a new order
        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(PAYMENT_STATUS.PENDING);

        // Assume all items in the cart are from the same restaurant
        Restaurant restaurant = cart.getItems().get(0).getFood().getRestaurant();
        order.setRestaurant(restaurant);

        // Convert CartItems to OrderItems and calculate total amount
        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // Ensure the order is set
            orderItem.setFood(cartItem.getFood());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            // Calculate total amount (assuming Food has a price field)
            totalAmount += cartItem.getFood().getPrice() * cartItem.getQuantity();
        }

        // Set the order items and total amount
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Save the order first to generate its ID
        orderRepository.save(order);

        // Save each OrderItem individually to ensure IDs are generated
        orderItemRepository.saveAll(orderItems);

        // Clear the cart
        var itemIds = cart.getItems().stream()
                .map(CartItem::getId)
                .toList();
        cartItemRepository.deleteAllByIdInBatch(itemIds);
        cartRepository.deleteAllByIdInBatch(List.of(cartId));

        // Convert to OrderResponse
        return convertToOrderResponse(order);
    }

    @Override
    public String deletePlaceOrder(Long orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(
                () -> new BadRequestException("Order not found")
        );
        orderRepository.deleteById(order.getId());
        return "Order has been delete";
    }

    @Transactional
    @Override
    public String deleteAllPlaceOrderInRestaurant() {
        var restaurant = restaurantService.findRestaurantByOwner(UserContext.getUser());
        var orders = orderRepository.findAllByRestaurant(restaurant);
        orderRepository.deleteAll(orders);
        return "All orders have been deleted in restaurant";
    }

    private OrderResponse convertToOrderResponse(Order order) {
        Restaurant restaurant = order.getRestaurant();
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUser(this.modelMapper.map(order.getUser(), UserResponse.class));
        response.setRestaurant(RestaurantImp.mapToRestaurantResponse(restaurant));
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(order.getStatus().name());
        response.setItems(order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getFood().getId(),
                        item.getQuantity(),
                        item.getFood().getPrice() * item.getQuantity(),
                        item.getFood().getName(),
                        item.getFood().getPrice()
                ))
                .toList());
        return response;
    }

}
