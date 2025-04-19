# Proyecto de Microservicios con Observabilidad y Monitorización

Este proyecto implementa una arquitectura de microservicios compuesta por distintos servicios relacionados con
un sistema bancario: `accounts`, `cards`, `loans`, `configserver`, `eurekaserver` y `gatewayserver`. Además,
se ha integrado un completo stack de observabilidad para trazabilidad, métricas y logs utilizando herramientas
como **Grafana**, **Prometheus**, **Tempo**, **Loki**, **Promtail**, **Alloy**, y la instrumentación con *
*Micrometer** y **OpenTelemetry**.

## 🧱 Microservicios Incluidos

| Servicio        | Puerto | Descripción                                |
|-----------------|--------|--------------------------------------------|
| `accounts`      | 8080   | Gestión de cuentas de usuario              |
| `cards`         | 9000   | Gestión de tarjetas                        |
| `loans`         | 8090   | Gestión de préstamos                       |
| `configserver`  | 8071   | Proporciona configuración centralizada     |
| `eurekaserver`  | 8070   | Registro de servicios con Eureka           |
| `gatewayserver` | 8072   | Puerta de enlace que enruta las peticiones |

Todos los servicios están instrumentados con **Micrometer** y **OpenTelemetry** para exponer métricas y
trazas.

---

## 🔍 Observabilidad Integrada

Este proyecto incluye un stack de observabilidad moderno y completamente funcional:

### 📈 Prometheus

- Recoge **métricas** expuestas por los endpoints `/actuator/prometheus` de los microservicios.
- Intervalo de scrape: 5 segundos.
- Accesible en: `http://localhost:9090`.

### 📊 Grafana

- Plataforma de visualización para **métricas, logs y trazas**.
- Se conecta a Prometheus, Loki y Tempo.
- Preconfigurada con datasources y dashboards.
- Accesible en: `http://localhost:3000`.

### ⏱️ Tempo

- Almacena y gestiona **trazas distribuidas**.
- Recepción de datos vía OTLP (HTTP/GRPC) desde los microservicios.
- Accesible en: `http://localhost:3110`.

### 📜 Loki + Promtail + Alloy

- Sistema de **agregación y consulta de logs**.
- **Promtail** ha sido reemplazado por **Alloy**, que recolecta logs desde los contenedores Docker.
- Loki funciona en modo microservicio (`read`, `write`, `backend`) con almacenamiento en MinIO.
- Gateway de logs: `http://localhost:3100`.

---

## 🧰 Tecnologías Usadas

| Herramienta        | Función                                                     |
|--------------------|-------------------------------------------------------------|
| **Spring Boot**    | Framework principal de los microservicios                   |
| **Spring Cloud**   | Configuración, descubrimiento y gateway                     |
| **Docker Compose** | Orquestación de los servicios                               |
| **Prometheus**     | Recolección de métricas                                     |
| **Grafana**        | Visualización de métricas, logs y trazas                    |
| **Tempo**          | Almacenamiento y consulta de trazas                         |
| **Loki**           | Almacenamiento de logs con consulta tipo PromQL             |
| **Alloy**          | Recolector unificado de logs, reemplazo moderno de Promtail |
| **MinIO**          | Almacenamiento S3 compatible usado por Loki                 |
| **Micrometer**     | Exportación de métricas en los microservicios               |
| **OpenTelemetry**  | Instrumentación de trazas distribuidas                      |

---

## 🧪 Verifica que todo funciona

- **Grafana**: [http://localhost:3000](http://localhost:3000)
- **Prometheus**: [http://localhost:9090](http://localhost:9090)
- **Tempo UI** (vía Grafana): Ver trazas en la pestaña "Traces"
- **Logs en Grafana (Loki)**: Desde la pestaña "Explore", selecciona el datasource "Loki"

---

## 🗺️ Arquitectura del Sistema

```
         +------------------------+
         |     Grafana (3000)    |
         +----------+------------+
                    |
  +-----------------+-------------------+
  |     Tempo       |     Loki          |     Prometheus
  |     (4318)      |   (read/write)    |     (9090)
  +--------+--------+--------+----------+
           |                 |
    +------+--+        +-----+----+
    |  Alloy  |        |  MinIO   |
    +---------+        +----------+

Microservicios:
  - accounts (8080)
  - cards (9000)
  - loans (8090)
  - configserver (8071)
  - eurekaserver (8070)
  - gatewayserver (8072)

Todos instrumentados con Micrometer y OpenTelemetry
```

---

## 📂 Estructura del Proyecto

```
├── accounts
├── cards
├── loans
├── configserver
├── eurekaserver
├── gatewayserver
├── observability
│   ├── grafana
│   ├── loki
│   ├── tempo
│   ├── prometheus
│   └── alloy
├── docker-compose.yml
├── common-config.yml
└── README.md
```

---

## 🛠️ Personalización

- Puedes modificar las reglas de scrape en `observability/prometheus/prometheus.yml`.
- Agrega dashboards en `observability/grafana/` o a través de la UI.
- Cambia las reglas de retención o configuración en `tempo.yml`, `loki-config.yml` o `alloy-local-config.yml`.

---
