/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `maven-publish`
    id("io.growing.gateway.java-library-conventions")
}

dependencies {

    api(project(":grpc-proto"))
    api("io.grpc:grpc-services")
    implementation(project(":utilities"))
}
