package com.pawconnect.store;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateCart(UUID petParentId) {
        return cartRepository.findByPetParentId(petParentId)
                .orElseGet(() -> cartRepository.save(Cart.builder().petParentId(petParentId).build()));
    }

    @Transactional
    public void addItem(UUID petParentId, UUID productId, int quantity) {
        Cart cart = getOrCreateCart(petParentId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(CartItem.builder().cartId(cart.getId()).productId(productId).quantity(0).build());

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateItemQuantity(UUID petParentId, UUID productId, int quantity) {
        Cart cart = getOrCreateCart(petParentId);
        if (quantity <= 0) {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                    .ifPresent(cartItemRepository::delete);
        } else {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        cartItemRepository.save(item);
                    });
        }
    }

    @Transactional(readOnly = true)
    public CartDetails getCartDetails(UUID petParentId) {
        Cart cart = getOrCreateCart(petParentId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemDetail> itemDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p != null) {
                BigDecimal itemTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
                total = total.add(itemTotal);
                itemDetails.add(CartItemDetail.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .imageUrl(p.getImageUrl())
                        .unitPrice(p.getPrice())
                        .quantity(ci.getQuantity())
                        .itemTotal(itemTotal)
                        .coinEligible(p.getCoinEligible())
                        .coinsReward(p.getCoinsReward())
                        .build());
            }
        }

        return CartDetails.builder()
                .cartId(cart.getId())
                .items(itemDetails)
                .totalAmount(total)
                .build();
    }

    @Transactional
    public void clearCart(UUID petParentId) {
        cartRepository.findByPetParentId(petParentId)
                .ifPresent(c -> cartItemRepository.deleteByCartId(c.getId()));
    }

    @Data
    @Builder
    public static class CartDetails {
        private UUID cartId;
        private List<CartItemDetail> items;
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    public static class CartItemDetail {
        private UUID productId;
        private String name;
        private String imageUrl;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal itemTotal;
        private boolean coinEligible;
        private Integer coinsReward;
    }
}
