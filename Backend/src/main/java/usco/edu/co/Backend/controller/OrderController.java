package usco.edu.co.Backend.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import usco.edu.co.Backend.dto.*;
import usco.edu.co.Backend.enums.PaymentMethod;
import usco.edu.co.Backend.model.Order;
import usco.edu.co.Backend.model.User;
import usco.edu.co.Backend.service.OrderService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = orderService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody CreateOrderRequest request
    ) {
        try {
            if (!isValidPaymentMethod(request.getPaymentMethod())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Método de pago no válido"));
            }

            OrderResponseDTO order = orderService.createOrder(user.getEmail(), request);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al crear el pedido"));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                System.out.println("Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Usuario no autenticado"));
            }

            System.out.println("Obteniendo órdenes para usuario: " + user.getEmail());
            List<OrderResponseDTO> orders = orderService.getUserOrders(user.getEmail());
            System.out.println("Órdenes encontradas: " + orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("Error al obtener órdenes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user
    ) {
        try {
            OrderResponseDTO order = orderService.getOrderById(orderId, user.getEmail());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener el pedido"));
        }
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() {
        try {
            return ResponseEntity.ok(Map.of(
                    "paymentMethods", Arrays.stream(PaymentMethod.values())
                            .map(method -> Map.of(
                                    "value", method.name(),
                                    "label", method.getDescription()
                            ))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener los métodos de pago"));
        }
    }

    @PostMapping("/process-paypal")
    public ResponseEntity<?> processPaypalOrder(
            @AuthenticationPrincipal User user,
            @RequestBody PayPalOrderRequest request
    ) {
        try {
            System.out.println("==== Iniciando procesamiento de orden PayPal ====");
            System.out.println("Usuario: " + user.getEmail());
            System.out.println("Request recibida: " + request);

            if (request == null) {
                throw new RuntimeException("Request es null");
            }
            if (request.getItems() == null) {
                throw new RuntimeException("Items es null");
            }
            System.out.println("Cantidad de items: " + request.getItems().size());

            OrderResponseDTO order = orderService.processPayPalOrder(
                    user.getEmail(),
                    request
            );

            System.out.println("Orden creada exitosamente: " + order.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            System.err.println("==== Error procesando orden PayPal ====");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private boolean isValidPaymentMethod(PaymentMethod paymentMethod) {
        return paymentMethod != null &&
                (paymentMethod == PaymentMethod.CASH_ON_DELIVERY ||
                        paymentMethod == PaymentMethod.PAYPAL);
    }


}
