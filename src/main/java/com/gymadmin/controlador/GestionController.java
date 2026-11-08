package com.gymadmin.controlador;

import com.gymadmin.dominio.ReglaNegocioException;
import com.gymadmin.servicio.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;

/** Panel, catalogo, recepcion y vencimientos. */
@Controller
public class GestionController {
    private final ConsultaService consultas;
    private final AccesoService accesos;
    private final Clock reloj;

    /**
     * @param consultas lecturas de las pantallas
     * @param accesos movimientos
     * @param reloj fecha
     *     local de negocio
     */
    public GestionController(ConsultaService consultas, AccesoService accesos, Clock reloj) {
        this.consultas = consultas;
        this.accesos = accesos;
        this.reloj = reloj;
    }

    /**
     * @param model datos comunes a las pantallas de gestion
     */
    @ModelAttribute
    public void fecha(Model model) {
        model.addAttribute("hoy", LocalDate.now(reloj));
    }

    /**
     * @param model datos de la vista
     * @return panel de inicio
     */
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("panel", consultas.panel());
        return "inicio";
    }

    /**
     * @param model datos de la vista
     * @return catalogo de planes
     */
    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("planes", consultas.planes());
        return "planes";
    }

    /**
     * @param q criterio de busqueda
     * @param model datos de la vista
     * @return panel de recepcion
     */
    @GetMapping("/recepcion")
    public String recepcion(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("resultados", consultas.recepcion(q));
        return "recepcion";
    }

    /**
     * @param id identidad del socio
     * @param flash mensajes posteriores
     * @return recepcion
     */
    @PostMapping("/recepcion/{id}/ingreso")
    public String ingreso(@PathVariable long id, RedirectAttributes flash) {
        try {
            accesos.ingresar(id);
            flash.addFlashAttribute("exito", "Ingreso registrado.");
        } catch (ReglaNegocioException | PersistenciaException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/recepcion";
    }

    /**
     * @param id identidad del socio
     * @param flash mensajes posteriores
     * @return recepcion
     */
    @PostMapping("/recepcion/{id}/salida")
    public String salida(@PathVariable long id, RedirectAttributes flash) {
        try {
            accesos.salir(id);
            flash.addFlashAttribute("exito", "Salida registrada.");
        } catch (ReglaNegocioException | PersistenciaException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/recepcion";
    }

    /**
     * @param estado tipo de vencimiento
     * @param dias horizonte
     * @param model datos
     * @return listado de
     *     vencimientos
     */
    @GetMapping("/vencimientos")
    public String vencimientos(
            @RequestParam(defaultValue = "POR_VENCER") String estado,
            @RequestParam(defaultValue = "7") int dias,
            Model model) {
        model.addAttribute("estado", estado);
        model.addAttribute("dias", dias);
        model.addAttribute("resultados", consultas.vencimientos(estado, dias));
        return "vencimientos";
    }
}
