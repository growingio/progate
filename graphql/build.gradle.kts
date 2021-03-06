/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.progate.java-publishing-conventions")
}

dependencies {

    api(project(":core"))
    api("com.graphql-java:graphql-java")
    api("com.graphql-java:graphql-java-extended-scalars")

    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java-util")

    // We use implementation instead of api for better compilation performance
    // https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation

    implementation("com.fasterxml.jackson.core:jackson-databind")

}
