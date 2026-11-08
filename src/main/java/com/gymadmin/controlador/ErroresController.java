package com.gymadmin.controlador;

import com.gymadmin.dominio.ReglaNegocioException;
import com.gymadmin.servicio.PersistenciaException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Presenta errores esperados sin volcar excepciones tecnicas en HTML. */
@ControllerAdvice
public class ErroresController {
    /**
     * @param ex rechazo de negocio
     * @param model datos de la vista
     * @return pagina legible con estado
     *     400
     */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String negocio(ReglaNegocioException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

    /**
     * @param ex fallo de infraestructura
     * @param model datos de la vista
     * @return pagina legible con
     *     estado 503
     */
    @ExceptionHandler(PersistenciaException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String persistencia(PersistenciaException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }
}
