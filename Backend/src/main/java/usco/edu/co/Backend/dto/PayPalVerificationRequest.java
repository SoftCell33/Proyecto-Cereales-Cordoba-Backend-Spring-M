package usco.edu.co.Backend.dto;

import lombok.Data;

@Data
public class PayPalVerificationRequest {
    private String orderId;
    private String transactionId;
}
