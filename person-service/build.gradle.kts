import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

val versions = mapOf(
    "keycloakAdminClientVersion" to "22.0.3",
    "springdocOpenapiStarterWebfluxUiVersion" to "2.5.0",
    "mapstructVersion" to "1.5.5.Final",
    "javaxAnnotationApiVersion" to "1.3.2",
    "javaxValidationApiVersion" to "2.0.0.Final",
    "logbackClassicVersion" to "1.5.18",
    "nettyResolverVersion" to "4.1.121.Final:osx-aarch_64",
    "feignMicrometerVersion" to "13.6",
    "testContainersVersion" to "1.19.3",
    "comGoogleCodeFindbugs" to "3.0.2",
    "springCloudStarterOpenfeign" to "4.1.1",
    "springdocOpenapiStarterWebmvcUiVersion" to "2.5.0",
)

plugins {
    java
    idea
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.13.0"
    `maven-publish`
}

group = "com.example"
version = "1.0.0-SNAPSHOT" // ← обязательно SNAPSHOT, если публикуете в maven-snapshots

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    resolutionStrategy.force("com.google.code.findbugs:jsr305:3.0.2")
}

dependencies {
// SPRING
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdocOpenapiStarterWebmvcUiVersion"]}")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

// OBSERVABILITY
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

// PERSISTENCE
    implementation("org.hibernate.orm:hibernate-envers:6.6.0.Final")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

// HELPERS
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstructVersion"]}")
    implementation("javax.validation:validation-api:${versions["javaxValidationApiVersion"]}")
    implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApiVersion"]}")

// TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.junit.jupiter:junit-jupiter:${versions["junitJupiterVersion"]}")
    testImplementation("org.testcontainers:testcontainers:${versions["testContainersVersion"]}")
    testImplementation("org.testcontainers:postgresql:${versions["testContainersVersion"]}")
    testImplementation("org.testcontainers:junit-jupiter:${versions["testContainersVersion"]}")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
──────────────────────────────────────────────────────
============== Api generation =========
──────────────────────────────────────────────────────
*/

val openApiDir = file("$rootDir/openapi")
val foundSpecifications = openApiDir.listFiles { f -> f.extension in listOf("yaml", "yml") } ?: emptyArray()
logger.lifecycle("Found ${foundSpecifications.size} specifications: " + foundSpecifications.joinToString { it.name })

val outRoot = layout.buildDirectory.dir("generated")

val generateTasks = foundSpecifications.map { specFile ->
    val name = specFile.nameWithoutExtension
    val taskName = "generate" + name
        .split(Regex("[^A-Za-z0-9]"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar(Char::uppercase) }
    tasks.register<GenerateTask>(taskName) {
        generatorName.set("spring")
        inputSpec.set(specFile.absolutePath)
        outputDir.set(outRoot.get().asFile.absolutePath)
        val base = "com.example.${name.substringBefore('-').lowercase()}"
        configOptions.set(
            mapOf(
                "library" to "spring-cloud",
                "skipDefaultInterface" to "true",
                "useBeanValidation" to "true",
                "openApiNullable" to "false",
                "useFeignClientUrl" to "true",
                "useTags" to "true",
                "apiPackage" to "$base.api",
                "modelPackage" to "$base.dto",
                "configPackage" to "$base.config"
            )
        )
        doFirst {
            logger.lifecycle("$taskName: starting generation from ${specFile.name}")
        }
    }
}

val generateAllOpenApi = tasks.register("generateAllOpenApi") {
    dependsOn(generateTasks)
    doLast { logger.lifecycle("generateAllOpenApi: all specifications have been generated") }
}

configure<SourceSetContainer> {
    named(SourceSet.MAIN_SOURCE_SET_NAME) {
        java.srcDir(outRoot.map { it.dir("src/main/java") })
    }
}

tasks.named("compileJava") {
    dependsOn(generateAllOpenApi)
}

idea {
    module {
        val genDir = outRoot.map { it.dir("src/main/java").asFile }.get()
        generatedSourceDirs = generatedSourceDirs + setOf(genDir)
        sourceDirs = sourceDirs + setOf(genDir)
    }
}

/*
──────────────────────────────────────────────────────
============== Nexus credentials =============
──────────────────────────────────────────────────────
*/

// Загрузка .env (только для локальной разработки)
file("$rootDir/.env").takeIf { it.exists() }?.readLines()?.forEach {
    val trimmed = it.trim()
    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
        val (k, v) = trimmed.split("=", limit = 2)
        System.setProperty(k.trim(), v.trim())
    }
}

// Fallback: в Docker-сети используем http://nexus:8081
val nexusUrl = System.getenv("NEXUS_URL")
    ?: System.getProperty("NEXUS_URL")
    ?: "http://nexus:8081"

val nexusUser = System.getenv("NEXUS_USERNAME")
    ?: System.getProperty("NEXUS_USERNAME")
    ?: "admin"

val nexusPassword = System.getenv("NEXUS_PASSWORD")
    ?: System.getProperty("NEXUS_PASSWORD")
    ?: "admin"

// Валидация обязательных параметров
if (nexusUrl.isBlank() || nexusUser.isBlank() || nexusPassword.isBlank()) {
    throw GradleException("NEXUS_URL, NEXUS_USERNAME, NEXUS_PASSWORD must be set")
}

repositories {
    maven {
        url = uri(nexusUrl + if (version.toString().endsWith("SNAPSHOT")) {
            "/repository/maven-snapshots/"
        } else {
            "/repository/maven-releases/"
        })
        credentials {
            username = nexusUser
            password = nexusPassword
        }
        isAllowInsecureProtocol = true
    }
}

/*
──────────────────────────────────────────────────────
============== Publishing =============
──────────────────────────────────────────────────────
*/

publishing {
    publications {
        foundSpecifications.forEach { specFile ->
            val apiName = specFile.nameWithoutExtension
            val jarTaskName = "jar" // OpenAPI generator создаёт обычный jar
            val pubName = "publish${apiName.replaceFirstChar(Char::uppercase)}Publication"

            create<MavenPublication>(pubName) {
                groupId = "com.example"
                artifactId = apiName
                version = project.version.toString()
                from(components["java"])

                pom {
                    name.set("Generated API: $apiName")
                    description.set("Auto-generated from ${specFile.name} OpenAPI spec")
                }
            }
        }
    }

    repositories {
        maven {
            name = "nexus"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) {
                    "$nexusUrl/repository/maven-snapshots/"
                } else {
                    "$nexusUrl/repository/maven-releases/"
                }
            )
            credentials {
                username = nexusUser
                password = nexusPassword
            }
            isAllowInsecureProtocol = true
        }
    }
}