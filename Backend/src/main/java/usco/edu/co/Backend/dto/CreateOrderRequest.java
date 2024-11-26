package usco.edu.co.Backend.dto;


import lombok.Data;
import usco.edu.co.Backend.enums.PaymentMethod;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemDTO> items;
    private PaymentMethod paymentMethod;
}
