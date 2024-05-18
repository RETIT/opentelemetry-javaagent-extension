# opentelemetry-javaagent-extension

This is an extension for the OpenTelemetry Java agent that provides additional instrumentation for Java applications.

You need to package this extension by running:
    
```bash
mvn clean package
```

The endpoints used in this extension, can be run with the docker-compose file in this repository. This will start Grafana, Prometheus, Jaeger and the Open Telemetry Collector.

To do this, run:

```bash 
docker compose -f .\docker\docker-compose up
```
Then you can run the sample Application with the OpenTelemetry Java agent attached.
    
```bash
java -javaagent:"path_to_otel_java_agent_jar" ^
-Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=logging ^
-Dotel.javaagent.extensions="path_to_jar_of_this_opentelemetry_javaagent_extension" ^
-jar "path_to_jar_of_sampleApplication"
```
