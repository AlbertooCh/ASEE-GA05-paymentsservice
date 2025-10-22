package es.unex.gc01.paymentsservice.backend.models;

public enum PaymentMethod {
    TRANSFER("Transferencia bancaria"),
    PAYPAL("PayPal"),
    CREDIT_CARD("Tarjeta de crédito"),
    CASH("Efectivo"),
    OTHER("Otro"); // Puedes agregar más métodos de pago si es necesario

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

