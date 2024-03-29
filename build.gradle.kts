import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    java
    `maven-publish`
    signing
}

val artifactVersion = "3.0.0"
val artifact = "ground-client"
val extremumGroup = "io.extremum"
val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

val springBootVersion = "3.0.1"
val extremumToolsVersion = "3.0.0"

group = extremumGroup
version = artifactVersion
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

configurations {
    all {
        exclude(module = "logback-classic")
    }
}

dependencies {
    implementation("io.extremum:extremum-shared-models:3.0.0") {
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

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = extremumGroup
            artifactId = artifact
            version = artifactVersion

            from(components["java"])

            pom {
                name.set("ground-client")
                description.set("Client for Ground api")
                url.set("https://github.com/smekalka/extremum-ground-client")
                inceptionYear.set("2022")

                scm {
                    url.set("https://github.com/smekalka/extremum-ground-client")
                    connection.set("scm:https://github.com/smekalka/extremum-ground-client.git")
                    developerConnection.set("scm:git://github.com/smekalka/extremum-ground-client.git")
                }

                licenses {
                    license {
                        name.set("Business Source License 1.1")
                        url.set("https://github.com/smekalka/extremum-ground-client/blob/develop/LICENSE.md")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("SherbakovaMA")
                        name.set("Maria Sherbakova")
                        email.set("m.sherbakova@smekalka.com")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            val isReleaseVersion = !(version as String).endsWith("-SNAPSHOT")
            url = uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
            credentials {
                username = System.getProperty("ossrhUsername")
                password = System.getProperty("ossrhPassword")
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}