# GymAdminApp

Proyecto integrador de Programación II del IFES. Aplicación web para administrar socios, personal, planes, membresías, pagos y accesos de un gimnasio.

**Integrantes:** Juan Alvarez, Pablo Ruiz Diaz y Mauro Presso.

## Primera versión de código

Incluye ABM de socios y personal con bajas lógicas, consulta de planes, contratación y renovación con un pago completo, ficha e historiales, recepción, vencimientos, panel y comprobante interno imprimible. El servidor calcula precios y períodos. Los reenvíos utilizan una clave idempotente y las operaciones de un mismo socio se serializan en MySQL.

Es una demostración académica local: escucha en `127.0.0.1`, mantiene CSRF en formularios y no incorpora autenticación ni roles. Los planes iniciales y sus importes son ficticios. No procesa tarjetas o transferencias: registra pagos previamente recibidos. Máquinas, rutinas, reservas, tienda y facturación fiscal quedan fuera del alcance.

## Requisitos y versiones

| Componente | Versión fijada |
|---|---|
| JDK | Java 17 |
| Maven | 3.6.3 o posterior; verificado con 3.9.11 |
| Spring Boot / MVC / Thymeleaf | 3.5.16 y dependencias administradas por su parent |
| JDO API | 3.2.1 |
| DataNucleus core / api-jdo / rdbms | 6.0.11 / 6.0.5 / 6.0.11 |
| Plugin enhancer | 6.0.1, asociado a `process-classes` |
| MySQL Connector/J | 8.4.0 |
| MySQL Server | 8.4 LTS; entorno de verificación 8.4.13 |
| Bootstrap | 5.3.8, servido desde un WebJar; no depende de una CDN |
| Doxygen / Graphviz | Generación comprobada con Doxygen 1.15.0 y Graphviz |

Se conserva JDO `javax.jdo` para seguir la materia; las validaciones web usan `jakarta.validation` de Spring Boot. No se incorpora JPA ni Hibernate.

## Preparar MySQL

Instalar MySQL Server y arrancar su servicio. En el cliente MySQL, con una cuenta administradora, ejecutar:

```sql
SOURCE db/crear-bases.sql;
```

Crear una cuenta local y otorgarle acceso a estos dos esquemas. En el siguiente ejemplo, reemplazar `TU_CLAVE_LOCAL` por una contraseña propia antes de ejecutarlo:

```sql
CREATE USER 'gymadmin'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
GRANT ALL PRIVILEGES ON gymadmin.* TO 'gymadmin'@'localhost';
GRANT ALL PRIVILEGES ON gymadmin_test.* TO 'gymadmin'@'localhost';
```

Si la cuenta ya existe, utilizarla o modificarla desde MySQL. JDO crea y valida las tablas al iniciar; no borra los datos existentes. Es necesario disponer de permisos para crear tablas e índices en esos esquemas. No se incluyen contraseñas reales en el repositorio.

## Compilar y ejecutar en Windows PowerShell

Desde la carpeta del repositorio, comprobar `java -version` y `mvn -version`. Maven debe informar un **JDK 17 completo**, no solo un JRE. Configurar la contraseña para esta sesión:

```powershell
$env:GYM_DB_USER = 'gymadmin'
$env:GYM_DB_PASSWORD = Read-Host 'Contraseña local de MySQL'
mvn clean verify
java -jar target/gymadmin-1.0.0-SNAPSHOT.jar
```

Abrir <http://127.0.0.1:8080>. Detener con `Ctrl+C`. Al volver a ejecutar el mismo JAR con el mismo esquema, se conservan personas, contratos, pagos y visitas. El catálogo inicial se agrega una sola vez.

Si MySQL utiliza otro puerto o servidor:

```powershell
$env:GYM_DB_URL = 'jdbc:mysql://localhost:3306/gymadmin?connectionTimeZone=America/Argentina/Buenos_Aires'
```

En Linux/macOS se utilizan las mismas variables con `export`. Para desarrollo, `mvn process-classes spring-boot:run` asegura que el enhancer se ejecute antes del arranque. El comando de entrega preferido es el JAR empaquetado.

## Pruebas

`mvn clean verify` compila, ejecuta enhancement, corre pruebas unitarias y empaqueta. No necesita una base para las pruebas unitarias.

Las pruebas de integración requieren MySQL y **eliminan los movimientos del esquema de prueba**. El nombre debe terminar en `_test`; no apuntarlas a la base de uso diario.

```powershell
$env:GYM_TEST_DB_URL = 'jdbc:mysql://localhost:3306/gymadmin_test?connectionTimeZone=America/Argentina/Buenos_Aires'
$env:GYM_TEST_DB_USER = 'gymadmin'
$env:GYM_TEST_DB_PASSWORD = $env:GYM_DB_PASSWORD
mvn clean verify -Pmysql-it
```

Los reportes quedan en `target/surefire-reports` y `target/failsafe-reports`. La integración prueba la reversión real de un contrato si falla el pago, herencia, DNI, relaciones detached, reenvíos, concurrencia, reglas semanales y renderizado MVC. Un mock del reloj fija fechas; la base y los repositorios persisten realmente en MySQL. Mockito usa su generador por subclases, sin requerir que la JVM se adjunte un agente durante las pruebas.

## Documentación Doxygen

Instalar [Doxygen](https://www.doxygen.nl/download.html) y [Graphviz](https://graphviz.org/download/), agregarlos al PATH y ejecutar desde la raíz:

```powershell
doxygen Doxyfile
Start-Process target/doxygen/html/index.html
```

Las clases y métodos públicos/protegidos contienen documentación de responsabilidad, parámetros y retornos. Se generan diagramas de herencia y relaciones, índice de clases y código navegable. La generación falla ante advertencias de documentación. Ver [guía de Doxygen](docs/DOXYGEN.md).

## Documentación del proyecto

| Documento | Contenido |
|---|---|
| [Especificación](docs/ESPECIFICACION.md) | Alcance, modelo y reglas R01 a R18 |
| [Arquitectura y materia](docs/ARQUITECTURA.md) | Capas, transacciones y relación con las unidades |
| [Pruebas de aceptación](docs/PRUEBAS_ACEPTACION.md) | Casos T01 a T32 y evidencias manuales requeridas |
| [Verificación del código](docs/VERIFICACION.md) | Resultados obtenidos y comprobaciones pendientes |
| [Implementación y defensa](docs/IMPLEMENTACION_Y_DEFENSA.md) | Hitos y recorrido para la defensa grupal |

El flujo de GitHub Actions compila con Java 17, ejecuta la integración contra un servicio MySQL y genera Doxygen. Sus reportes se adjuntan como artefactos de cada ejecución; su presencia en el repositorio no implica que una ejecución remota ya esté aprobada.

## Organización y configuración

`dominio` contiene las entidades; `repositorio` recibe administradores JDO; `servicio` controla casos de uso y transacciones; `controlador` gestiona HTTP; `templates` contiene Thymeleaf. La fábrica se construye con `META-INF/persistence.xml` y las credenciales externas, no mediante propiedades JPA. Cada operación abre y cierra su propio administrador. Las vistas reciben DTO y copias detached con las relaciones necesarias.

Las variables disponibles son `GYM_DB_URL`, `GYM_DB_USER`, `GYM_DB_PASSWORD` y `GYM_PORT` (8080 por defecto). `GYM_ADDRESS` permite cambiar la dirección de escucha si el entorno de demostración lo requiere. No versionar archivos locales con credenciales ni el directorio `target`.

## Diagnóstico

- Si falta `GYM_DB_PASSWORD`, configurarla antes de arrancar. Un arranque fallido no confirma operaciones.
- Si hay rechazo de conexión, comprobar el servicio MySQL, puerto, esquema y permisos del usuario.
- Si JDO informa clases sin enhancement, ejecutar `mvn clean verify` y utilizar el JAR recién generado.
- Si el puerto 8080 está ocupado, definir `GYM_PORT` con otro puerto.

Cada integrante debe revisar el código, realizar aportes propios y poder explicar un recorrido completo desde el formulario hasta la base. La documentación organiza la defensa y registra evidencia; la evaluación depende del funcionamiento y la comprensión individual.
