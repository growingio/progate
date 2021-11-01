/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("io.growing.progate.java-publishing-conventions")
}

dependencies {
    api(project(":api"))
    api(project(":core"))
    api(project(":grpc"))
    api(project(":restful"))
    api(project(":graphql"))
    api(project(":utilities"))
    implementation("org.slf4j:slf4j-api")
    implementation("com.typesafe:config")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-web-client")

    implementation("org.apache.commons:commons-text")
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")

    implementation("com.google.inject:guice")

}
