/*
 * This file was generated by the Gradle 'init' task.
 */
import com.google.protobuf.gradle.*

plugins {
    idea
    id("com.google.protobuf") version "0.8.17"
    id("io.growing.progate.java-publishing-conventions")
}

dependencies {
    api("io.grpc:grpc-stub")
    api("io.grpc:grpc-protobuf")
    if (JavaVersion.current().isJava9Compatible) {
        compileOnly("javax.annotation:javax.annotation-api:${ComponentVersions.annotationApi}")
    }

}

protobuf {

    protoc {
        artifact = if (osdetector.os == "osx") {
            "com.google.protobuf:protoc:${ComponentVersions.protobuf}:osx-x86_64"
        } else {
            "com.google.protobuf:protoc:${ComponentVersions.protobuf}"
        }
    }

    plugins {

        id("grpc") {
            artifact = if (osdetector.os == "osx") {
                "io.grpc:protoc-gen-grpc-java:${ComponentVersions.grpc}:osx-x86_64"
            } else {
                "io.grpc:protoc-gen-grpc-java:${ComponentVersions.grpc}"
            }
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
