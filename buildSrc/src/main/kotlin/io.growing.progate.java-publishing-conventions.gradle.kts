plugins {
    `maven-publish`
    id("io.growing.progate.java-library-conventions")
}

group = "io.growing.progate"
version = "1.0.1-SNAPSHOT"

publishing {
    repositories {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
        maven {
            name = "growingNexus"
            val host: String = "https://nexus.growingio.cn"
            val releasesRepoUrl = uri("$host/repository/maven-releases/")
            val snapshotsRepoUrl = uri("$host/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials(PasswordCredentials::class)
        }
    }
}
