# Lectura de Propiedades y Perfiles

## 1. Introducción

Spring Boot proporciona varias formas de leer las propiedades definidas en los archivos de configuración, como
`application.yml` o `application.properties`. En este manual, explicaremos dos grandes bloques:

1. **Lectura de Propiedades**: Métodos para acceder a las propiedades en Spring Boot.
2. **Perfiles en Spring Boot**: Uso de perfiles para gestionar configuraciones en distintos entornos.
3. **Opciones para establecer valores en propiedades**: Formas en las que se pueden definir propiedades en una
   aplicación Spring Boot.

---

## 2. Lectura de Propiedades en Spring Boot

### 2.1. Uso de `@EnableConfigurationProperties`

Spring Boot permite enlazar propiedades de configuración a una clase Java usando la anotación
`@ConfigurationProperties` junto con `@EnableConfigurationProperties`.

Los ejemplos en esta sección se encuentran en la clase `AccountController` dentro del paquete
`com.dfragar.accounts.controller`, y la configuración de `@EnableConfigurationProperties` se encuentra en
`AccountsApplication` dentro de `com.dfragar.accounts`.

### **Ejemplo:**

Definimos una clase DTO para mapear las propiedades bajo `accounts`.

```java
package com.dfragar.accounts.dto;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "accounts")
public record AccountsContactInfoDto(
        String message,
        Map<String, String> contactDetails,
        List<String> onCallSupport
) {

}
```

En `AccountsApplication`, habilitamos la configuración:

```java
package com.dfragar.accounts;

import com.dfragar.accounts.dto.AccountsContactInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableConfigurationProperties(value = {AccountsContactInfoDto.class})
public class AccountsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }

}
```

Ahora, podemos inyectar esta clase en `AccountController`:

```java

@Autowired
private AccountsContactInfoDto accountsContactInfoDto;
```

---

### 2.2. Uso de `@Value`

Otra manera de leer propiedades es usando la anotación `@Value`, que permite inyectar valores directamente en
campos de clase.

En `AccountController`, se usa `@Value` para obtener la versión de la aplicación:

```java

@Value("${build.version}")
private String buildVersion;

@GetMapping("/build-info")
public ResponseEntity<String> getBuildVersion() {
    return ResponseEntity.status(HttpStatus.OK).body(buildVersion);
}
```

---

### 2.3. Uso de `Environment`

El objeto `Environment` de Spring nos permite acceder a las propiedades mediante código.

En `AccountController`, se usa para obtener la variable de entorno `JAVA_HOME`:

```java

@Autowired
private Environment environment;

@GetMapping("/java-version")
public ResponseEntity<String> getJavaVersion() {
    return ResponseEntity.status(HttpStatus.OK).body(environment.getProperty("JAVA_HOME"));
}
```

---

## 3. Perfiles en Spring Boot

Spring Boot permite definir diferentes configuraciones para distintos entornos usando perfiles.

### **Ejemplo de configuración de perfiles:**

En el archivo `application.yml`, se pueden definir archivos de configuración específicos para cada entorno y
establecer el perfil activo:

```yaml
spring:
  config:
    import:
      - "application_qa.yml"
      - "application_prod.yml"
  profiles:
    active: "qa"
```

Esto indica que el perfil activo es `qa`, por lo que se utilizará el archivo `application_qa.yml`.

### **Ejemplo de `application_qa.yml`**

```yaml
accounts:
  message: "Welcome to QA environment"
```

### **Ejemplo de `application_prod.yml`**

```yaml
accounts:
  message: "Welcome to Production environment"
```

Si queremos cambiar el perfil en tiempo de ejecución, podemos hacerlo mediante argumentos de línea de
comandos:

```shell
java -jar myapp.jar --spring.profiles.active=prod
```

O estableciendo la variable de entorno:

```shell
export SPRING_PROFILES_ACTIVE=prod
```

---

## 4. Opciones para establecer valores en propiedades

En Spring Boot, podemos definir valores en las propiedades de distintas maneras:

1. **Archivos de configuración (`application.properties` o `application.yml`)**:
    - Ejemplo en `application.yml`:

      ```yaml
      my.property: "Valor desde YAML"
      ```

2. **Argumentos de línea de comandos**:
    - Se pueden pasar propiedades al ejecutar la aplicación:

      ```shell
      java -jar myapp.jar --my.property="Valor desde CLI"
      ```

3. **Variables de entorno**:
    - Se pueden definir en el sistema operativo:

      ```shell
      export MY_PROPERTY="Valor desde Variable de Entorno"
      ```
    - En Windows:

      ```shell
      set MY_PROPERTY=Valor desde Variable de Entorno
      ```

4. **Propiedades de JVM**:
    - Se pueden pasar al iniciar la JVM:

      ```shell
      java -Dmy.property="Valor desde JVM" -jar myapp.jar
      ```

5. **Valores por defecto en el código**:
    - Usando `@Value` con un valor por defecto:

      ```java
      @Value("${my.property:Valor por defecto}")
      private String myProperty;
      ```

Cada una de estas opciones tiene su prioridad. La precedencia de valores es:

1. Argumentos de línea de comandos
2. Variables de entorno
3. Propiedades de JVM
4. Archivos de configuración (`application.properties` o `application.yml`)
5. Valores por defecto en el código

---

## 5. Conclusión

Hemos visto cómo leer propiedades en Spring Boot mediante `@EnableConfigurationProperties`, `@Value` y
`Environment`. También explicamos cómo funcionan los perfiles para gestionar configuraciones en distintos
entornos y las diversas maneras de establecer valores en propiedades. Usar el método adecuado depende de la
necesidad del proyecto, pero `@EnableConfigurationProperties` es generalmente la opción más recomendada para
configuraciones estructuradas.