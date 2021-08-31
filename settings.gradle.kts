rootProject.name = "graphql-gateway"
include(
    "bootstrap",
    "api",
    "core",
    "graphql",
    "restful",
    "utilities",
    "compiler",
    "grpc",
    "grpc-proto",
    "grpc-client",
    "grpc-libs",
    "examples:grpc-example"
)

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.growingio.cn/repository/maven-public/")
        }
    }
}
