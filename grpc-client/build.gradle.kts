/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.gateway.java-publishing-conventions")
}

dependencies {

    api(project(":grpc-proto"))
    api("io.grpc:grpc-services")
    implementation(project(":utilities"))
}
