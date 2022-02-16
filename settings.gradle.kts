rootProject.name = "progate"
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
        mavenCentral()
    }
}

include("code-coverage-report")
