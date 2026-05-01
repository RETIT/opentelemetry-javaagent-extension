# quarkus-rest-service-library

A minimal Quarkus REST service that demonstrates how to use the **RETIT OpenTelemetry
instrumentation library** without a Java agent.

## Why use the library instead of the agent?

| | Java-agent approach | Library (this example) |
|---|---|---|
| OTel SDK lifecycle | Managed by the agent | Managed by `quarkus-opentelemetry` |
| Startup | `-javaagent:…` JVM flag required | No JVM flags needed |
| Quarkus native | Not supported | Supported |
| Spring Boot, Quarkus, WildFly | Works | Works (via CDI auto-discovery) |

## How it works

1. Add the library as a compile dependency — **no additional configuration class required**.
2. The library ships a `META-INF/beans.xml` (discovery-mode `annotated`).
3. Quarkus (or any CDI 3.0+ container) auto-discovers
   `io.retit.opentelemetry.javaagent.extension.cdi.RETITSpanProcessorConfiguration`
   from the library JAR at startup.
4. That CDI producer registers `RETITSpanProcessor` into the `quarkus-opentelemetry` tracing
   pipeline — identical behaviour to the Java-agent extension.

## Quick start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.retit</groupId>
    <artifactId>opentelemetry-java-agent-extension-cdi-library</artifactId>
    <version><!-- latest release tag --></version>
</dependency>
```

`quarkus-opentelemetry` must also be on the classpath (usually already present in a Quarkus app):

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry</artifactId>
</dependency>
```

### 2. Configure via environment variables (or system properties)

| Environment variable | Default | Description |
|---|---|---|
| `IO_RETIT_EMISSIONS_CLOUD_PROVIDER` | – | Cloud provider (`AWS`, `AZURE`, `GCP`) |
| `IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION` | – | Region (e.g. `eu-central-1`) |
| `IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE` | – | Instance type (e.g. `m5.large`) |
| `IO_RETIT_LOG_CPU_DEMAND` | `true` | Record CPU demand as span attribute |
| `IO_RETIT_LOG_HEAP_DEMAND` | `true` | Record heap allocation as span attribute |
| `IO_RETIT_LOG_DISK_DEMAND` | `false` | Record disk I/O as span attribute |
| `IO_RETIT_LOG_NETWORK_DEMAND` | `false` | Record network I/O as span attribute |
| `IO_RETIT_LOG_GC_EVENT` | `true` | Track GC events |

### 3. Configure `application.properties`

```properties
quarkus.otel.exporter.otlp.endpoint=http://otelcol:4317
quarkus.otel.metrics.enabled=true

io.retit.emissions.cloud.provider=AWS
io.retit.emissions.cloud.provider.region=eu-central-1
io.retit.emissions.cloud.provider.instance.type=m5.large
```

### 4. That's it!

No `-javaagent` flag, no custom CDI configuration class in your application.
Every span automatically carries resource-demand attributes and the top-level
transaction spans are published as OpenTelemetry metrics for SCI/carbon calculations.

## Building and running

```bash
# Build the uber-jar and Docker image
mvn package

# Run locally (without container)
java -jar target/quarkus-rest-service-library.jar
```

## Testing

The integration test `io.retit.opentelemetry.javaagent.extension.frameworks.quarkus.QuarkusWithInternalOtelSupportAndCDILibraryIT` (in the `extension` module) uses a Container image of this service and verifies that:

- The three REST endpoints (`/test-rest-endpoint/getData`, `/postData`, `/deleteData`) return
  HTTP 200.
- Spans emitted to the console exporter contain the expected RETIT resource-demand attributes.
- Metrics (CPU, heap, disk, network demand vectors) are published for root-level transactions.

```bash
# From the project root – runs unit and integration tests for the extension module
mvn verify -pl extension -Dtest=QuarkusWithInternalOtelSupportAndCDILibraryIT
```

