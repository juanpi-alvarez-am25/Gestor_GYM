# GymAdminApp

Proyecto integrador de Programación II del IFES. Aplicación web propuesta para administrar socios, personal, planes, membresías, pagos y accesos de un gimnasio.

**Integrantes:** Juan Alvarez, Pablo Ruiz Diaz y Mauro Presso.

## Estado del proyecto

Este repositorio contiene la especificación para implementar y verificar la aplicación. El código ejecutable, la configuración de la base de datos y las pruebas automatizadas todavía no están incorporados. Los documentos de esta propuesta describen el comportamiento esperado; no representan funcionalidades ya implementadas ni pruebas aprobadas.

## Documentación de trabajo

| Documento | Contenido |
|---|---|
| [Especificación](docs/ESPECIFICACION.md) | Alcance, entidades, relaciones, reglas y pantallas |
| [Arquitectura y materia](docs/ARQUITECTURA.md) | Aplicación de las unidades, capas y decisiones de persistencia |
| [Pruebas de aceptación](docs/PRUEBAS_ACEPTACION.md) | Casos, resultados esperados y registro de evidencias |
| [Implementación y defensa](docs/IMPLEMENTACION_Y_DEFENSA.md) | Hitos, criterios de terminación y demostración grupal |

## Alcance de la primera versión

- ABM de socios y personal, con bajas lógicas.
- Consulta de los planes Bronce, Plata y Oro.
- Contratación y renovación con pago completo, conservando el historial.
- Registro de ingresos y salidas con control de vigencia y días semanales.
- Ficha del socio, búsquedas y filtros por vencimiento.
- Panel de recepción y comprobante interno de pago imprimible desde HTML.

## Tecnologías requeridas

Java, Maven, DataNucleus JDO, MySQL, Spring, Spring Boot, MVC, Thymeleaf, HTML, CSS y Bootstrap. Se tomará Java 17 como base del ejemplo docente. Las versiones exactas de las dependencias se fijarán en el primer proyecto ejecutable y se conservarán después de verificar su compatibilidad; no se declara todavía una combinación probada.

## Ejecución y validación

La guía de instalación se incorporará junto con los archivos reales. Para considerar terminada la entrega, deberá permitir a otro integrante preparar la base, configurar credenciales locales, compilar, ejecutar la aplicación y comprobar la persistencia después de reiniciarla. No se publican comandos que dependan de archivos inexistentes.

## Forma de trabajo

Cada tarea se desarrolla en una rama y se revisa mediante una solicitud de cambios. Los commits deben reflejar aportes reales y describir su propósito. Antes de integrar código, deben pasar la compilación y las comprobaciones correspondientes. Los tres integrantes deben comprender el funcionamiento general y poder recorrer una operación desde la vista hasta la base de datos.

El cumplimiento de la consigna y la defensa individual determinan la evaluación. Esta propuesta organiza el trabajo; no garantiza una calificación.
