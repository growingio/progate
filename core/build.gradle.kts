/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.gateway.java-library-conventions")
}

dependencies {

    api(project(":api"))
    api(project(":utilities"))
    api("org.slf4j:slf4j-api")
    api("com.typesafe:config")
    implementation("com.google.dagger:dagger")
    annotationProcessor("com.google.dagger:dagger-compiler")

}
