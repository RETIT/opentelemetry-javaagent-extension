# opentelemetry-javaagent-extension

This is an extension for the OpenTelemetry Java agent to provide additional instrumentation for Java applications. It collects 
usage data for the application, such as the total disk demand, total CPU demand, total storage demand and the network demand of the application.
This data is attached to the span of each request and can be viewed in the Jaeger UI.

This data will later be used to calculate the SCI value of each API call.

To run the sample application with the OpenTelemetry Java agent attached, the OpenTelemetry Java agent has to be downloaded from the OpenTelemetry website. The OpenTelemetry Java agent can be downloaded from the following link:
https://opentelemetry.io/docs/zero-code/java/agent/

To run the extension it has to be packaged into a jar file. This can be done by running:
    
```bash
mvn clean package
```

The data (traces and metrics) collected by this extension can be processed by all OpenTelemetry Protocol (OTLP) compatible 
backends. However, in order to process the additional attributes added to the metrics and traces for SCI calculation you 
need to use a compatible backend such as [https://www.retit.io](https://www.retit.io) or a custom setup using the 
OpenTelemetry Collector, Prometheus and Grafana. You can start an example setup for development purposes using the provided
docker/docker-compose.yml file in this repository. file in this repository.
To do this, run:

```bash 
docker compose -f ./docker/docker-compose up
```
The sample Application can then be run with the OpenTelemetry Java agent attached from the root directory as follows.
    
```bash
java -javaagent:./sampleapplication/target/jib/opentelemetry-javaagent-all.jar \
-Dotel.service.name=sampleapplication \
-Dotel.logs.exporter=logging \
-Dotel.javaagent.extensions=./sampleapplication/target/jib/io.retit.opentelemetry.javaagent.extension.jar \
-Dio.retit.emissions.cloud.provider=aws \
-Dio.retit.emissions.cloud.provider.region=af-south-1 \
-Dio.retit.emissions.cloud.provider.instance.type=a1.medium \
-jar ./sampleapplication/target/sampleapplication-0.0.1-SNAPSHOT.jar
```
