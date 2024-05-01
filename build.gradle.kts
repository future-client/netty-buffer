plugins {
    `java-library`
    `maven-publish`
    signing
}

/**
 * Extracted from the Netty project (commit hash: f235b37c281977b5428dacb32cc44072c9a441e5, version 4.1.76.Final-SNAPSHOT).
 *
 * The Netty project is licensed under the Apache License 2.0.
 *
 * commit f235b37c281977b5428dacb32cc44072c9a441e5 (HEAD -> 4.1, origin/HEAD, origin/4.1)
 * Author: Hiep Vo <53218461+andrewvo148@users.noreply.github.com>
 * Date:   Mon Mar 28 17:17:49 2022 +0700
 *
 * Fix a typo (#12241)
 **/
group = "net.futureclient.netty-buffer"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

sourceSets {
    main {
        tasks.getByName(compileJavaTaskName, closureOf<JavaCompile> {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
        })
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
}

testing {
    suites {
        @Suppress("UnstableApiUsage") val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

publishing {
    repositories {
        val publishName = System.getenv("MAVEN_PUBLISH_NAME")
        val publishToken = System.getenv("MAVEN_PUBLISH_TOKEN")
        if (publishName != null && publishToken != null) {
            maven {
                name = "future-private"
                url = uri("https://maven.futureclient.net/private")
                credentials {
                    username = publishName
                    password = publishToken
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

signing {
    val signingKeyId = System.getenv("GPG_SIGNING_KEY_ID")
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}
