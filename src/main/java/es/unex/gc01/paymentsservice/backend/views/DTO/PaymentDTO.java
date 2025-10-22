package es.unex.gc01.paymentsservice.backend.views.DTO;

import es.unex.gc01.paymentsservice.backend.models.PaymentMethod;
import es.unex.gc01.paymentsservice.backend.models.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

// La anotación @Data ya genera todos los getters, setters, toString(), etc.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;
    private Long artistId;
    // CAMBIO: Se elimina el campo artistName. Este DTO solo debe manejar
    // datos que pertenecen al dominio del microservicio de pagos.
    private String concept;
    private Date paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    // CAMBIO: Se eliminan todos los getters y setters manuales.
    // Son redundantes gracias a la anotación @Data de Lombok.
}