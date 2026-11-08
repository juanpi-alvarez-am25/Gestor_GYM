# Implementación y defensa de GymAdminApp

## Hitos de implementación

| Hito | Resultado | Condición para avanzar |
|---|---|---|
| 1 Base ejecutable | Proyecto Maven, dependencias fijadas, enhancer, arranque Spring Boot, unidad JDO y conexión MySQL. | Una vista obtiene desde el controlador datos guardados por JDO; el esquema y el reinicio funcionan. |
| 2 Gestión básica | Modelo persistente, herencia, ABM de socios y personal, consulta de planes, búsquedas y bajas lógicas. | Se verifican identidad, unicidad, edición, consulta de relaciones y conservación del historial. |
| 3 Operaciones de negocio | Contratación y pago atómicos, renovación anticipada y vencida, ingreso y salida, control semanal y reenvíos. | Pasan los casos de fechas, límites, duplicados, reversión y concurrencia. |
| 4 Entrega y defensa | Fichas, vencimientos, panel, comprobante, instalación real y documentación fiel al código. | Otro equipo ejecuta el proyecto desde cero y se conserva la evidencia de aceptación. |

Las mejoras de presentación se realizan sobre recorridos ya funcionales. Máquinas, clases, rutinas y tienda se revisan únicamente si estos hitos están completos.

## Estructura prevista

| Ubicación | Contenido que deberá existir al implementar |
|---|---|
| pom.xml | Java, dependencias, compiler, enhancement y empaquetado ejecutable |
| src/main/java/com/gymadmin/dominio | Entidades, relaciones y enumeraciones |
| src/main/java/com/gymadmin/repositorio | Acceso JDO y consultas |
| src/main/java/com/gymadmin/servicio | Casos de uso y reglas |
| src/main/java/com/gymadmin/controlador | Controladores MVC y formularios |
| src/main/java/com/gymadmin/configuracion | Persistencia, reloj y configuración Spring |
| src/main/resources/templates | Plantillas Thymeleaf |
| src/main/resources/static | CSS y recursos de presentación |
| src/main/resources/META-INF | Unidad de persistencia JDO cuando corresponda a la integración elegida |
| db | Preparación de la base e instrucciones de datos de demostración |
| src/test/java | Pruebas de reglas y de integración |

El README ejecutable se escribirá a partir de estos archivos reales y de una instalación comprobada. Las credenciales personales se reciben por configuración local o variables de entorno y no se publican.

## Reparto sugerido

La siguiente distribución es una propuesta que el grupo puede ajustar. No describe aportes ya realizados.

| Integrante | Foco propuesto | Revisión cruzada |
|---|---|---|
| Mauro Presso | Dominio, herencia, relaciones y repositorios JDO | Revisa persistencia de las operaciones implementadas por Juan |
| Juan Alvarez | Configuración Spring, servicios y transacciones de negocio | Revisa el recorrido de los formularios implementados por Pablo |
| Pablo Ruiz Diaz | Controladores MVC, Thymeleaf, navegación y presentación | Revisa los listados e historiales construidos sobre el modelo de Mauro |

Los tres participan en pruebas, README y demostración. Cada integrante debe recorrer también una operación ajena a su foco y explicar su colaboración real con commits y decisiones concretas.

## Demostración sugerida de doce minutos

| Tiempo orientativo | Responsable sugerido | Demostración y explicación |
|---|---|---|
| 0 a 4 minutos | Mauro | Problema, alcance, modelo y relaciones; alta de socio; consulta desde la base y explicación de herencia y estados JDO. |
| 4 a 8 minutos | Juan | Contratación con pago; renovación; recorrido del servicio y de la transacción; explicación de commit y rollback. |
| 8 a 12 minutos | Pablo | Ficha, recepción, reingreso permitido y rechazo por límite; MVC, Model y Thymeleaf; navegación, mensajes e instalación. |

La duración se ajusta al tiempo indicado por el docente. Los datos de demostración deben permitir mostrar los casos sin modificar manualmente el reloj del sistema durante la defensa. Puede prepararse una base de demostración y un entorno de pruebas con reloj fijo, claramente identificados.

## Preguntas que todos deben poder responder

1. ¿Qué dependencia gestiona Maven y qué tarea cumple el enhancer?
2. ¿Qué diferencia hay entre Persona, Socio y Entrenador y cómo se persisten?
3. ¿Dónde está la relación uno a muchos y cómo se crea desde la web?
4. ¿Qué administra Spring y qué dependencia recibe cada componente?
5. ¿Por qué la vista no accede directamente al repositorio?
6. ¿Qué pasa si falla el pago luego de iniciar la contratación?
7. ¿Cómo se recuperan datos relacionados antes de cerrar la persistencia?
8. ¿Cómo se diferencia un plan de una membresía contratada?
9. ¿Por qué se permite otro ingreso el jueves pero no el viernes en el ejemplo Bronce?
10. ¿Qué impide duplicar un cobro al reenviar el formulario?
11. ¿Qué datos permanecen después de una baja y de reiniciar la aplicación?
12. ¿Qué parte hizo cada integrante y qué prueba demuestra que funciona?

## Condiciones de entrega

La entrega requiere código fuente, pom.xml, configuración sin secretos, preparación de MySQL, README ejecutable, casos verificados y demostración. Deben eliminarse las afirmaciones que no correspondan al estado real, completar decisiones de versiones y registrar limitaciones. La calificación depende de la aplicación y de la comprensión individual mostrada en la defensa.
