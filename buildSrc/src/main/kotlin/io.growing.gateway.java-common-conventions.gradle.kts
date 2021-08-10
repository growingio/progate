plugins {
    java
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
        implementation("io.vertx:vertx-web:4.1.1")
        implementation("org.slf4j:slf4j-api:1.7.31")
        implementation("com.typesafe:config:1.4.1")
        implementation("com.github.os72:protoc-jar:3.11.4")
        implementation("org.apache.commons:commons-text:1.9")
        implementation("org.apache.logging.log4j:log4j-api:${ComponentVersions.log4j}")
        implementation("org.apache.logging.log4j:log4j-core:${ComponentVersions.log4j}")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:${ComponentVersions.log4j}")

        implementation("com.google.code.gson:gson:2.8.7")
        implementation("io.grpc:grpc-stub:${ComponentVersions.grpc}")
        implementation("io.grpc:grpc-protobuf:${ComponentVersions.grpc}")
        implementation("io.grpc:grpc-services:${ComponentVersions.grpc}")
        implementation("com.google.protobuf:protobuf-java-util:3.17.3")
        implementation("io.grpc:grpc-netty-shaded:${ComponentVersions.grpc}")

        implementation("com.google.guava:guava:30.1.1-jre")
        implementation("org.apache.commons:commons-lang3:3.12.0")

        implementation("com.graphql-java:graphql-java:17.0")

    }

    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}
