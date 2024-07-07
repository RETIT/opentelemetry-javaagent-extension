# opentelemetry-javaagent-extension

This is an extension for the OpenTelemetry Java agent to provide additional instrumentation for Java applications. It collects 
usage data for the application, such as the total disk demand, total CPU demand, total storage demand and the network demand of the application.
This data is attached to the span of each request and can be viewed in the Jaeger UI.

This data will later be used to find the SCI value of each API call. 
## Usage

To run the sample application with the OpenTelemetry Java agent attached, the OpenTelemetry Java agent has to be downloaded from the OpenTelemetry website. The OpenTelemetry Java agent can be downloaded from the following link:
https://opentelemetry.io/docs/zero-code/java/agent/

To run the extension it has to be packaged into a jar file. This can be done by running:
    
```bash
mvn clean package
```

The endpoints used in this extension, can be run with the docker-compose file in this repository. This will start Grafana, Prometheus, Jaeger and the Open Telemetry Collector.

To do this, run:

```bash 
docker compose -f .\docker\docker-compose up
```
The sample Application can then be run with the OpenTelemetry Java agent attached.
    
```bash
java -javaagent:"path_to_otel_java_agent_jar" ^
-Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=logging ^
-Dotel.javaagent.extensions="path_to_jar_of_this_opentelemetry_javaagent_extension" ^
-jar "path_to_jar_of_sampleApplication"
```

In order to collect the custom usage data, the SampleApplication ahas to be run with the following command:

```bash
java -javaagent:"path_to_otel_java_agent_jar" ^
-Dotel.javaagent.extensions="path_to_jar_of_this_opentelemetry_javaagent_extension" ^
-Dotel.javaagent.debug=true ^
-Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=logging ^
-Dde.retit.apm.log.total.disk.demand=true ^
-Dde.retit.apm.log.network.demand=true ^
-jar "path_to_jar_of_sampleApplication"
```
