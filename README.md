# GymAdminApp

Proyecto integrador de Programación II del IFES. Aplicación web para administrar socios, personal, planes, membresías, pagos y accesos de un gimnasio.

**Integrantes:** Juan Alvarez, Pablo Ruiz Diaz y Mauro Presso.

## Ejecutar desde cero en Windows

### 1. Instalá las herramientas

- **JDK 17.** En el instalador de Temurin, activá las opciones para agregar Java al `Path` y definir `JAVA_HOME`. [Descargar JDK 17 para Windows](https://adoptium.net/temurin/releases/?version=17&os=windows)
- **Apache Maven.** Descargá el ZIP binario, descomprimilo y agregá la carpeta `bin` de Maven al `Path` de Windows. [Guía de instalación](https://maven.apache.org/install.html)
- **MySQL Community Server 8.4 LTS.** Instalalo con el MSI y completá MySQL Configurator. Usá el puerto `3306`, dejá MySQL como servicio de Windows y guardá la contraseña de `root`. [Instalación en Windows](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html)

Abrí una nueva ventana de PowerShell y ejecutá estos comandos. Ambos deben indicar Java 17.

```powershell
java -version
mvn -version
```

### 2. Descargá el proyecto

En GitHub elegí **Code → Download ZIP** y descomprimilo. En el Explorador de Windows, entrá a la carpeta que contiene `pom.xml`, escribí `powershell` en la barra de direcciones y presioná Enter. Todos los comandos siguientes se ejecutan desde esa carpeta.

### 3. Prepará MySQL una sola vez

Desde PowerShell, abrí el cliente de MySQL con la contraseña de `root`. Este comando usa la ubicación habitual del instalador. Si elegiste otra carpeta, ajustá la ruta a `mysql.exe`.

```powershell
& "$env:ProgramFiles\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p
```

Cuando veas `mysql>`, copiá este bloque. Reemplazá `TU_CLAVE_LOCAL` por una contraseña propia para el usuario `gymadmin`.

```sql
SOURCE db/crear-bases.sql;
CREATE USER 'gymadmin'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
GRANT ALL PRIVILEGES ON gymadmin.* TO 'gymadmin'@'localhost';
GRANT ALL PRIVILEGES ON gymadmin_test.* TO 'gymadmin'@'localhost';
exit
```

Si la cuenta `gymadmin` ya existe, ejecutá el bloque sin `CREATE USER` y usá su contraseña actual. En el primer arranque, la aplicación prepara las tablas y carga los planes iniciales.

### 4. Compilá y arrancá GymAdminApp

De vuelta en PowerShell, compilá el proyecto. La primera compilación necesita Internet para descargar las dependencias.

```powershell
mvn clean verify
```

Cuando aparezca `BUILD SUCCESS`, ejecutá el bloque siguiente. Escribí la contraseña del usuario `gymadmin` cuando PowerShell te la pida.

```powershell
$env:GYM_DB_USER = 'gymadmin'
$claveGym = Read-Host 'Contraseña de gymadmin' -AsSecureString
$env:GYM_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveGym).Password
java -jar .\target\gymadmin-1.0.0-SNAPSHOT.jar
```

La consola muestra `GymAdminApp | Gestion de gimnasios`. Cuando veas `Started GymAdminApplication`, abrí <http://127.0.0.1:8080> en el navegador.

Para detenerla, presioná `Ctrl+C`. Para volver a arrancarla en la misma consola, repetí el comando `java -jar`. Si abrís otra ventana, ejecutá el bloque completo de arranque para volver a cargar las credenciales. Al usar la misma base, se conservan los datos.

## Estructura del proyecto

```text
Gestor_GYM/
├── README.md                       # Guía de uso e instalación
├── pom.xml                         # Dependencias y construcción con Maven
├── Doxyfile                        # Configuración de Doxygen
├── .github/workflows/verificar.yml  # Pruebas y documentación en GitHub Actions
├── db/
│   ├── crear-bases.sql             # Crea las bases para la app y las pruebas
│   └── esquema-referencia.sql      # Permite consultar tablas y relaciones
├── docs/                           # Arquitectura, reglas, pruebas y defensa
└── src/
    ├── main/
    │   ├── java/com/gymadmin/
    │   │   ├── GymAdminApplication.java  # Punto de arranque
    │   │   ├── configuracion/             # Componentes de Spring y catálogo inicial
    │   │   ├── controlador/               # Rutas HTTP y formularios
    │   │   ├── servicio/                  # Casos de uso y transacciones
    │   │   ├── repositorio/               # Consultas y escrituras con JDO
    │   │   └── dominio/                   # Entidades y reglas del gimnasio
    │   └── resources/
    │       ├── application.properties    # Configuración de ejecución
    │       ├── META-INF/persistence.xml  # Unidad de persistencia JDO
    │       ├── banner.txt                # Mensaje de arranque en consola
    │       ├── templates/                # Vistas Thymeleaf
    │       └── static/css/gym.css         # Estilos propios
    └── test/                       # Pruebas unitarias y de integración
```

Este es el recorrido al abrir el listado de socios.

1. El navegador envía `GET /socios`. El controlador recibe la solicitud y pide los datos al servicio. [SocioController.java](src/main/java/com/gymadmin/controlador/SocioController.java)
2. El servicio pasa los filtros al repositorio. Este consulta en MySQL los datos de `Socio` mediante JDO. [SocioService.java](src/main/java/com/gymadmin/servicio/SocioService.java) · [SocioRepositorio.java](src/main/java/com/gymadmin/repositorio/SocioRepositorio.java)
3. El controlador pasa la lista al `Model`. Thymeleaf usa esos datos para generar la tabla que se muestra en el navegador. [socios/lista.html](src/main/resources/templates/socios/lista.html)

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

## Configuración

La fábrica de JDO se construye con `META-INF/persistence.xml` y las credenciales de la sesión. Cada operación abre y cierra su propio administrador. Las vistas reciben DTO y copias detached con las relaciones necesarias.

Las variables disponibles son `GYM_DB_URL`, `GYM_DB_USER`, `GYM_DB_PASSWORD` y `GYM_PORT` (8080 por defecto). `GYM_ADDRESS` permite cambiar la dirección de escucha si el entorno de demostración lo requiere. No versionar archivos locales con credenciales ni el directorio `target`.

Si MySQL usa otro puerto o servidor, ajustá la dirección en `GYM_DB_URL` antes de arrancar. Este ejemplo usa el puerto `3307`.

```powershell
$env:GYM_DB_URL = 'jdbc:mysql://localhost:3307/gymadmin?connectionTimeZone=America/Argentina/Buenos_Aires'
```

En Linux/macOS se configuran las mismas variables con `export`. Durante el desarrollo, `mvn process-classes spring-boot:run` ejecuta el enhancer de JDO antes de arrancar. Para la entrega se usa el JAR empaquetado.

## Diagnóstico

- Si falta `GYM_DB_PASSWORD`, configurarla antes de arrancar. Un arranque fallido no confirma operaciones.
- Si hay rechazo de conexión, comprobar el servicio MySQL, puerto, esquema y permisos del usuario.
- Si JDO informa clases sin enhancement, ejecutar `mvn clean verify` y utilizar el JAR recién generado.
- Si el puerto 8080 está ocupado, definir `GYM_PORT` con otro puerto.

Cada integrante debe revisar el código, realizar aportes propios y poder explicar un recorrido completo desde el formulario hasta la base. La documentación organiza la defensa y registra evidencia; la evaluación depende del funcionamiento y la comprensión individual.
