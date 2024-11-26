package usco.edu.co.Backend.enums;

public enum OrderStatus {
    PENDING("Pendiente"),
    PROCESSING("En proceso"),
    PAID("Pagado"),
    COMPLETED("Completado"),
    CANCELLED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
