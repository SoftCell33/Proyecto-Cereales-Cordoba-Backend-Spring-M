package usco.edu.co.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import usco.edu.co.Backend.model.OrderItem;


import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPalOrderRequest {
    private String orderId;
    private PayPalOrderDetails orderDetails;
    private List<OrderItemDTO> items;
    private Double totalAmount;
}
