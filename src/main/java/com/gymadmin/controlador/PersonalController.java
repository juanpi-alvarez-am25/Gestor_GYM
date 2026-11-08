package com.gymadmin.controlador;

import com.gymadmin.controlador.formulario.PersonalFormulario;
import com.gymadmin.dominio.*;
import com.gymadmin.servicio.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Formularios y consultas de empleados y entrenadores. */
@Controller
@RequestMapping("/personal")
public class PersonalController {
    private final PersonalService personal;

    /**
     * @param personal casos de uso de personal
     */
    public PersonalController(PersonalService personal) {
        this.personal = personal;
    }

    /**
     * @param model datos comunes a los formularios
     */
    @ModelAttribute
    public void catalogos(Model model) {
        model.addAttribute("tipos", TipoEmpleado.values());
        model.addAttribute("especialidades", Especialidad.values());
    }

    /**
     * @param tipo filtro opcional
     * @param model datos de la vista
     * @return listado de personal
     */
    @GetMapping
    public String listar(@RequestParam(required = false) TipoEmpleado tipo, Model model) {
        model.addAttribute("personal", personal.listar(tipo));
        model.addAttribute("tipo", tipo);
        return "personal/lista";
    }

    /**
     * @param model datos de la vista
     * @return formulario de alta
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new PersonalFormulario());
        return "personal/formulario";
    }

    /**
     * @param id identidad del empleado
     * @param model datos de la vista
     * @return formulario de edicion
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("form", new PersonalFormulario(personal.obtener(id)));
        model.addAttribute("id", id);
        return "personal/formulario";
    }

    /**
     * Da de alta la clase persistente correspondiente al tipo seleccionado.
     *
     * @param form campos recibidos
     * @param errores validaciones de formato
     * @param flash mensajes posteriores
     * @return listado o formulario con errores
     */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("form") PersonalFormulario form,
            BindingResult errores,
            RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                personal.crear(
                        form.getNombre(),
                        form.getApellido(),
                        form.getDni(),
                        form.getTelefono(),
                        form.getTipo(),
                        form.getEspecialidad(),
                        form.getHorario());
                flash.addFlashAttribute("exito", "Personal registrado.");
                return "redirect:/personal";
            } catch (ReglaNegocioException | PersistenciaException ex) {
                errores.reject("operacion", ex.getMessage());
            }
        }
        return "personal/formulario";
    }

    /**
     * Guarda una edicion conservando la clase persistente.
     *
     * @param id identidad
     * @param form campos recibidos
     * @param errores validaciones de formato
     * @param model datos de la vista
     * @param flash mensajes posteriores
     * @return listado o formulario con errores
     */
    @PostMapping("/{id}")
    public String guardar(
            @PathVariable long id,
            @Valid @ModelAttribute("form") PersonalFormulario form,
            BindingResult errores,
            Model model,
            RedirectAttributes flash) {
        model.addAttribute("id", id);
        if (!errores.hasErrors()) {
            try {
                personal.editar(
                        id,
                        form.getNombre(),
                        form.getApellido(),
                        form.getDni(),
                        form.getTelefono(),
                        form.getTipo(),
                        form.getEspecialidad(),
                        form.getHorario());
                flash.addFlashAttribute("exito", "Personal actualizado.");
                return "redirect:/personal";
            } catch (ReglaNegocioException | PersistenciaException ex) {
                errores.reject("operacion", ex.getMessage());
            }
        }
        return "personal/formulario";
    }

    /**
     * @param id identidad
     * @param activo estado solicitado
     * @param flash mensajes
     * @return listado de
     *     personal
     */
    @PostMapping("/{id}/estado")
    public String estado(
            @PathVariable long id, @RequestParam boolean activo, RedirectAttributes flash) {
        try {
            personal.cambiarEstado(id, activo);
            flash.addFlashAttribute("exito", "Estado actualizado.");
        } catch (ReglaNegocioException | PersistenciaException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/personal";
    }
}
