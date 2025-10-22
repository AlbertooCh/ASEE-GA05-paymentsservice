package es.unex.gc01.paymentsservice.backend.controllers;

import es.unex.gc01.paymentsservice.backend.exceptions.general.ProcessingException;
import es.unex.gc01.paymentsservice.backend.models.PaymentStatus;
import es.unex.gc01.paymentsservice.backend.services.PaymentService;
import es.unex.gc01.paymentsservice.backend.views.DTO.PaymentDTO;
import es.unex.gc01.paymentsservice.backend.views.DTO.external.ArtistDTO; // CAMBIO: Importar el nuevo DTO
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient; // CAMBIO: Importar WebClient
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    // CAMBIO: Inyectar WebClient.Builder para crear el cliente HTTP
    private final WebClient.Builder webClientBuilder;

    // CAMBIO: Inyectar la URL del servicio de artistas desde application.properties
    @Value("${services.artists.url}")
    private String artistsServiceUrl;

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentService.createPayment(paymentDTO));
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getFilteredPayments(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        List<PaymentDTO> filtered = paymentService.filterPayments(artistId, month, year, status, minAmount, maxAmount);
        return ResponseEntity.ok(filtered);
    }

    /**
     * CAMBIO: Este endpoint ahora orquesta llamadas a dos microservicios.
     * 1. Llama a este mismo servicio (pagos) para obtener los detalles del pago (incluido el artistId).
     * 2. Llama al servicio de artistas para obtener el nombre del artista.
     * 3. Llama al método del servicio de pagos para generar el PDF, pasándole el nombre.
     */
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadPaymentReceipt(@PathVariable Long paymentId) {
        try {
            // 1. Obtener los datos del pago para saber el ID del artista
            PaymentDTO payment = paymentService.getPaymentById(paymentId);
            Long artistId = payment.getArtistId();

            // 2. Llamar al microservicio de artistas para obtener el nombre
            ArtistDTO artist = webClientBuilder.build()
                    .get()
                    .uri(artistsServiceUrl + "/api/artists/" + artistId) // Endpoint del otro microservicio
                    .retrieve()
                    .bodyToMono(ArtistDTO.class)
                    .block(); // .block() hace la llamada síncrona, espera la respuesta

            String artistName = (artist != null) ? artist.getArtisticName() : "Nombre no encontrado";

            // 3. Generar el PDF pasando el nombre del artista
            byte[] pdf = paymentService.generatePaymentReceipt(paymentId, artistName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante_pago_" + paymentId + ".pdf\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            throw new ProcessingException("Error al generar el comprobante de pago: " + e.getMessage());
        }
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> updatePayment(@PathVariable Long paymentId, @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO updatedPayment = paymentService.updatePayment(paymentId, paymentDTO);
        return ResponseEntity.ok(updatedPayment);
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByArtist(@PathVariable Long artistId) {
        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByArtist(artistId);
            return ResponseEntity.ok(payments);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/artist/{artistId}/ordered")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByArtistOrdered(@PathVariable Long artistId) {
        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByArtistOrdered(artistId);
            return ResponseEntity.ok(payments);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}