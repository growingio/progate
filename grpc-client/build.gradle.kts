/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.progate.java-publishing-conventions")
}

dependencies {

    api(project(":grpc-proto"))
    api("io.grpc:grpc-services")
    implementation(project(":utilities"))
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
