import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    java
    `maven-publish`
}

val artifactVersion = "0.0.1"
val artifact = "ground-client"
val extremumGroup = "io.extremum"
val releasesRepoUrl = "https://artifactory.extremum.monster/artifactory/extremum-releases/"
val snapshotsRepoUrl = "https://artifactory.extremum.monster/artifactory/extremum-snapshots/"

val springBootVersion = "3.0.1"
val extremumToolsVersion = "0.0.4"

group = extremumGroup
version = artifactVersion
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri(snapshotsRepoUrl)
        credentials {
            username = System.getenv("ARTIFACTORY_USER")
            password = System.getenv("ARTIFACTORY_PASSWORD")
        }
        mavenContent {
            snapshotsOnly()
        }
    }

    maven {
        url = uri(releasesRepoUrl)
        credentials {
            username = System.getenv("ARTIFACTORY_USER")
            password = System.getenv("ARTIFACTORY_PASSWORD")
        }
    }
}
configurations {
    all {
        exclude(module = "logback-classic")
    }
}

dependencies {
    implementation("io.extremum:extremum-shared-models:2.1.17-SNAPSHOT") {
        exclude("io.extremum", "extremum-mongo-db-factory-reactive")
        exclude("io.extremum", "extremum-mongo-db-factory-sync")
    }
    implementation("io.extremum:extremum-model-tools:$extremumToolsVersion")
    testImplementation("io.extremum:extremum-test-tools:$extremumToolsVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("de.cronn:reflection-util:2.14.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    implementation("org.apache.commons:commons-text:1.3")

    implementation("io.smallrye:smallrye-graphql-client:1.7.1")
    implementation("io.vertx:vertx-core:3.5.1")

    testImplementation("io.rest-assured:rest-assured:4.5.1") {
        exclude("javax.activation", "activation")
        exclude("javax.activation", "javax.activation-api")
        exclude("jakarta.activation", "jakarta.activation-api")
        exclude("com.sun.xml.bind", "jaxb-osg")
        exclude("commons-logging", "commons-logging")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = extremumGroup
            artifactId = artifact
            version = artifactVersion

            from(components["java"])
        }

        repositories {
            maven {
                val isReleaseVersion = !(version as String).endsWith("-SNAPSHOT")
                url = uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
                credentials {
                    username = System.getenv("ARTIFACTORY_USER")
                    password = System.getenv("ARTIFACTORY_PASSWORD")
                }
            }
        }
    }
}

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}