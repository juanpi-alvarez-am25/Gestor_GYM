# Verificación de la primera versión

Ejecución del 8 de noviembre de 2026 con asistencia de Codex. Este registro describe resultados observados, no una aprobación del docente ni una revisión ya realizada por los integrantes. El commit exacto se identifica en la solicitud de cambios y en los reportes de GitHub Actions.

## Entorno y resultados

| Comprobación | Resultado observado |
|---|---|
| JDK | Eclipse Temurin 17.0.17, compilación con release 17 |
| Maven | 3.9.11, `clean verify -Pmysql-it` |
| Base real | MySQL Community Server 8.4.13, InnoDB, esquema nuevo y nombres sensibles a mayúsculas |
| Pruebas unitarias | 23 ejecuciones, cero fallos y cero errores |
| Pruebas de integración | 12 ejecuciones, cero fallos y cero errores |
| Enhancement | Ocho clases persistentes procesadas correctamente |
| Empaquetado | JAR ejecutable inicia fuera de Maven y recupera información persistida |
| Doxygen | 1.15.0, HTML y diagramas generados; cero advertencias |

La compilación y la integración también se ejecutaron durante el desarrollo con un servidor configurado sin distinción de mayúsculas. La ejecución final desde limpio utilizó una base nueva sensible a mayúsculas, como el servicio Linux previsto en CI.

## Qué prueban los casos automatizados

| Clase o grupo | Evidencia funcional |
|---|---|
| ReglasDominioTest | DNI, campos, importes, fechas inclusivas, duración de un día, copia histórica del plan, renovación, semana, límites, reingreso, salida y tipos de personal |
| Alta, edición y herencia MySQL | Identidad estable, búsquedas por DNI y número, edición, baja, reactivación y DNI único entre socios y empleados; recuperación de campos base y de Entrenador |
| Contratación y pago | Un período y un pago, importes del servidor, clave idempotente, renovación anticipada y vencida, rechazo de otra futura, condiciones históricas |
| Reversión real | Se inserta una membresía; se provoca un fallo al guardar el pago; una conexión independiente comprueba que no quedó contrato ni pago |
| Acceso y conservación | Visitas abiertas, baja condicionada a salida, Bronce, reingreso del mismo día, reinicio semanal, salida después del vencimiento e historiales tras la baja |
| Dos transacciones simultáneas | Dos solicitudes con la misma clave devuelven el mismo pago; dos ingresos dejan exactamente una visita abierta |
| MVC y formularios | Renderizado de pantallas y relaciones detached, validaciones, protección CSRF y manipulación de un importe del formulario sin alterar el precio cobrado |
| JAR en otro proceso | Inicia el artefacto empaquetado y consulta socio, contrato, pago y visita creados desde otro contexto JDO |

Los reportes reproducibles se generan en `target/surefire-reports` y `target/failsafe-reports`. El código de las pruebas está en `src/test/java`. La prueba de pago usa un spy para provocar el fallo; las inserciones, la reversión y las comprobaciones de estado se realizan en MySQL, no en una base simulada.

## Recorrido HTTP adicional

Se inició el JAR contra MySQL y se completó un recorrido HTTP real, utilizando cookies de sesión y tokens CSRF obtenidos de los formularios:

1. Alta de un socio ficticio.
2. Contratación de Bronce y emisión del comprobante. Un parámetro externo `importe=1.00` no cambió el precio del servidor de $18.000,00.
3. Ingreso, salida y consulta de la ficha con sus movimientos.
4. Detención de MySQL durante el uso: la consulta respondió HTTP 503 con un mensaje legible, sin contraseña ni excepción SQL en el HTML.
5. Reinicio de MySQL y del proceso de aplicación: el mismo socio, contrato, pago, visita y comprobante continuaron disponibles.

Se inspeccionó el DDL obtenido de MySQL: ocho tablas de dominio, claves foráneas de relaciones y herencia, DNI único, tres restricciones únicas de Pago y restricción de visita abierta. [Esquema de referencia](../db/esquema-referencia.sql) contiene estructura, sin datos de personas o movimientos.

## Lo que queda pendiente de aceptación

- Revisión visual en navegador de escritorio y teléfono, navegación con teclado e impresión física del comprobante (T30). El navegador de este entorno no pudo iniciarse por una restricción de sockets Unix. El renderizado MVC y el recorrido HTTP no sustituyen esa revisión visual.
- Instalación desde cero por otro integrante en su equipo Windows siguiendo el README (T31).
- Recorrer y registrar entre los tres integrantes todos los casos T01 a T32, incluidos los escenarios que la automatización cubre solo parcialmente. No se declaran los 32 casos completos aprobados.
- Verificar las ejecuciones remotas de GitHub Actions: el flujo está configurado; los resultados locales no aseguran que una ejecución remota haya terminado correctamente.
- Revisar el código, completar aportes del grupo y ensayar la defensa con sus propios datos ficticios.

## Límites de esta versión

La aplicación se utiliza como demostración local sin autenticación ni roles. Los listados no incorporan paginación y recepción calcula el estado por socio; una base con muchos registros requiere paginación y optimizar esas consultas. El esquema se crea desde metadatos JDO y no incorpora migraciones versionadas para instalaciones con datos de versiones anteriores. El comprobante es interno y los medios de pago se registran después de verificarlos fuera de la aplicación.

Estos límites deben explicarse con el alcance acordado. No impiden continuar la revisión académica de esta primera versión, pero tampoco deben ocultarse durante la defensa.
