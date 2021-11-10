plugins {
    java
    jacoco
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://nexus.growingio.cn/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {

    constraints {
        implementation("org.slf4j:slf4j-api:1.7.31")
        implementation("com.typesafe:config:1.4.1")
        implementation("com.github.os72:protoc-jar:3.11.4")
        implementation("org.apache.commons:commons-text:1.9")
//        implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
        implementation("com.github.ben-manes.caffeine:caffeine:2.9.0")
        implementation("io.vertx:vertx-web:${ComponentVersions.vertx}")
        implementation("io.vertx:vertx-core:${ComponentVersions.vertx}")
        implementation("io.vertx:vertx-web-client:${ComponentVersions.vertx}")
        //implementation("io.vertx:vertx-auth-oauth2:${ComponentVersions.vertx}")
        implementation("org.apache.logging.log4j:log4j-api:${ComponentVersions.log4j}")
        implementation("org.apache.logging.log4j:log4j-core:${ComponentVersions.log4j}")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:${ComponentVersions.log4j}")

        implementation("com.google.code.gson:gson:2.8.7")
        implementation("io.grpc:grpc-stub:${ComponentVersions.grpc}")
        implementation("io.grpc:grpc-protobuf:${ComponentVersions.grpc}")
        implementation("io.grpc:grpc-services:${ComponentVersions.grpc}")
        implementation("com.google.protobuf:protobuf-java-util:3.17.3")
        implementation("io.grpc:grpc-netty-shaded:${ComponentVersions.grpc}")

        implementation("com.google.inject:guice:${ComponentVersions.guice}")

        implementation("com.jayway.jsonpath:json-path:2.6.0")
        implementation("org.yaml:snakeyaml:1.28")

        implementation("com.google.guava:guava:30.1.1-jre")
        implementation("org.apache.commons:commons-lang3:3.12.0")

        implementation("com.graphql-java:graphql-java:17.0")
        implementation("com.graphql-java:graphql-java-extended-scalars:17.0")

    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}