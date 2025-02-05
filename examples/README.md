This directory contains several examples on how to use the OpenTelemetry extension for different application types. You will find details on how to use the examples in the corresponding readme files:

- A simple [JDK8-based application](simple-jdk8-application/README.md)
- A simple [JDK21-based application](simple-jdk21-application/README.md)
- A [Quarkus-based REST service](quarkus-rest-service/README.md)
- A [Spring-based REST service](quarkus-rest-service/README.md)

# Starting OpenTelemetry Backends for the Example Applications 

For all application types, we are publishing the metric and span data to OpenTelemetry compatible backends ([OpenTelemetry Collector](https://github.com/open-telemetry/opentelemetry-collector/tree/main), [Prometheus](https://prometheus.io/), [Grafana](https://grafana.com/) and optionally [Jaeger](https://www.jaegertracing.io/)) as shown in the following architectural overview:

![demo_architecture.png](../img/demo_architecture.png)

These backends can be started using the 
```bash 
docker compose -f ./docker/docker-compose.yml up -d
```

This will start an [OpenTelemetry Collector](https://github.com/open-telemetry/opentelemetry-collector/tree/main) to which the metric and trace data is being sent. Furthermore, it starts [Prometheus](https://prometheus.io/) instance to store the metric data and a [Grafana](https://grafana.com/) instance to visualize the metrics stored in Prometheus. You can optionally also start a [Jaeger](https://www.jaegertracing.io/) instance by commenting out the corresponding section in the docker compose file to visualize the span attributes.

