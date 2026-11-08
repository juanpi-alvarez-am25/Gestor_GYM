# GymAdminApp: documentacion del codigo

Proyecto integrador de Programacion II de Juan Alvarez, Pablo Ruiz Diaz y Mauro Presso.
El contenido se escribe en castellano. El indice de Doxygen utiliza ingles para
evitar avisos de traducciones incompletas en versiones recientes del generador.

## Generacion

Desde la raiz del repositorio, con Doxygen y Graphviz instalados:

```sh
doxygen Doxyfile
```

Abrir `target/doxygen/html/index.html`. No requiere que MySQL este iniciado.
La configuracion genera el indice de clases, diagramas de herencia y relaciones,
codigo navegable, parametros y retornos. Las advertencias se consideran errores;
`EXTRACT_ALL = NO` mantiene activa la deteccion de API sin documentar.
Los archivos generados pertenecen a `target` y no se versionan.

## Recorrido de una operacion

1. El controlador recibe un formulario, valida su formato y agrega datos al Model.
2. El servicio aplica las reglas y delimita el caso de uso con UnidadTrabajo.
3. Los repositorios reciben el mismo PersistenceManager. No hacen commits propios.
4. JDO persiste las entidades y MySQL respalda sus restricciones.
5. La unidad confirma o revierte y cierra los recursos.
6. Las consultas devuelven DTO y copias detached con los datos que utiliza Thymeleaf.

## Paquetes

| Paquete | Responsabilidad |
|---|---|
| com.gymadmin.dominio | Entidades, herencia, enumeraciones y reglas puras |
| com.gymadmin.repositorio | Consultas JDOQL parametrizadas y escrituras dentro de la unidad |
| com.gymadmin.servicio | ABM, contratacion, pagos, acceso y consultas de pantallas |
| com.gymadmin.servicio.vista | Respuestas independientes del contexto de persistencia |
| com.gymadmin.controlador | HTTP, Model, validacion y Post/Redirect/Get |
| com.gymadmin.controlador.formulario | Datos recibidos desde formularios |
| com.gymadmin.configuracion | Fabrica JDO, reloj, catalogo inicial y proteccion CSRF |

## Decisiones que deben poder defenderse

- Persona centraliza identidad y DNI; NEW_TABLE separa los datos particulares.
- El numero de socio deriva del ID de MySQL, sin contar filas ni reutilizar bajas.
- PlanMembresia es una oferta; Membresia conserva las condiciones contratadas.
- Membresia y Pago se guardan en la misma transaccion.
- La clave de operacion y el contrato de Pago tienen indices unicos.
- Las escrituras del mismo socio se serializan mediante lecturas bloqueantes en MySQL.
- Asistencia reserva una referencia unica al socio mientras la visita esta abierta;
  al salir la referencia se vuelve nula. MySQL permite varios valores nulos en ese indice.
- El reloj inyectado fija la zona America/Argentina/Buenos_Aires y permite pruebas de fechas.
- Spring administra infraestructura y servicios; las entidades no son beans compartidos.

## Alcance y verificacion

Maven ejecuta el enhancer en `process-classes` antes del empaquetado. El plugin de
Spring Boot declara `requiresUnpack` para los tres JAR de DataNucleus: su registro
de complementos necesita leer manifiestos desde archivos fisicos. La unidad XML
enumera todas las clases y prepara el esquema al iniciar con el modo `create`,
sin utilizar modos de borrado. Una prueba de integracion arranca el JAR final
en otro proceso para comprobar esta combinacion.

Las pruebas unitarias comprueban reglas puras y las pruebas de integracion requieren
un esquema MySQL exclusivo terminado en `_test`. La configuracion de la conexion,
los comandos y los resultados se mantienen en el README y en la documentacion de
verificacion del repositorio. La existencia de una pagina generada no sustituye la
ejecucion de las pruebas funcionales.
