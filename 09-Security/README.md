# Proyecto de Integración Keycloak + Gateway Server

Este proyecto integra **Keycloak** como proveedor de autenticación/autorización para un conjunto de
microservicios gestionados a través de un **Gateway Server**. Se implementan dos flujos de autorización de
OAuth2:

- **Client Credentials Grant Type Flow** (para clientes máquina a máquina)
- **Authorization Code Grant Type Flow** (para usuarios humanos).

---

## 1. Levantar Keycloak en Docker

1. Ir a la guía
   oficial: [Keycloak Getting Started - Docker](https://www.keycloak.org/getting-started/getting-started-docker).
2. Copiar el comando de inicio, pero modificarlo:
    - Añadir `-d` para ejecutarlo en modo detached (en segundo plano).
    - Cambiar el puerto de mapeo a `7080`.

Comando final:

```bash
docker run -d -p 7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.0 start-dev
```

---

## 2. Configuración del Microservicio `gatewayserver`

### 2.1. Agregar dependencias en el `pom.xml`

Añadir las siguientes dependencias:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

### 2.2. Crear clases de configuración

#### `KeycloakRoleConverter.java`

```java
// Código de KeycloakRoleConverter.java
```

#### `SecurityConfig.java`

```java
// Código de SecurityConfig.java
```

### 2.3. Configurar `application.yml` en `gatewayserver`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: "http://localhost:7080/realms/master/protocol/openid-connect/certs"
```

---

## 3. Configurar Keycloak

Accede a [http://localhost:7080/](http://localhost:7080/), con:

- **Usuario:** `admin`
- **Contraseña:** `admin`

---

## 4. Deep Dive: **Client Credentials Grant Type Flow**

1. **Crear Cliente para Cliente-Servidor:**
2. **Asignar Roles:**
3. **Obtener token:**

---

## 5. Deep Dive: **Authorization Code Grant Type Flow**

1. **Crear Cliente para UI Aplicación:**
2. **Crear Usuario:**
3. **Asignar Roles al Usuario:**

---

# Notas Finales

- **Client Credentials Grant Flow:** utilizado para autenticar sistemas entre sí (sin interacción de usuario).
- **Authorization Code Grant Flow:** utilizado para autenticar usuarios reales y proteger los accesos a
  recursos del sistema.
