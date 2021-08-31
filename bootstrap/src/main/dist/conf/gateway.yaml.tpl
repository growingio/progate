upstreams:
  - name: metadata
    protocol: grpc
    nodes:
      - host: localhost
        port: 18080
        weight: 1


plugins:
  - hash-id

extra-plugins:
  - hash-id

graphql:
  - scalars:
      - graphql.scalars.datetime.DateTimeScalar
