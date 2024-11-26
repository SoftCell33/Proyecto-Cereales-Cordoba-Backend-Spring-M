package usco.edu.co.Backend.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Pago contra entrega"),
    PAYPAL("Pago con Paypal");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
