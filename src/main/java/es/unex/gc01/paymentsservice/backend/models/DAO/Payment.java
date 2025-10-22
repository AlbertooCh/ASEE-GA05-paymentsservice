package es.unex.gc01.paymentsservice.backend.models.DAO;

import es.unex.gc01.paymentsservice.backend.models.PaymentMethod;
import es.unex.gc01.paymentsservice.backend.models.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;  // Identificador único para el pago

    @JoinColumn(name = "artist_id", nullable = false)
    private Long artist;  // Artista que recibió el pago

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;  // Monto del pago

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date paymentDate;  // Fecha del pago

    @Column(nullable = false, length = 100)
    private String concept;  // Concepto del pago (Descripción breve)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;  // Método de pago (Transferencia, PayPal, etc.)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;  // Estado del pago: PENDING, COMPLETED, FAILED
}
