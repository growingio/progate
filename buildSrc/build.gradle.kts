plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("gradle.plugin.org.barfuin.gradle.jacocolog:gradle-jacoco-log:2.0.0")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://nexus.growingio.cn/repository/maven-public/")
    }
    gradlePluginPortal()
}
