package usco.edu.co.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPalOrderDetails {
    private String id;
    private String status;
    private String payerEmail;
    private Double amount;
    private String currencyCode;
    private String createTime;
}
