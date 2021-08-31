server:
  port: {{ services.graphql_gateway.http.port }}

hashids:
  length: 8
  salt: "{{ global.hashids_salt  }}"

upstreams:
  - name: growing-analysis-java
    protocol: grpc
    nodes:
    {% for host in services.analysis_service.hosts %}
      - host: {{ host }}
        port: {{ services.analysis_service.grpc.port }}
        weight: 1
    {% endfor %}


plugins:
  - hash-id

extra-plugins:
  - hash-id

graphql:
  - scalars:
      - graphql.scalars.datetime.DateTimeScalar
