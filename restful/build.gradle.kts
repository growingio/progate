/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.gateway.java-library-conventions")
}

dependencies {

    implementation(project(":core"))
    implementation("com.google.code.gson:gson")
    // We use implementation instead of api for better compilation performance
    // https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation

}
