package com.gymadmin.controlador;

import com.gymadmin.controlador.formulario.ContratoFormulario;
import com.gymadmin.dominio.*;
import com.gymadmin.servicio.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/** Contratacion con precio del servidor y comprobante interno imprimible. */
@Controller
public class MembresiaController {
    private final MembresiaService membresias;
    private final SocioService socios;

    /**
     * @param membresias contratacion y comprobantes
     * @param socios datos del titular
     */
    public MembresiaController(MembresiaService membresias, SocioService socios) {
        this.membresias = membresias;
        this.socios = socios;
    }

    /**
     * @param id identidad del titular
     * @param model datos de la vista
     * @return formulario con vista
     *     previa
     */
    @GetMapping("/socios/{id}/contratar")
    public String formulario(@PathVariable long id, Model model) {
        ContratoFormulario form = new ContratoFormulario();
        form.setClaveOperacion(UUID.randomUUID().toString());
        model.addAttribute("form", form);
        preparar(id, model);
        return "contrato";
    }

    /**
     * Confirma la operacion y redirige al comprobante del pago nuevo o existente.
     *
     * @param id identidad del socio
     * @param form datos enviados sin precio modificable
     * @param errores validacion del formulario
     * @param model datos de la vista
     * @param flash mensajes posteriores
     * @return comprobante o formulario con errores
     */
    @PostMapping("/socios/{id}/contratar")
    public String contratar(
            @PathVariable long id,
            @Valid @ModelAttribute("form") ContratoFormulario form,
            BindingResult errores,
            Model model,
            RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                long pagoId =
                        membresias.contratar(
                                id, form.getPlanId(), form.getMetodo(), form.getClaveOperacion());
                flash.addFlashAttribute(
                        "exito", "Operación registrada. Un reenvío conserva el mismo comprobante.");
                return "redirect:/pagos/" + pagoId;
            } catch (ReglaNegocioException | PersistenciaException ex) {
                errores.reject("operacion", ex.getMessage());
            }
        }
        preparar(id, model);
        return "contrato";
    }

    /**
     * @param id identidad del pago
     * @param model datos de la vista
     * @return comprobante HTML
     */
    @GetMapping("/pagos/{id}")
    public String comprobante(@PathVariable long id, Model model) {
        model.addAttribute("pago", membresias.comprobante(id));
        return "comprobante";
    }

    private void preparar(long id, Model model) {
        model.addAttribute("socio", socios.obtener(id));
        try {
            model.addAttribute("opciones", membresias.opciones(id));
        } catch (ReglaNegocioException ex) {
            model.addAttribute("bloqueo", ex.getMessage());
            model.addAttribute("opciones", java.util.List.of());
        }
        model.addAttribute("metodos", MetodoPago.values());
    }
}
