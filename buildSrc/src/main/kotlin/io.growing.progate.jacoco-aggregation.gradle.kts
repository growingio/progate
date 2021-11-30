plugins {
    java
    jacoco
}

repositories {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.growingio.cn/repository/maven-public/")
        }
        mavenCentral()
    }
}
