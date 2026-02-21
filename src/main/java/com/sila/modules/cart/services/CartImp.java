package com.sila.modules.cart.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.BadRequestException;
import com.sila.config.exception.NotFoundException;
import com.sila.modules.cart.dto.CartResponse;
import com.sila.modules.cart.model.Cart;
import com.sila.modules.cart.model.CartItem;
import com.sila.modules.cart.repository.CartItemRepository;
import com.sila.modules.cart.repository.CartRepository;
import com.sila.modules.food.model.Food;
import com.sila.modules.food.services.FoodService;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.repository.UserRepository;
import com.sila.modules.profile.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartImp implements CartService {
    final UserService userService;
    final FoodService foodService;
    final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Override
    public List<CartResponse> getAll() {
        var categories = cartRepository.findAllByUser(UserContext.getUser());
        if (CollectionUtils.isEmpty(categories)) {
            return new ArrayList<>();
        }
        return categories.stream().map(CartResponse::toResponse).toList();
    }

    @Override
    @Transactional
    public void addItemToCart(Long foodId, int quantity) {
        var food = foodService.getById(foodId);
        var foodRestaurant = food.getRestaurant(); // Assuming food has a restaurant field

        var carts = cartRepository.findAllByUserId(UserContext.getUser().getId());

        for (var cart : carts) {
            for (var f : cart.getItems()) {
                var restaruantId = f.getFood().getRestaurant().getId();
                if (Objects.equals(foodRestaurant.getId(), restaruantId)) {
                    cartRepository.save(addItemToCartItemExit(cart, food, quantity));
                    return;
                }
            }
        }
        /** Add new Cart when food from new restaurant */

        User user = UserContext.getUser();
        Cart newCart = new Cart();
        newCart.setUser(user);
        cartRepository.save(addItemToCartItemExit(newCart, food, quantity));
        user.getCarts().add(newCart);
        userRepository.save(user);

    }


    @Override
    public void removeItemFromCart(Long cartId, Long cartItemId) {
        Cart cart = findCartById(cartId);
        var itemsInCart = cart.getItems();
        var exited = cartItemRepository.findById(cartItemId).isPresent();
        if (!exited) {
            throw new BadRequestException("Not found item cart with this id");
        }
        if (itemsInCart.size() == 1) {
            cartItemRepository.deleteAllByIdInBatch(List.of(cartItemId));
            cartRepository.deleteAllByIdInBatch(List.of(cartId));
        } else {
            cart.removeItemById(cartItemId);
            cartRepository.save(cart);
        }
    }

    @Override
    public void updateItemFromCart(Long cartId, Long cartItemId, int quantity) throws Exception {
        Cart cart = findCartById(cartId); // Get cart of currently authenticated user
        CartItem cartItem = cartItemRepository.findByIdAndCart(cartItemId, cart)
                .orElseThrow(() -> new BadRequestException("Cart item not found in your cart"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {

        cartItemRepository.deleteAllByCartId(cartId);

        cartRepository.deleteAllByIdInBatch(Collections.singletonList(cartId));
    }

    /**
     * ==================================== Extra Method ====================================
     **/

    private Cart findCartByUser() throws Exception {
        var user = userService.getById(userService.getProfile().getId());
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private Cart addItemToCartItemExit(Cart cart, Food food, int quantity) {
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getFood().getId().equals(food.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setFood(food);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cart;
    }

    private Cart findCartById(Long cartId) {
        return cartRepository.findById(cartId).orElseThrow(() -> new NotFoundException("Not found cart with this id" + cartId));
    }
}
