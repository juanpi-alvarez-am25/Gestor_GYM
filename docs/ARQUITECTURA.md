# Arquitectura y relación con la materia

## Organización propuesta

La aplicación será un proyecto Maven con paquetes de dominio, repositorio, servicio, controlador y configuración. Las vistas estarán en templates y los recursos CSS e imágenes en static. Esta separación en paquetes cubre las responsabilidades requeridas; conocer proyectos multimódulo no obliga a dividir esta aplicación en varios módulos.

| Capa | Responsabilidad | Elementos propuestos |
|---|---|---|
| Dominio | Estado, identidad, relaciones y comportamiento de las entidades | Persona, Socio, Empleado, Entrenador, PlanMembresia, Membresia, Pago y Asistencia |
| Repositorio | Consultas y escrituras JDO | IRepositorio con operaciones comunes y repositorios de consultas específicas |
| Servicio | Casos de uso, validaciones y unidad de trabajo | SocioService, PersonalService, MembresiaService y AccesoService |
| Controlador | Solicitudes, formularios, Model y elección de vistas | Controladores MVC por funcionalidad |
| Vista | Presentación, navegación y mensajes | Plantillas Thymeleaf con HTML, CSS y Bootstrap |
| Configuración | Componentes e infraestructura | PersistenceManagerFactory, unidad JDO, reloj y propiedades de conexión |

## Aplicación de los contenidos

| Material | Aplicación concreta | Evidencia prevista |
|---|---|---|
| Unidad 1 | Maven, pom.xml, dependencias, plugins y ciclo de construcción | Compilación y empaquetado desde una copia limpia |
| Unidad 2 | Estructura de proyectos y comprensión de arquetipos y módulos | Explicación del proyecto y sus directorios; un módulo suficiente para este alcance |
| Unidad 3 | ORM, JDO, entidades y transacciones | Guardar, consultar y recuperar datos desde MySQL |
| Unidad 4 | Herencia, relaciones y estados de objetos | Jerarquía Persona, relaciones de historial y uso seguro de datos luego de cerrar la persistencia |
| Unidad 5 y clase 12 video 1 | IoC, contenedor y beans | Componentes administrados por Spring, con dependencias explícitas |
| Unidad 6 y clase 12 videos 2 y 3 | Configuración Java e inyección | Configuration, Bean para infraestructura e inyección en servicios y repositorios |
| Unidad 7 y clases 13 y 14 | HTML, formularios, tablas, CSS, Bootstrap, cliente-servidor y MVC | Solicitud HTTP, controlador, vista y datos de gestión reales |
| Consigna del integrador | Spring Boot, Model y Thymeleaf | Arranque web y datos dinámicos en formularios y listados |

El contraste de videos se efectuó sobre pantallas y ejemplos visibles. No se contó con una transcripción completa del audio. Spring Boot y Thymeleaf son requisitos explícitos de la consigna; no se afirma que su implementación ya haya sido identificada en las clases 12 a 14.

## Configuración de Spring

Spring administrará los servicios, repositorios y una única PersistenceManagerFactory. La configuración de infraestructura se expresará mediante los mecanismos enseñados, como Configuration y Bean. Los componentes se descubrirán en el paquete base correcto.

Las dependencias se inyectarán de forma consistente. La inyección por constructor hace visibles las dependencias obligatorias y permite sustituirlas en pruebas. Los socios y pagos creados por cada operación son datos de dominio; no se registrará un bean compartido para cada fila de la base.

La integración con Spring Boot debe conservar estos conceptos. No se crearán contextos manuales independientes dentro de cada controlador o servicio.

## Persistencia y transacciones

La unidad JDO y la construcción de la PersistenceManagerFactory se configurarán explícitamente, tomando como referencia el uso de META-INF/persistence.xml del docente. Debe documentarse cómo se aplican las credenciales locales; escribir propiedades de JDO en application.properties no demuestra por sí solo que se hayan cargado en la fábrica.

Cada caso de uso abre su PersistenceManager, ejecuta la unidad de trabajo y lo cierra al terminar. Todas las escrituras relacionadas comparten la misma transacción. Los repositorios participantes no deben confirmar por separado partes de la contratación. Ante una excepción se revierte la transacción activa y se propaga un error que el controlador pueda presentar de forma clara.

Las consultas y sus resultados se cierran. Antes de devolver datos a las vistas se cargan los campos y relaciones necesarios y se separan los objetos del contexto, o se construyen objetos de datos dentro de la operación. La vista no provoca consultas sobre un PersistenceManager cerrado. La profundidad de carga debe responder a lo que utiliza cada pantalla; no se cargará todo el historial para cada fila de un listado general.

El ejemplo docente de IRepositorio y Repositorio constituye una base para comprender persistencia y detach. Debe adaptarse al servicio transaccional compuesto: copiar sin cambios métodos que abren y confirman transacciones independientes no garantiza la atomicidad de membresía y pago.

## Recorrido de contratación

1. El controlador recibe socio, plan, método y clave de operación. Valida el formato del formulario.
2. MembresiaService abre la unidad de trabajo, consulta las referencias y verifica socio activo, plan disponible, duplicación y períodos previos.
3. Determina inicio, fin inclusivo e importe y conserva las condiciones contratadas.
4. Los repositorios guardan membresía y pago con el mismo administrador y transacción.
5. El servicio confirma o revierte la operación completa.
6. El controlador redirige a la ficha o al comprobante. Thymeleaf presenta el resultado y los mensajes.

## Identidad y consistencia

Se propondrá NEW_TABLE para la jerarquía, de modo que el DNI único se verifique en la tabla base. Las subclases conservan la identidad de Persona. Esta elección se comprobará inspeccionando el esquema generado y persistiendo y consultando cada tipo; la aprobación del hito requiere esos resultados.

Las relaciones se expresarán mediante referencias y colecciones JDO, con claves foráneas y restricciones apropiadas. No se borrarán socios, planes ni movimientos referenciados para resolver una baja. La unicidad de DNI, número de socio, comprobante, solicitud y pago por membresía debe estar respaldada por la base cuando corresponda.

## Base técnica y compatibilidad

El pom.xml docente consultado utiliza Java 17, JDO 3.2.1, DataNucleus core 6.0.11, api-jdo 6.0.5 y rdbms 6.0.11, MySQL Connector/J 8.4.0 y plugin enhancer 6.0.1. Estos datos describen ese ejemplo, no una combinación de Spring Boot ya comprobada para GymAdminApp.

Se fijará una única combinación compatible en el primer hito. Antes de declararla apta deben funcionar compilación, enhancement, arranque web y una operación JDO contra MySQL. La guía de instalación final identificará la versión del servidor MySQL y las dependencias exactas. Una base en memoria, si se utiliza para pruebas rápidas, no reemplaza la comprobación contra la base elegida para la entrega.

## Referencias de trabajo

- [Consigna y propuesta compartida](https://docs.google.com/document/d/1SNZ8984_Fr8OAzh3zMIb2pdM4oEklJoZbubK52QFYvY).
- Apuntes de las unidades 1 a 4 y ejercicios de las unidades 5 a 7 compartidos en el proyecto.
- Videos de las clases 12, 13 y 14 compartidos en el proyecto.
- [Repositorio docente](https://github.com/juanjosenava75-bot/proyecto-jdo).
- [Persona del ejemplo docente](https://github.com/juanjosenava75-bot/proyecto-jdo/blob/main/src/main/java/com/programacion2/dominio/Persona.java).
- [Repositorio JDO del ejemplo docente](https://github.com/juanjosenava75-bot/proyecto-jdo/blob/main/src/main/java/com/programacion2/repositorio/Repositorio.java).

La tabla de trazabilidad aplica conceptos de la materia al dominio del gimnasio. Las decisiones de negocio del gimnasio son propuestas del grupo, no exigencias atribuidas al docente.
