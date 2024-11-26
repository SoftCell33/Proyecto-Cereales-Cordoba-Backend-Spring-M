package usco.edu.co.Backend.dto;


import lombok.Data;
import lombok.Builder;
import usco.edu.co.Backend.enums.OrderStatus;
import usco.edu.co.Backend.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private Long id;
    private String userEmail;
    private OrderStatus status;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private List<OrderItemDTO> items;
}
