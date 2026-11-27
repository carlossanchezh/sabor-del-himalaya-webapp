# Sabor del Himalaya

![License](https://img.shields.io/github/license/carlossanchezh/sabor-del-himalaya-webapp?style=flat)
[![Build](https://img.shields.io/github/actions/workflow/status/carlossanchezh/sabor-del-himalaya-webapp/ci.yml?branch=main&style=flat&logo=githubactions&logoColor=white&label=Build)](https://github.com/carlossanchezh/sabor-del-himalaya-webapp/actions/workflows/ci.yml)
![Version](https://img.shields.io/badge/Version-2.0.0-blue?style=flat)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=flat&logo=apachemaven&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=flat&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat&logo=mysql&logoColor=white)
[![Docker Hub](https://img.shields.io/badge/Docker_Hub-Image-2496ED?style=flat&logo=docker&logoColor=white)](https://hub.docker.com/r/carlossanchezh/sabor-del-himalaya-webapp)

## Descripción

**Sabor del Himalaya** es una aplicación web para la gestión de pedidos de comida a domicilio de un restaurante nepalí.

La plataforma permite a los clientes consultar la carta, configurar sus pedidos y realizar un seguimiento de los mismos. Además, ofrece herramientas específicas para que el chef gestione el menú diario, el personal controle el estado de los pedidos y el dueño administre la carta y consulte la facturación.

## Contenidos

- [Descripción](#descripción)
- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Arquitectura](#arquitectura)
- [Modelo de datos](#modelo-de-datos)
- [Reglas de negocio](#reglas-de-negocio)
- [Tests](#tests)
- [Integración continua](#integración-continua)
- [Logs](#logs)
- [Contribuidores](#contribuidores)
- [Licencia](#licencia)

## Funcionalidades

La aplicación ofrece funcionalidades diferenciadas según el rol del usuario. Cada perfil dispone de una interfaz y permisos específicos para realizar las operaciones correspondientes a sus responsabilidades.

### Roles

| Rol | Funcionalidades                                                                          | Pantalla de inicio |
|---|------------------------------------------------------------------------------------------|---|
| **Cliente** | Autenticación, consulta de la carta, crear, modificar y cancelar pedidos.                | `/carta` |
| **Chef** | Consultar la carta, configurar el menú del día y crear platos fuera de carta.            | `/chef/carta` |
| **Staff** | Consultar los pedidos del día por estado y actualizar su estado.                         | `/staff/pedidos` |
| **Dueño** | Añadir, modificar, retirar y recuperar productos de la carta y consultar la facturación. | `/dueno/panel` |

### Rutas

#### Público

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/auth/login` | Pantalla de inicio de sesión y registro. |
| `POST` | `/auth/registro` | Registrar un nuevo cliente. |
| `GET` | `/auth/activar?token=...` | Activar la cuenta desde el enlace del correo. |
| `POST` | `/auth/reenviar` | Reenviar el correo de activación. |

#### Cliente

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/carta` | Carta por categorías y menú del día. |
| `POST` | `/pedidos/anadir-plato` | Añadir un plato al pedido temporal. |
| `POST` | `/pedidos/anadir-menu` | Añadir el menú del día al pedido temporal. |
| `GET` | `/pedidos/revisar` | Revisar el pedido antes de confirmarlo. |
| `GET` `POST` | `/pedidos/editar-pedido-temporal` | Editar las cantidades del pedido temporal. |
| `POST` | `/pedidos/confirmar` | Confirmar el pedido. |
| `GET` | `/pedidos/mis-pedidos` | Historial y seguimiento de pedidos. |
| `GET` `POST` | `/pedidos/{id}/modificar` | Modificar un pedido confirmado (10 min). |
| `POST` | `/pedidos/{id}/cancelar` | Cancelar un pedido (10 min). |

#### Chef

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/chef/carta` | Carta, estado del menú de hoy y platos fuera de carta. |
| `GET` `POST` | `/chef/menu` | Configurar el menú del día. |
| `POST` | `/chef/platos/nuevo` | Crear un plato fuera de carta. |

#### Staff

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/staff/pedidos` | Pedidos del día por estado. |
| `POST` | `/staff/pedidos/estado` | Cambiar el estado de un pedido. |

#### Dueño

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/dueno/panel` | Carta y facturación. |
| `POST` | `/dueno/platos/nuevo` | Añadir un producto a la carta. |
| `POST` | `/dueno/platos/{id}/editar` | Modificar un producto. |
| `POST` | `/dueno/platos/{id}/eliminar` | Retirar un producto de la carta. |
| `POST` | `/dueno/platos/{id}/anadir-existente` | Volver a añadir un producto retirado. |

## Tecnologías

### Backend y lógica de negocio
- Lenguaje: **Java 21**
- Framework: **Spring Boot 4.1.1**
- Seguridad: **Spring Security**

### Frontend y presentación
- Motor de plantillas: **Thymeleaf**
- Framework CSS: **Bootstrap 5.3.3**
- Visualización de datos: **Chart.js 4.4.1**

### Persistencia de datos
- Acceso a datos: **Spring Data JPA**
- Mapeo Objeto-Relacional (ORM): **Hibernate**
- Base de Datos: **MySQL 8**

### Servicios y validación
- Correo: **Spring Boot Starter Mail**
- Validación: **Jakarta Bean Validation**

### Desarrollo y testing
- Build y dependencias: **Maven**
- Utilidades: **Lombok**
- Tests: **JUnit 5 y Mockito**
- Logging: **SLF4J**

### Infraestructura y automatización
- Contenedorización: **Docker y Docker Compose**
- Integración continua (CI): **GitHub Actions**

## Instalación y ejecución

El proyecto se puede ejecutar en **local** o desplegándolo con **Docker** (no requiere instalar Java ni MySQL).

### Requisitos

**En local**

- **Java 21** o superior
- **Maven 3.9** o superior
- **MySQL 8** o superior
- Una cuenta de correo **SMTP**.

**Con Docker**

- **Docker** (Docker Desktop).
- Una cuenta de correo **SMTP**.

> [!NOTE]
> La aplicación está configurada para Gmail (`smtp.gmail.com`). Para otro proveedor, modifica `application.properties`.

### Ejecución local

#### 1. Clona el repositorio:

```bash
git clone https://github.com/carlossanchezh/sabor-del-himalaya-webapp.git
```

```bash
 cd sabor-del-himalaya-webapp
 ```

#### 2. Crea la base de datos e importa el esquema:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sabor_himalaya CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

```bash
mysql -u root -p sabor_himalaya < database/sabor_himalaya.sql
```

> [!NOTE]
> La base de datos se llama siempre `sabor_himalaya`, nombre fijado en `application.properties`.

#### 3. Define las variables de entorno:

| Variable | Obligatoria | Valor por defecto | Descripción                                           |
|---|---|---|-------------------------------------------------------|
| `DB_USERNAME` | Sí | — | Usuario de MySQL.                                     |
| `DB_PASSWORD` | Sí | — | Contraseña de MySQL.                                  |
| `MAIL_USERNAME` | Sí | — | Cuenta de correo que envía los correos de activación. |
| `MAIL_PASSWORD` | Sí | — | Contraseña de aplicación de la cuenta de correo.      |
| `DB_HOST` | No | `localhost` | Host de MySQL.                                        |
| `DB_PORT` | No | `3306` | Puerto de MySQL.                                      |
| `APP_PORT` | No | `8080` | Puerto en el que se expone la aplicación.             |
| `APP_BASE_URL` | No | `http://localhost:8080` | URL base de los enlaces de activación del correo.     |
| `LOG_FILE` | No | `logs/sabor-del-himalaya.log` | Ruta del fichero de log.                              |

> [!TIP]
> Para probar sin correo, usa cualquier valor en `MAIL_USERNAME` y `MAIL_PASSWORD`, el correo no se enviará.

#### 4. Arranca la aplicación:

```bash
mvn spring-boot:run
```

> [!TIP]
> Puedes ejecutar `SaborDelHimalayaApplication` desde tu IDE, definiendo las variables de entorno en su configuración.

#### 5. Uso de la aplicación:

Abre `http://localhost:8080` y navega por el sistema.

### Ejecución con Docker

#### 1. Clona el repositorio:

```bash
git clone https://github.com/carlossanchezh/sabor-del-himalaya-webapp.git
```
```bash
cd sabor-del-himalaya-webapp
```

#### 2. Crea un fichero `.env` en la raíz del proyecto:

```env
# ---------- Base de datos ----------

# Usuario de MySQL (en Docker debe ser root)
DB_USERNAME=root

# Contraseña de MySQL
DB_PASSWORD=cambia_esta_contraseña

# Base de datos que crea MySQL (fijado en `application.properties`)
DB_NAME=sabor_himalaya

# Puerto del equipo con el que se expone MySQL
DB_PORT_HOST=3306

# ---------- Aplicación ----------

# Puerto del equipo con el que se expone la aplicación
APP_PORT=8080

# URL base de los enlaces de activación del correo
APP_BASE_URL=http://localhost:8080

# ---------- Correo ----------

# Cuenta de correo que envía los correos de activación
MAIL_USERNAME=tu_cuenta@mail.com

# Contraseña de aplicación de la cuenta de correo
MAIL_PASSWORD=tu_contraseña_de_aplicacion
```

> [!CAUTION]
> El fichero `.env` contiene credenciales, no lo compartas.

#### 3. Levanta los contenedores:

```bash
docker compose up -d
```

Docker Compose realiza los siguientes pasos:
- Descarga la imagen de la aplicación desde Docker Hub o, si no está disponible, la construye a partir del `Dockerfile`.
- Arranca MySQL e importa automáticamente `database/sabor_himalaya.sql`.
- Espera a que la base de datos esté lista y arranca la aplicación.

> [!NOTE]
> Los datos se conservan entre despliegues en el volumen `db_data`.

> [!TIP]
> Usa `docker compose up -d --build` para construir la imagen a partir del `Dockerfile` en lugar de descargarla.

#### 4. Uso de la aplicación:

Abre `http://localhost:8080` y navega por el sistema.

#### 5. Detén los contenedores:

```bash
docker compose down
```

> [!TIP]
> Para detener los contenedores y borrar también el volumen, usa `docker compose down -v`.

### Despliegue en un dominio propio

Por defecto, la aplicación queda disponible en `http://localhost:8080`. Para ejecutarla en otro puerto o en tu propio dominio, modifica estas variables (en el `.env` con Docker o como variables de entorno en local):

| Variable | Qué controla |
|---|---|
| `APP_PORT` | Puerto en el que se expone la aplicación. |
| `APP_BASE_URL` | dirección pública de la web, usada en los enlaces de activación del correo. |

> [!IMPORTANT]
> El dominio debe apuntar al servidor y, para usar HTTPS, la aplicación debe ir detrás de un proxy inverso (Nginx, Caddy…).

Ejemplos:

```env
# Otro puerto
APP_PORT=9090
APP_BASE_URL=http://localhost:9090
```

```env
# Dominio propio
APP_PORT=8080
APP_BASE_URL=https://pedidos.midominio.com
```

### Usuarios del personal

El registro desde la web crea siempre usuarios con rol `CLIENTE`. Por eso, las cuentas de **chef**, **dueño** y **staff** se crean de forma manual: se registran como cualquier cliente y después se les cambia el rol en la base de datos.

#### 1. Regístrate desde `/auth/login` con tres usuarios, por ejemplo (el correo debe tener un formato válido, aunque no exista):

| Rol | Login | Correo |
|---|---|---|
| Chef | `chef` | `chef@example.com` |
| Dueño | `dueno` | `dueno@example.com` |
| Staff | `staff` | `staff@example.com` |

> [!NOTE]
> Los clientes se activan con el enlace del correo; sin correo válido configurado hay que activarlos a mano en la base de datos.

#### 2. Abre una consola de MySQL:

```bash
# Con Docker
docker exec -it sabor-del-himalaya-db mysql -u root -p sabor_himalaya
```

```bash
# En local
mysql -u root -p sabor_himalaya
```

#### 3. Asigna el rol y activa cada cuenta:

```sql
UPDATE usuario SET rol = 'CHEF', estado_activacion = 1, token = NULL WHERE login = 'chef';
UPDATE usuario SET rol = 'DUENO', estado_activacion = 1, token = NULL WHERE login = 'dueno';
UPDATE usuario SET rol = 'STAFF', estado_activacion = 1, token = NULL WHERE login = 'staff';
```

#### 4. Inicia sesión con cada usuario.

Cada uno es redirigido a su pantalla de inicio: `/chef/carta`, `/dueno/panel` y `/staff/pedidos`.

## Arquitectura

La aplicación sigue una **arquitectura monolítica por capas**, que separa las responsabilidades y facilita el mantenimiento y la evolución del sistema.

La estructura principal se organiza en las siguientes capas:

| Capa | Paquete | Responsabilidad                                                             |
|---|---------|-----------------------------------------------------------------------------|
| Controller | `controller` | Gestiona las peticiones HTTP y coordina la interacción con los servicios.   |
| Service | `service` | Implementa la lógica de negocio y las reglas de la aplicación.              |
| Repository | `repository` | Gestiona el acceso y la persistencia de los datos mediante Spring Data JPA. |
| Model | `model` | Define las entidades y estructuras principales del dominio.                 |

Además, el proyecto incluye paquetes específicos para advice (`advice`), DTOs (`dto`), configuración (`config`), enumeraciones (`enums`) y excepciones (`exception`).

### Componentes del sistema

El sistema se compone de dos componentes que se ejecutan de forma independiente: la **aplicación web** y la **base de datos**.

| Componente | Tecnología | Puerto | Descripción |
|---|---|---|---|
| Aplicación web | Spring Boot (Java 21) con Tomcat embebido | `8080` | Recibe las peticiones HTTP, aplica la lógica de negocio y genera las páginas HTML en el servidor con Thymeleaf. |
| Base de datos | MySQL 8 | `3306` | Almacena de forma persistente usuarios, carta, menús y pedidos en la base de datos `sabor_himalaya`. |

La aplicación se comunica con MySQL mediante **JDBC**, con Spring Data JPA e Hibernate por encima. Además, envía los correos de activación de cuenta a través del servidor SMTP, un servicio externo.

### Decisiones de diseño

Durante el desarrollo se han adoptado distintas decisiones para garantizar la integridad de los datos, mantener el historial de operaciones y mejorar la experiencia de uso.

- **Pedido temporal en sesión**: El pedido se guarda en `HttpSession` y solo se persiste al confirmarlo.
- **Precio congelado**: Cada línea de pedido guarda el precio del momento de la compra.
- **Cancelación y retirada lógicas**: Pedidos y platos no se borran, se conserva el historial.
- **Correo asíncrono**: El correo de activación se envía con `@Async` para no bloquear el registro.
- **Revalidación en el servidor**: Plazos, cantidades e importes se comprueban de nuevo al guardar.

## Reglas de negocio

Las reglas de negocio definen las restricciones y condiciones que deben cumplirse durante el funcionamiento de la aplicación. Se agrupan según las principales áreas del dominio.

### Usuarios

| Regla | Descripción |
|---|---|
| Correo y login únicos | No puede haber dos usuarios con el mismo correo ni el mismo login. |
| Formato del login | Entre 4 y 50 caracteres y sin el símbolo `@`. |
| Contraseña | Entre 6 y 100 caracteres, almacenada cifrada con BCrypt. |
| Rol por defecto | Todo registro nuevo es `CLIENTE`. |
| Activación de cuenta | La cuenta se crea inactiva y no permite iniciar sesión hasta abrir el enlace de activación enviado por correo. |

### Menú del día

| Regla | Descripción |
|---|---|
| Precio fijo | El menú cuesta **15,00 €** e incluye 1 entrante, 1 principal, 1 postre y 1 bebida de la carta. |
| Composición | El chef elige **3 entrantes, 3 principales y 2 postres** distintos. |
| Un menú por día | Solo hay un menú por día y no se puede reconfigurar. |
| Fecha | No se puede configurar el menú de días pasados. |
| Pedido | Solo se puede pedir el menú del día de hoy. |

### Carta

| Regla | Descripción |
|---|---|
| Platos del chef | Se crean fuera de carta, con precio 0 y sin bebidas, y nunca pueden entrar en la carta. |
| Nombres únicos | Los nombres no se pueden repetir entre platos de la carta y retirados. |
| Retirada lógica | Un plato retirado no se borra: deja de ser visible y se puede volver a añadir. |

### Pedidos

| Regla | Descripción |
|---|---|
| Disponibilidad | Solo se pueden pedir platos que estén actualmente en la carta. |
| Dirección de entrega | Obligatoria, de máximo 255 caracteres. Se propone la del perfil. |
| Plazo de cambios | Se pueden **modificar o cancelar durante los 10 minutos** posteriores a su realización, mientras no estén cancelados. |
| Estados | `RECIBIDO` → `EN_PREPARACION` → `EN_REPARTO` → `ENTREGADO`, o `CANCELADO`. |

### Facturación

| Regla | Descripción |
|---|---|
| Cálculo diario | La facturación de un día suma el importe de los pedidos `ENTREGADO` de ese día. |

## Modelo de datos

La aplicación utiliza una base de datos relacional MySQL, estructurada en siete tablas que representan las principales entidades del sistema y sus relaciones.

### Entidades principales

| Entidad | Tabla | Descripción |
|---|---|---|
| `Usuario` | `usuario` | Usuario del sistema con sus datos personales, credenciales, rol y estado de activación. |
| `Plato` | `plato` | Producto del restaurante con nombre, descripción, precio y categoría. Indica si está en la carta y si es un plato creado por el chef. |
| `MenuDiario` | `menu_diario` | Menú del día, con su fecha (única) y su precio. |
| `MenuDiarioOpcion` | `menu_diario_opciones` | Plato ofrecido como opción de un menú del día, con su categoría (entrante, principal o postre). |
| `Pedido` | `pedido` | Pedido de un usuario con dirección de entrega, fecha y hora, importe total y estado. |
| `LineaPedidoPlato` | `linea_pedido_plato` | Línea de un pedido correspondiente a un plato de la carta, con su cantidad y el precio unitario en el momento de la compra. |
| `LineaPedidoMenuDiario` | `linea_pedido_menudiario` | Línea de un pedido correspondiente a un menú del día, con el entrante, principal, postre y bebida elegidos, su cantidad y el precio unitario. |

### Diagrama entidad-relación

```text
┌─────────────────────────────┐         ┌────────────────────────────┐         ┌──────────────────────────────────┐
│           usuario           │         │           pedido           │         │        linea_pedido_plato        │
├─────────────────────────────┤         ├────────────────────────────┤         ├──────────────────────────────────┤
│ id_usuario (PK)             │ 1     N │ id_pedido (PK)             │ 1     N │ id_linea_pedido_plato (PK)       │
│ nombre                      │─────────│ id_usuario (FK)            │─────────│ id_pedido (FK)                   │
│ direccion                   │         │ direccion_entrega          │         │ id_plato (FK)                    │
│ correo (UNIQUE)             │         │ fecha_hora                 │         │ cantidad                         │
│ login (UNIQUE)              │         │ importe_total              │         │ precio_unitario                  │
│ contrasena                  │         │ estado (ENUM)              │         └──────────────────────────────────┘
│ estado_activacion           │         └────────────────────────────┘                             │ N
│ rol (ENUM)                  │                        │ 1                                         │
│ token                       │                        │                                           │
└─────────────────────────────┘                        │                                           │
                                                       │                                           │
                                                       │ N                                         │ 1
                                       ┌──────────────────────────────────┐         ┌──────────────────────────────┐
                                       │     linea_pedido_menudiario      │         │            plato             │
                                       ├──────────────────────────────────┤         ├──────────────────────────────┤
                                       │ id_linea_pedido_menu_diario (PK) │ N     1 │ id_plato (PK)                │
                                       │ id_pedido (FK)                   │─────────│ nombre                       │
                                       │ id_menu_diario (FK)              │         │ descripcion                  │
                                       │ id_entrante (FK)                 │         │ precio                       │
                                       │ id_principal (FK)                │         │ categoria (ENUM)             │
                                       │ id_postre (FK)                   │         │ esta_en_carta                │
                                       │ id_bebida (FK)                   │         │ es_del_chef                  │
                                       │ cantidad                         │         └──────────────────────────────┘
                                       │ precio_unitario                  │                        │ 1
                                       └──────────────────────────────────┘                        │
                                                        │ N                                        │
                                                        │                                          │
                                                        │                                          │
                                                        │                                          │
                                                        │ 1                                        │ N
                                       ┌──────────────────────────────────┐         ┌──────────────────────────────┐
                                       │           menu_diario            │         │     menu_diario_opciones     │
                                       ├──────────────────────────────────┤         ├──────────────────────────────┤
                                       │ id_menu_diario (PK)              │ 1     N │ id_menu_diario_opciones (PK) │
                                       │ fecha (UNIQUE)                   │─────────│ id_menu_diario (FK)          │
                                       │ precio                           │         │ id_plato (FK)                │
                                       └──────────────────────────────────┘         │ categoria (ENUM)             │
                                                                                    └──────────────────────────────┘
```

## Tests

El proyecto incluye **tests unitarios** desarrollados con JUnit 5 y Mockito para verificar el correcto funcionamiento de la lógica de negocio y sus distintos componentes.

| Clase de test | Cobertura                                                       |
|---|-----------------------------------------------------------------|
| `UsuarioServiceImplTest` | Registro de usuarios, activación de cuentas e inicio de sesión. |
| `UsuarioDetailsServiceImplTest` | Carga de usuarios para autenticación y asignación de roles.     |
| `EmailServiceImplTest` | Envío de correos electrónicos de activación.                    |
| `PlatoServiceImplTest` | Consulta y gestión de la carta, creación de platos por el chef. |
| `MenuDiarioServiceImplTest` | Consulta y configuración del menú diario.                       |
| `PedidoServiceImplTest` | Gestión de pedidos y cálculo de la facturación.                 |
| `PedidoTemporalDTOTest` | Gestión del pedido temporal en sesión (carrito).                |

Para ejecutarlos:

```bash
mvn test
```

> [!NOTE]
> `SaborDelHimalayaApplicationTests` y `EmailTest` están deshabilitados con `@Disabled` porque necesitan un entorno real.

## Integración continua

El proyecto incorpora un pipeline de integración continua mediante GitHub Actions, que automatiza la compilación y ejecución de las pruebas para detectar errores en los cambios introducidos.

El proceso realiza las siguientes tareas:

- Obtención del código fuente.

- Configuración de **JDK 21 (Temurin)** con caché de dependencias Maven.

- Ejecución de `mvn -B clean verify` para compilar el proyecto y ejecutar los tests.

## Logs

La aplicación utiliza **SLF4J** para registrar en `logs/sabor-del-himalaya.log` la actividad relevante, incluyendo:

- Registros de usuarios.

- Inicios y cierres de sesión.

- Pedidos confirmados, modificados y cancelados.

- Cambios de estado de los pedidos.

- Gestión de la carta.

> [!NOTE]
> Con **Docker**, se utiliza un bind mount que conecta la carpeta `./logs` del proyecto con la del contenedor.

## Contribuidores

<br>
<br>

<p align="center">
  <a href="https://github.com/carlossanchezh/sabor-del-himalaya-webapp/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=carlossanchezh/sabor-del-himalaya-webapp" alt="Contribuidores del proyecto"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/carlossanchezh">@carlossanchezh</a> ·
  <a href="https://github.com/aasierr">@aasierr</a> ·
  <a href="https://github.com/just585">@just585</a> ·
  <a href="https://github.com/DaniCaste10">@DaniCaste10</a> ·
  <a href="https://github.com/FrancoSosaMartinez">@FrancoSosaMartinez</a> ·
  <a href="https://github.com/dawesito">@dawesito</a>
</p>

## Licencia

Distribuido bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE.md) para más información.