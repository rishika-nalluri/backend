package com.pawconnect.store;

import com.pawconnect.reward.CoinService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Checkout in two steps. placeOrder creates a PAYMENT_PENDING order and a Razorpay order, and changes
 * nothing else. confirmPayment runs only after Razorpay's signature checks out. Coins are spent and stock
 * is reserved at that point, so an unpaid order costs the customer nothing and earns nothing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal MAX_COIN_SHARE = new BigDecimal("0.30");
    private static final String PENDING = "PAYMENT_PENDING";

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CoinService coinService;
    private final PaymentRepository paymentRepository;
    private final RazorpayGateway gateway;

    public record CheckoutResult(Order order, String razorpayOrderId, String razorpayKeyId,
                                 long amountPaise, String currency) {
    }

    @Transactional
    public CheckoutResult placeOrder(UUID petParentId, String shippingAddress, int coinsToUse) {
        if (!gateway.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Online payments are not configured");
        }

        CartService.CartDetails cart = cartService.getCartDetails(petParentId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot place an order with an empty cart");
        }
        for (CartService.CartItemDetail item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "A product is no longer available"));
            if (product.getStockQty() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough stock for " + product.getName());
            }
        }

        BigDecimal subtotal = cart.getTotalAmount();
        int coins = 0;
        if (coinsToUse > 0) {
            int cap = subtotal.multiply(MAX_COIN_SHARE).setScale(0, RoundingMode.DOWN).intValue();
            int requested = Math.min(coinsToUse, cap);
            if (requested > 0) {
                if (coinService.getBalance(petParentId) < requested) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough PawCoins");
                }
                coins = requested;
            }
        }

        BigDecimal discount = BigDecimal.valueOf(coins);
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        if (total.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order total must be greater than zero");
        }

        Order order = orderRepository.save(Order.builder()
                .petParentId(petParentId)
                .totalAmount(total)
                .discountAmount(discount)
                .coinsUsed(coins)
                .shippingAddress(shippingAddress)
                .status(PENDING)
                .build());

        for (CartService.CartItemDetail item : cart.getItems()) {
            orderItemRepository.save(OrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build());
        }

        long paise = total.movePointRight(2).longValueExact();
        String razorpayOrderId = gateway.createOrder(paise, order.getId().toString());
        paymentRepository.save(Payment.builder()
                .orderId(order.getId())
                .profileId(petParentId)
                .amount(total)
                .gatewayRef(razorpayOrderId)
                .paymentMethod("RAZORPAY")
                .status("PENDING")
                .build());

        return new CheckoutResult(order, razorpayOrderId, gateway.keyId(), paise, "INR");
    }

    @Transactional
    public Order confirmPayment(UUID petParentId, UUID orderId, String razorpayOrderId,
                                String razorpayPaymentId, String signature) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!petParentId.equals(order.getPetParentId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (!PENDING.equals(order.getStatus())) {
            // Already confirmed. Repeating the call is harmless.
            return order;
        }

        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No payment is pending for this order"));
        if (razorpayOrderId == null || !razorpayOrderId.equals(payment.getGatewayRef())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment does not match this order");
        }
        if (!gateway.verifySignature(razorpayOrderId, razorpayPaymentId, signature)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature is invalid");
        }

        payment.setStatus("COMPLETED");
        payment.setGatewayRef(razorpayPaymentId);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        if (order.getCoinsUsed() != null && order.getCoinsUsed() > 0) {
            coinService.redeemCoins(petParentId, order.getCoinsUsed(), "STORE_PURCHASE_DISCOUNT", orderId);
        }
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                // Stock was checked at checkout. Never go negative if it changed since.
                p.setStockQty(Math.max(0, p.getStockQty() - item.getQuantity()));
                productRepository.save(p);
            });
        }

        order.setStatus("PLACED");
        Order placed = orderRepository.save(order);
        cartService.clearCart(petParentId);
        try {
            coinService.awardCoins(petParentId, "STORE_PURCHASE", orderId);
        } catch (RuntimeException e) {
            log.warn("Could not award purchase coins for order {}: {}", orderId, e.getMessage());
        }
        return placed;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(UUID petParentId) {
        return orderRepository.findByPetParentIdOrderByPlacedAtDesc(petParentId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(UUID orderId, UUID petParentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getPetParentId().equals(petParentId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        return orderItemRepository.findByOrderId(orderId);
    }
}
