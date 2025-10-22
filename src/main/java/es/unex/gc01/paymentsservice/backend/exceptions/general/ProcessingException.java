package es.unex.gc01.paymentsservice.backend.exceptions.general;

/**
 * Excepción personalizada que se lanza cuando ocurre un error durante el procesamiento de una operación.
 * Esta clase extiende RuntimeException.
 */
public class ProcessingException extends RuntimeException {

    /**
     * Constructor que crea una excepción con un mensaje personalizado y una causa.
     * Este constructor es útil cuando se desea proporcionar detalles adicionales sobre el error,
     * y también se necesita adjuntar una excepción subyacente que causó el fallo.
     *
     * @param message El mensaje que describe el motivo de la excepción.
     */
    public ProcessingException(String message) {
        super(message);
    }
}
