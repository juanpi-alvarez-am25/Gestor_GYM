package com.gymadmin.controlador;

import com.gymadmin.controlador.formulario.PersonaFormulario;
import com.gymadmin.dominio.ReglaNegocioException;
import com.gymadmin.servicio.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Traduce solicitudes de socios a casos de uso y vistas Thymeleaf. */
@Controller
@RequestMapping("/socios")
public class SocioController {
    private final SocioService socios;
    private final ConsultaService consultas;

    /**
     * Inyecta casos de uso, sin acceso directo a JDO.
     *
     * @param socios gestion de socios
     * @param consultas lecturas de la ficha
     */
    public SocioController(SocioService socios, ConsultaService consultas) {
        this.socios = socios;
        this.consultas = consultas;
    }

    /**
     * Muestra busqueda y estados vacios.
     *
     * @param q criterio de busqueda
     * @param estado filtro administrativo
     * @param model datos de la vista
     * @return plantilla del listado
     */
    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "TODOS") String estado,
            Model model) {
        model.addAttribute("socios", socios.buscar(q, estado));
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        return "socios/lista";
    }

    /**
     * @param model datos de la vista
     * @return formulario de alta
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new PersonaFormulario());
        return "socios/formulario";
    }

    /**
     * Valida el alta y aplica Post/Redirect/Get.
     *
     * @param form campos enviados
     * @param errores resultado de validacion
     * @param flash mensajes de la redireccion
     * @return formulario con errores o ficha del nuevo socio
     */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("form") PersonaFormulario form,
            BindingResult errores,
            RedirectAttributes flash) {
        if (errores.hasErrors()) return "socios/formulario";
        try {
            long id =
                    socios.crear(
                            form.getNombre(),
                            form.getApellido(),
                            form.getDni(),
                            form.getTelefono());
            flash.addFlashAttribute("exito", "Socio registrado.");
            return "redirect:/socios/" + id;
        } catch (ReglaNegocioException | PersistenciaException ex) {
            errores.reject("operacion", ex.getMessage());
            return "socios/formulario";
        }
    }

    /**
     * @param id identidad del socio
     * @param model datos de la vista
     * @return ficha con historiales
     */
    @GetMapping("/{id}")
    public String ficha(@PathVariable long id, Model model) {
        model.addAttribute("ficha", consultas.ficha(id));
        return "socios/ficha";
    }

    /**
     * @param id identidad del socio
     * @param model datos de la vista
     * @return formulario de edicion
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("form", new PersonaFormulario(socios.obtener(id)));
        model.addAttribute("id", id);
        return "socios/formulario";
    }

    /**
     * Guarda datos personales sin reemplazar el objeto persistente.
     *
     * @param id identidad del socio
     * @param form campos enviados
     * @param errores resultado de validacion
     * @param model datos de la vista
     * @param flash mensajes posteriores
     * @return ficha o formulario corregible
     */
    @PostMapping("/{id}")
    public String guardar(
            @PathVariable long id,
            @Valid @ModelAttribute("form") PersonaFormulario form,
            BindingResult errores,
            Model model,
            RedirectAttributes flash) {
        model.addAttribute("id", id);
        if (!errores.hasErrors()) {
            try {
                socios.editar(
                        id,
                        form.getNombre(),
                        form.getApellido(),
                        form.getDni(),
                        form.getTelefono());
                flash.addFlashAttribute("exito", "Datos actualizados.");
                return "redirect:/socios/" + id;
            } catch (ReglaNegocioException | PersistenciaException ex) {
                errores.reject("operacion", ex.getMessage());
            }
        }
        return "socios/formulario";
    }

    /**
     * Cambia el estado por POST, conservando movimientos.
     *
     * @param id identidad del socio
     * @param activo estado solicitado
     * @param flash mensajes posteriores
     * @return ficha del titular
     */
    @PostMapping("/{id}/estado")
    public String estado(
            @PathVariable long id, @RequestParam boolean activo, RedirectAttributes flash) {
        try {
            socios.cambiarEstado(id, activo);
            flash.addFlashAttribute(
                    "exito",
                    activo ? "Socio reactivado." : "Baja registrada; se conserva el historial.");
        } catch (ReglaNegocioException | PersistenciaException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/socios/" + id;
    }
}
