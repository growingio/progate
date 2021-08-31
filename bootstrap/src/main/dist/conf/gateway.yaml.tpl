server:
  port: {{ services.graphql_gateway.http.port }}

hashids:
  length: 8
  salt: "{{ global.hashids_salt  }}"

upstreams:
  - name: growing-analysis-java
    protocol: grpc
    nodes:
    {% for host in services.growing_analysis_java.hosts %}
      - host: {{ host }}
        port: {{ services.growing_analysis_java.grpc.port }}
        weight: 1
    {% endfor %}


plugins:
  - hash-id

extra-plugins:
  - hash-id

graphql:
  path: /v3/data-centers/:dataCenterUid/graphql
