plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://nexus.growingio.cn/repository/maven-public/")
    }
    gradlePluginPortal()
    mavenCentral()
}
