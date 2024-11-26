package usco.edu.co.Backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usco.edu.co.Backend.dto.*;
import usco.edu.co.Backend.enums.OrderStatus;
import usco.edu.co.Backend.enums.PaymentMethod;
import usco.edu.co.Backend.model.Order;
import usco.edu.co.Backend.model.OrderItem;
import usco.edu.co.Backend.model.Product;
import usco.edu.co.Backend.model.User;
import usco.edu.co.Backend.repository.OrderRepository;
import usco.edu.co.Backend.repository.ProductRepository;
import usco.edu.co.Backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;




    public List<ProductDTO> getAllProducts() {
        return productRepository.findByAvailableTrue()
                .stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO createOrder(String userEmail, CreateOrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getProductId()));

            if (!product.isAvailable()) {
                throw new RuntimeException("Producto no disponible: " + product.getName());
            }

            double itemTotal = product.getPrice() * itemDTO.getQuantity();
            totalAmount += itemTotal;

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice())
                    .build());
        }


        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .items(items)
                .build();


        items.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponseDTO(savedOrder);
    }

    public List<OrderResponseDTO> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return orderRepository.findByUserOrderByOrderDateDesc(user)
                .stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("No autorizado para ver esta orden");
        }

        return mapToOrderResponseDTO(order);
    }


    private ProductDTO mapToProductDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .available(product.isAvailable())
                .build();
    }


    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .paymentMethod(order.getPaymentMethod())
                .paymentReference(order.getPaymentReference())
                .items(itemDTOs)
                .build();
    }

    public OrderResponseDTO processPayPalOrder(String userEmail, PayPalOrderRequest request) {
        try {
            System.out.println("Iniciando procesamiento de orden PayPal");
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<OrderItem> items = new ArrayList<>();
            double totalAmount = 0.0;

            System.out.println("Procesando items: " + request.getItems().size());


            for (OrderItemDTO itemDTO : request.getItems()) {
                System.out.println("Procesando item: " + itemDTO);

                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getProductId()));

                System.out.println("Producto encontrado: " + product.getId());

                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .price(itemDTO.getPrice())
                        .build();

                items.add(orderItem);
                totalAmount += itemDTO.getPrice() * itemDTO.getQuantity();
            }

            // Crear la orden
            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.COMPLETED)
                    .totalAmount(totalAmount)
                    .orderDate(LocalDateTime.now())
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paypalOrderId(request.getOrderId())
                    .paypalTransactionId(request.getOrderDetails().getId())
                    .paypalPayerEmail(request.getOrderDetails().getPayerEmail())
                    .paymentReference(request.getOrderDetails().getId())
                    .items(new ArrayList<>())
                    .build();


            items.forEach(item -> {
                item.setOrder(order);
                order.getItems().add(item);
            });

            System.out.println("Guardando orden");
            Order savedOrder = orderRepository.save(order);
            System.out.println("Orden guardada con ID: " + savedOrder.getId());

            return mapToOrderResponseDTO(savedOrder);
        } catch (Exception e) {
            System.err.println("Error en processPayPalOrder: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


}

