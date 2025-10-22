package es.unex.gc01.paymentsservice.backend.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import es.unex.gc01.paymentsservice.backend.models.DAO.Payment;
import es.unex.gc01.paymentsservice.backend.models.PaymentStatus;
import es.unex.gc01.paymentsservice.backend.repositories.PaymentRepository;
import es.unex.gc01.paymentsservice.backend.views.DTO.PaymentDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentDTO createPayment(PaymentDTO dto) {
        Payment payment = Payment.builder()
                .artist(dto.getArtistId()) // Se asigna el ID del artista
                .concept(dto.getConcept())
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : new Date())
                .amountPaid(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PENDING)
                .build();

        return toDTO(paymentRepository.save(payment));
    }

    public List<PaymentDTO> filterPayments(Long artistId, Integer month, Integer year, PaymentStatus status, BigDecimal minAmount, BigDecimal maxAmount) {
        // La lógica de filtrado es correcta, ya que opera sobre el ID del artista
        return paymentRepository.findAll().stream()
                .filter(p -> (artistId == null || p.getArtist().equals(artistId)) &&
                        (month == null || p.getPaymentDate().getMonth() + 1 == month) &&
                        (year == null || p.getPaymentDate().getYear() + 1900 == year) &&
                        (status == null || p.getStatus() == status) &&
                        (minAmount == null || p.getAmountPaid().compareTo(minAmount) >= 0) &&
                        (maxAmount == null || p.getAmountPaid().compareTo(maxAmount) <= 0))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * CAMBIO: El nombre del artista ahora se pasa como parámetro.
     * Este servicio no es responsable de conocer el nombre, solo el ID.
     * La capa superior (Controller) debe obtener el nombre del microservicio de artistas y pasarlo aquí.
     */
    public byte[] generatePaymentReceipt(Long paymentId, String artistName) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago con ID " + paymentId + " no encontrado"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Solo se puede generar comprobante para pagos completados.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Comprobante de Pago").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(18));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("ID del pago: " + payment.getId()));
        // CAMBIO: Se utiliza el nombre del artista que viene por parámetro.
        document.add(new Paragraph("Artista: " + artistName));
        document.add(new Paragraph("Fecha: " + payment.getPaymentDate().toString()));
        document.add(new Paragraph("Cantidad: €" + payment.getAmountPaid()));
        document.add(new Paragraph("Método de pago: " + payment.getPaymentMethod().toString())); // O usa .getDescription() si lo tienes
        document.add(new Paragraph("Concepto: " + payment.getConcept()));

        document.close();
        return out.toByteArray();
    }

    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                // CAMBIO: Se mapea correctamente el ID del artista guardado en la entidad Payment.
                .artistId(payment.getArtist())
                .concept(payment.getConcept())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .build();
    }

    public PaymentDTO updatePayment(Long paymentId, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        // Se actualizan los campos desde el DTO. No se necesita el repositorio de artistas.
        payment.setArtist(paymentDTO.getArtistId());
        payment.setConcept(paymentDTO.getConcept());
        payment.setPaymentDate(paymentDTO.getPaymentDate());
        payment.setAmountPaid(paymentDTO.getAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setStatus(paymentDTO.getStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        return toDTO(updatedPayment);
    }

    public List<PaymentDTO> getPaymentsByArtist(Long artistId) {
        return paymentRepository.findByArtist(artistId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsByArtistOrdered(Long artistId) {
        return paymentRepository.findByArtistOrderByPaymentDateDesc(artistId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago con ID " + paymentId + " no encontrado"));
        return toDTO(payment);
    }
}