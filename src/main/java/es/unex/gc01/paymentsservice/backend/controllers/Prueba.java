package es.unex.gc01.paymentsservice.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Prueba {
    @GetMapping("/prueba")
    public String prueba() {
        return "¡La aplicación está funcionando correctamente!";
    }
}
