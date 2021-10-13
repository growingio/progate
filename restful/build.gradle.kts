/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.gateway.java-library-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":grpc"))
    implementation("com.google.code.gson:gson")
    implementation("com.jayway.jsonpath:json-path")
    implementation("org.yaml:snakeyaml")
    implementation("io.swagger.core.v3:swagger-models:2.1.7")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java-util")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.protobuf:protobuf-java-util:3.18.0")


    api("io.growing.gateway.plugin:gateway-plugin:1.0.0-SNAPSHOT") {
        isChanging = true
    }
    implementation("com.google.dagger:dagger")
    annotationProcessor("com.google.dagger:dagger-compiler")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")

}
