# Pruebas de aceptación de GymAdminApp

## Uso del documento

Estos son casos por ejecutar. Ninguno se declara aprobado mientras no exista aplicación y evidencia del resultado. Registrar para cada ejecución commit, entorno, fecha, responsable, datos utilizados, resultado obtenido y enlace o ruta de la evidencia. Los datos deben ser ficticios y las operaciones deben realizarse en una base de prueba.

El reloj de prueba se podrá fijar en lunes 2 de noviembre de 2026, zona America/Argentina/Buenos_Aires. Para las comprobaciones semanales también se utilizarán martes 3, jueves 5, viernes 6 y lunes 9 de noviembre. Los precios son datos de prueba definidos en el juego de demostración, no precios reales del gimnasio.

## Casos funcionales y de persistencia

| Caso | Preparación y acción | Resultado esperado |
|---|---|---|
| T01 Alta y consulta | Crear un socio con datos válidos y buscarlo por DNI y número. | Se encuentra el mismo socio con identidad y número únicos. |
| T02 Campos obligatorios | Enviar un socio sin nombre, apellido o DNI. | Mensajes junto a los campos; no se persiste un registro incompleto. |
| T03 DNI global | Intentar usar el DNI de un socio para otro socio o empleado, incluso tras su baja. | Se rechaza la duplicación y no queda una persona parcialmente creada. |
| T04 Edición | Cambiar teléfono y apellido; volver al listado y a la ficha. | Conserva identidad, número e historial y muestra los nuevos datos. |
| T05 Personal y herencia | Crear recepción, limpieza y entrenador; editar datos del entrenador y consultar por tipo. | Se recuperan los datos comunes y particulares. Entrenador siempre informa tipo ENTRENADOR. |
| T06 Planes | Listar Bronce, Plata y Oro y elegir uno al contratar. | Sus límites y duración son visibles y la contratación referencia el plan elegido. |
| T07 Contratación completa | Contratar para un socio activo con pago válido. | Una membresía, un pago, importe y fechas correctos; comprobante consultable. |
| T08 Precio del servidor | Modificar en la solicitud el importe mostrado por el formulario. | El servidor aplica el precio del plan o rechaza la discrepancia; nunca cobra un importe arbitrario del cliente. |
| T09 Reversión | Forzar una falla de persistencia después de crear una parte y antes de confirmar. | La base no conserva ni contrato ni pago parcial. Un mensaje informa el error. |
| T10 Reenvío de operación | Repetir la misma solicitud de contratación con la misma clave. | Se identifica el resultado existente. No se duplican cobro, comprobante ni período. |
| T11 Renovación anticipada | Renovar un período vigente que vence el 10 de noviembre. | El nuevo comienza el 11. No altera el período vigente y queda visible como futuro. |
| T12 Renovación vencida | Fijar hoy 9 de noviembre y renovar una membresía que terminó el 6. | El nuevo período comienza el 9, sin cobrar días anteriores no contratados. |
| T13 Período de un día | Contratar un plan de duración un día. | Inicio y vencimiento coinciden; puede ingresar durante esa fecha. |
| T14 Cambio de condiciones | Contratar; cambiar precio o límite del plan mediante preparación de prueba; consultar el contrato. | Conserva precio, duración y límite acordados originalmente. |
| T15 Límite Bronce | Registrar visitas cerradas martes y jueves; intentar un ingreso el viernes. | Rechazo por tercer día distinto; no se crea una asistencia adicional. |
| T16 Reingreso permitido | Con las visitas del caso anterior, reingresar el jueves tras haber salido. | Se permite; continúa consumiendo dos días semanales. |
| T17 Reinicio semanal | Tras consumir dos días, fijar el lunes siguiente e ingresar con membresía válida. | Comienza el conteo de otra semana. |
| T18 Pase libre | Socio Oro con más de tres días de visita en la semana. | El límite semanal no impide un nuevo ingreso. Se mantienen las otras validaciones. |
| T19 Visita abierta | Intentar registrar otro ingreso sin salida del anterior. | Se rechaza y sigue existiendo una sola visita abierta. |
| T20 Salida y vencimiento | Ingresar con membresía válida y registrar la salida luego de su vencimiento. | Se permite cerrar la visita y se conserva el historial. |
| T21 Baja lógica | Cerrar visitas, dar de baja al socio y consultar sus movimientos. | No se pierde el historial y se rechazan nuevas contrataciones e ingresos. |
| T22 Solicitudes concurrentes | Enviar simultáneamente dos ingresos o contrataciones para el mismo socio. | No quedan visitas abiertas duplicadas, cobros duplicados ni períodos superpuestos. Se informa el conflicto de forma controlada. |
| T23 Reinicio real | Guardar socio, contrato, pago y asistencia; detener y arrancar nuevamente el proceso. | Todos los datos se recuperan desde MySQL y mantienen sus relaciones. |
| T24 Datos fuera del contexto | Consultar ficha e historiales después del cierre del PersistenceManager. | La vista accede a los datos preparados sin errores de carga de relaciones. |
| T25 Fechas límite | Consultar e ingresar el día del vencimiento y al día siguiente. | El día final está incluido; al día siguiente se requiere otra membresía aplicable. |
| T26 Búsqueda y filtros | Usar nombre, DNI, número, estado y horizonte de vencimiento. | Resultados coherentes; búsqueda sin coincidencias muestra un estado vacío claro. |
| T27 Segunda renovación futura | Con una renovación futura ya registrada, enviar una nueva contratación distinta. | Rechazo claro, sin otro pago ni otro período futuro. |
| T28 Salida sin ingreso | Registrar salida sin visita abierta o con hora anterior al ingreso. | Se rechaza sin alterar movimientos anteriores. |

## Interfaz y ejecución

| Caso | Acción | Resultado esperado |
|---|---|---|
| T29 Navegación | Recorrer todas las pantallas desde el menú y desde la ficha. | Enlaces correctos, regreso al listado y referencia del socio coherente. |
| T30 Presentación | Revisar formularios, tablas, mensajes y comprobante en escritorio y ancho de teléfono. | Texto legible, etiquetas visibles y acciones utilizables; los estados no dependen solo del color. |
| T31 Equipo limpio | Otro integrante clona el commit, prepara la base y sigue el README. | Compila, ejecuta el enhancer, arranca y completa una operación sin pasos omitidos. |
| T32 Error de infraestructura | Ejecutar con base detenida o credenciales inválidas. | Fallo identificable, sin exposición de contraseñas ni confirmación falsa de operaciones. |

## Automatización prioritaria

Automatizar las fechas y el conteo semanal con reloj fijo; la operación compuesta con su reversión; unicidad y duplicados; herencia y relaciones JDO; carga de datos para las vistas; y control concurrente. Las comprobaciones de interfaz y ejecución limpia complementan esas pruebas. El caso de reversión debe verificar el contenido real de la base, no solamente una llamada a un mock.

## Registro de ejecución

| Caso | Commit y entorno | Responsable y fecha | Resultado obtenido | Evidencia |
|---|---|---|---|---|
| Por registrar | Por registrar | Por registrar | Pendiente | Sin ejecución |

La entrega debe identificar los casos aprobados, fallidos o pendientes y las limitaciones conocidas. No se reemplaza evidencia por la presencia de este listado.
