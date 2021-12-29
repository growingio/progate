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
        implementation("org.slf4j:slf4j-api:${ComponentVersions.slf4j}")
        implementation("com.typesafe:config:${ComponentVersions.typesafeConfig}")
        implementation("com.github.os72:protoc-jar:${ComponentVersions.protocJar}")
        implementation("org.apache.commons:commons-text:${ComponentVersions.commonsText}")
//        implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
        implementation("com.github.ben-manes.caffeine:caffeine:${ComponentVersions.caffeine}")
        implementation("io.vertx:vertx-web:${ComponentVersions.vertx}")
        implementation("io.vertx:vertx-core:${ComponentVersions.vertx}")
        implementation("io.vertx:vertx-web-client:${ComponentVersions.vertx}")
        implementation("io.vertx:vertx-web-openapi:${ComponentVersions.vertx}")
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

        implementation("io.swagger.parser.v3:swagger-parser:2.0.28")


    }

    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

// Share sources folder with other projects for aggregated JaCoCo reports
configurations.create("transitiveSourcesElements") {
    isVisible = false
    isCanBeResolved = false
    isCanBeConsumed = true
    extendsFrom(configurations.implementation.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
    }
    sourceSets.main.get().java.srcDirs.forEach {
        outgoing.artifact(it)
    }
}

// Share the coverage data to be aggregated for the whole product
configurations.create("coverageDataElements") {
    isVisible = false
    isCanBeResolved = false
    isCanBeConsumed = true
    extendsFrom(configurations.implementation.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("jacoco-coverage-data"))
    }
    // This will cause the test task to run if the coverage data is requested by the aggregation task
    outgoing.artifact(tasks.test.map { task ->
        task.extensions.getByType<JacocoTaskExtension>().destinationFile!!
    })
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}
