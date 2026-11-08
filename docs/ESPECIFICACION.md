# Especificación de GymAdminApp

## Propósito y alcance

Reemplazar el registro en Excel por una aplicación web que permita conocer quién es socio, qué período tiene contratado, qué pagó y si puede ingresar. La primera versión concentra las operaciones de recepción y la administración del personal. Conserva los datos históricos y permite explicar su funcionamiento con las tecnologías de Programación II.

Se implementarán ABM de socios y personal; consulta de planes; contratación y renovación con pago; registro de ingresos y salidas; búsquedas; ficha del socio; vencimientos; panel de recepción y comprobante interno. Máquinas, reservas de clases, rutinas, sueldos, sucursales, tienda, proveedores y notificaciones por correo quedan fuera de esta versión.

## Modelo de dominio

Cada entidad persistente tendrá identidad generada, encapsulamiento y constructor compatible con JDO. Los nombres de tablas y columnas se definirán en el mapeo y se verificarán contra MySQL.

| Entidad | Datos y responsabilidad |
|---|---|
| Persona abstracta | Identidad, nombre, apellido, DNI y teléfono comunes a socios y empleados. |
| Socio | Número de socio estable y único, fecha de inscripción, estado activo, historial de membresías y asistencias. |
| Empleado | Estado activo y tipo. Los empleados de recepción y limpieza se representan con esta clase. |
| Entrenador | Subclase de Empleado con especialidad y horario obligatorio. Su tipo siempre es ENTRENADOR. |
| PlanMembresia | Nombre, precio vigente, duración positiva en días, límite semanal y estado activo para nuevas contrataciones. |
| Membresia | Socio, plan, inicio, vencimiento inclusivo, precio acordado, duración y límite semanal contratados. Conserva las condiciones del período. |
| Pago | Membresía contratada, fecha y hora, importe, método, número de comprobante único y clave de operación única. |
| Asistencia | Socio, membresía utilizada al ingresar, fecha y hora de ingreso y salida opcional mientras la visita está abierta. |

TipoEmpleado contiene RECEPCIONISTA, LIMPIEZA y ENTRENADOR. El tipo de Entrenador se fija por su clase y se valida en el servicio; no se edita como un dato independiente. En esta versión, la edición del personal no convierte un objeto de una clase persistente en otra.

Especialidad y MetodoPago serán enumeraciones con los valores previstos por el grupo. No requieren tablas de catálogo mientras no se necesite administrarlos desde una pantalla. MetodoPago contempla EFECTIVO, TRANSFERENCIA y TARJETA.

Se usarán BigDecimal para importes, String para DNI y teléfono, LocalDate para períodos y LocalDateTime para movimientos. El último pago, la vigencia actual y el consumo semanal se consultan o calculan; no se mantienen como copias editables en Socio.

## Relaciones y herencia

| Relación | Cardinalidad y condición |
|---|---|
| Persona con Socio y Empleado | Herencia; datos comunes en la clase base. |
| Empleado con Entrenador | Herencia; atributos particulares en la subclase. |
| Socio con Membresia | Un socio tiene cero o muchas membresías históricas; cada membresía pertenece a un socio. |
| PlanMembresia con Membresia | Un plan puede originar muchas contrataciones; cada contratación corresponde a un plan. |
| Membresia con Pago | Un pago completo por membresía en esta versión, creado en la misma transacción. La referencia al contrato debe ser única. |
| Socio con Asistencia | Un socio puede tener muchas visitas; cada visita pertenece a un socio. |
| Membresia con Asistencia | Cada visita referencia la membresía utilizada; debe pertenecer al mismo socio. |

La estrategia implementada es NEW_TABLE para la jerarquía persistente, con identidad y DNI único en la tabla base Persona y referencias de las subclases. Permite centralizar la unicidad del DNI. Es una decisión entre las estrategias trabajadas en la materia. Las pruebas de integración verifican las clases concretas y sus campos; el esquema generado queda como referencia en `db/esquema-referencia.sql`. No se considera validada únicamente por el diagrama.

## Reglas de socios y personal

R01. Nombre, apellido y DNI son obligatorios. El DNI se normaliza antes de comparar y no puede repetirse en la jerarquía, incluso si un registro está inactivo.

R02. El número de socio se genera de forma estable y única. No se calcula contando filas ni se reutiliza al dar de baja un socio.

R03. Las bajas son lógicas. Pagos, membresías y asistencias se conservan. Para dar de baja un socio con una visita abierta, primero se debe registrar su salida.

R04. Un socio inactivo no puede contratar, renovar ni ingresar. Su historial sigue siendo consultable. La reactivación no borra consumos semanales ni reemplaza membresías.

## Reglas de membresías y pagos

R05. La primera contratación comienza hoy. Si el último período terminó antes de hoy, una nueva contratación comienza hoy. Si continúa vigente, la renovación comienza al día siguiente de su vencimiento.

R06. Las fechas finales son inclusivas. Con duración D mayor que cero, vencimiento = inicio + D - 1 días. Una membresía de un día comienza y vence en la misma fecha.

R07. No se superponen períodos de un mismo socio. Puede existir un período vigente y una renovación futura; para mantener simple la operación inicial, no se admite otra renovación anticipada si ya existe una futura.

R08. El servicio consulta el plan activo y determina el importe. El formulario no puede imponer un precio distinto. Precio, duración y límite semanal se copian al contratar; cambios posteriores en el plan no modifican esas condiciones históricas.

R09. Cada período se paga completamente en una sola operación. No se incluyen cuotas, saldos, reintegros ni cancelación de períodos cobrados en esta versión. Los importes deben ser positivos.

R10. Crear la membresía y registrar el pago constituye una única transacción JDO, con el mismo PersistenceManager. Si falla una parte, no se confirma ninguna de las dos.

R11. Cada solicitud de contratación tiene una clave única. Reenviar la misma operación no genera otro cobro ni otro período. Una restricción de unicidad respalda el control y la respuesta identifica la operación ya registrada.

R12. Después de un registro exitoso se redirige a la consulta del resultado. El comprobante interno muestra número, socio, importe, método y período. No se presenta como factura fiscal.

## Reglas de acceso

R13. El día y la semana se interpretan en America/Argentina/Buenos_Aires. La semana va de lunes a domingo. El reloj se obtiene mediante una dependencia controlable para las pruebas.

R14. Bronce permite dos días distintos por semana; Plata, tres; Oro no tiene límite semanal. Se cuentan las fechas de ingreso distintas del socio en la semana actual, aunque cambie de período durante esa semana.

R15. El límite se verifica solo si el ingreso utilizaría un día nuevo. Si un socio Bronce ya asistió martes y jueves, puede reingresar el jueves después de su salida; no puede agregar el viernes como tercer día.

R16. No se permite el ingreso de un socio inactivo, sin membresía aplicable hoy, con membresía vencida, con una visita ya abierta o con el límite semanal agotado para un día nuevo.

R17. La salida corresponde a una visita abierta y debe ser posterior al ingreso. Puede registrarse aunque la membresía haya vencido durante la visita. La salida no vuelve a comprobar la autorización de entrada.

R18. Dos solicitudes simultáneas no deben dejar dos ingresos abiertos ni períodos superpuestos. El mecanismo transaccional implementado se describe en Arquitectura y se comprueba con administradores y transacciones independientes en MySQL.

## Pantallas y comportamiento esperado

| Pantalla | Acciones y datos |
|---|---|
| Inicio | Cantidad de socios activos, visitas abiertas y vencimientos próximos. Accesos directos a recepción y socios. |
| Socios | Alta, edición, baja y reactivación; búsqueda por nombre, apellido, DNI o número; filtro por estado. |
| Ficha del socio | Datos, membresía aplicable hoy, renovación futura e historiales de contratos, pagos y asistencias. |
| Personal | ABM con baja lógica y filtro por tipo; datos particulares del entrenador. |
| Planes | Consulta de Bronce, Plata y Oro, precio, duración y límite. Se utilizan en la contratación. |
| Contratar o renovar | Socio, plan y método; vista previa del precio y período; confirmación transaccional. |
| Recepción | Búsqueda rápida, estado de acceso, registro de ingreso o salida y motivo claro de un rechazo. |
| Vencimientos | Filtros de vencidas, vigentes y por vencer; horizonte expresado en días. |
| Comprobante | Datos del pago y período; presentación HTML legible e imprimible. |

Las validaciones se ejecutan en el servidor y se muestran junto al campo o como mensaje de la operación. Los listados incluyen un estado vacío útil. La navegación permite volver a los listados y a la ficha sin perder la referencia del socio. Los colores acompañan textos de estado y no son la única forma de comunicar un resultado.

## Evidencia de terminación

Una funcionalidad se considera terminada cuando tiene recorrido web completo, validación, persistencia, manejo del error y evidencia de aceptación. La definición de los casos se encuentra en [Pruebas de aceptación](PRUEBAS_ACEPTACION.md).
