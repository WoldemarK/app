import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.api.publish.maven.MavenPublication
import java.net.URI

val versions = mapOf(
    "mapstructVersion" to "1.5.5.Final",
    "springdocOpenapiStarterWebmvcUiVersion" to "2.5.0",
    "javaxAnnotationApiVersion" to "1.3.2",
    "javaxValidationApiVersion" to "2.0.0.Final",
    "comGoogleCodeFindbugs" to "3.0.2",
    "springCloudStarterOpenfeign" to "4.1.1",
    "javaxServletApiVersion" to "2.5",
    "logbackClassicVersion" to "1.5.18",
    "hibernateEnversVersion" to "6.4.4.Final",
    "testContainersVersion" to "1.19.3",
    "junitJupiterVersion" to "5.10.0",
    "feignMicrometerVersion" to "13.6"
)

plugins {
    idea
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("org.openapi.generator") version "7.13.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
description = "Persons domain service for study project"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    // SPRING
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdocOpenapiStarterWebmvcUiVersion"]}")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:${versions["springCloudStarterOpenfeign"]}")

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
    implementation("org.hibernate.orm:hibernate-envers:${versions["hibernateEnversVersion"]}")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

    // HELPERS
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
    compileOnly("com.google.code.findbugs:jsr305:${versions["comGoogleCodeFindbugs"]}")
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
============== OpenAPI Generation ==============
──────────────────────────────────────────────────────
*/

val openApiDir = file("${rootDir}/openapi")
val foundSpecifications = openApiDir.listFiles { _, name -> name.endsWith(".yaml") || name.endsWith(".yml") } ?: emptyArray()

logger.lifecycle("Found ${foundSpecifications.size} OpenAPI specs: ${foundSpecifications.joinToString { it.name }}")

foundSpecifications.forEach { specFile ->
    val apiName = specFile.nameWithoutExtension
    val packageName = defineJavaPackageName(apiName)
    val basePackage = "com.example.$packageName"
    val taskName = buildGenerateApiTaskName(apiName)

    // Имя переменной НЕ должно совпадать с именем свойства задачи!
    val generatedOutputDir = layout.buildDirectory.dir("generated-sources/openapi/$apiName")

    tasks.register(taskName, GenerateTask::class) {
        generatorName.set("spring")
        inputSpec.set(specFile.absolutePath)
        // Передаём СТРОКУ — абсолютный путь
        outputDir.set(generatedOutputDir.map { it.asFile.absolutePath })

        configOptions.set(
            mapOf(
                "sourceFolder" to "src/main/java",
                "library" to "spring-cloud",
                "skipDefaultInterface" to "true",
                "useBeanValidation" to "true",
                "openApiNullable" to "false",
                "useFeignClientUrl" to "true",
                "useTags" to "true",
                "apiPackage" to "$basePackage.api",
                "modelPackage" to "$basePackage.dto",
                "configPackage" to "$basePackage.config"
            )
        )
    }

    sourceSets["main"].java.srcDir(generatedOutputDir.map { it.dir("src/main/java") })
}

tasks.register("generateAllOpenApi") {
    foundSpecifications.forEach { spec ->
        dependsOn(buildGenerateApiTaskName(spec.nameWithoutExtension))
    }
}

tasks.named("compileJava") {
    dependsOn("generateAllOpenApi")
}

/*
──────────────────────────────────────────────────────
============== JARs for Generated APIs ==============
──────────────────────────────────────────────────────
*/

val generatedJars = mutableListOf<Jar>()

foundSpecifications.forEach { specFile ->
    val apiName = specFile.nameWithoutExtension
    val generateTaskName = buildGenerateApiTaskName(apiName)
    val jarTaskName = buildJarTaskName(apiName)

    val sourceSetName = apiName
    val sourceSet = sourceSets.create(sourceSetName) {
        java.srcDir(layout.buildDirectory.dir("generated-sources/openapi/$apiName/src/main/java"))
        compileClasspath += sourceSets["main"].compileClasspath
        runtimeClasspath += sourceSets["main"].runtimeClasspath
    }

    val compileTask = tasks.register<JavaCompile>("compile${sourceSetName.replaceFirstChar(Char::uppercase)}Java") {
        source = sourceSet.java
        classpath = sourceSet.compileClasspath
        destinationDirectory.set(layout.buildDirectory.dir("classes/generated/$apiName"))
        dependsOn(generateTaskName)
    }

    val jarTask = tasks.register<Jar>(jarTaskName) {
        group = "build"
        archiveBaseName.set(apiName)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        from(compileTask.map { it.destinationDirectory })
        dependsOn(compileTask)
    }

    generatedJars.add(jarTask.get())
}

/*
──────────────────────────────────────────────────────
============== Load .env ==============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach { line ->
    if (line.isNotBlank() && !line.startsWith("#")) {
        val parts = line.split("=", limit = 2)
        if (parts.size == 2) {
            System.setProperty(parts[0].trim(), parts[1].trim())
        }
    }
}

val nexusUrl = System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL") ?: error("NEXUS_URL not set")
val nexusUser = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME") ?: error("NEXUS_USERNAME not set")
val nexusPassword = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD") ?: error("NEXUS_PASSWORD not set")

/*
──────────────────────────────────────────────────────
============== Publishing to Nexus ==============
──────────────────────────────────────────────────────
*/

publishing {
    publications {
        foundSpecifications.forEach { specFile ->
            val apiName = specFile.nameWithoutExtension
            val jarTaskName = buildJarTaskName(apiName)
            val pubName = "publish${apiName.replaceFirstChar(Char::uppercase)}Jar"

            create<MavenPublication>(pubName) {
                groupId = "com.example"
                artifactId = apiName
                version = project.version.toString()
                artifact(tasks.named<Jar>(jarTaskName))

                pom {
                    name.set("Generated API: $apiName")
                    description.set("Auto-generated from ${specFile.name} OpenAPI spec")
                    // url.set("https://github.com/your/repo") // опционально
                }
            }
        }
    }

    repositories {
        maven {
            name = "nexus"
            url = URI(
                if (project.version.toString().endsWith("SNAPSHOT")) {
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

// ─── Вспомогательные функции ───────────────────────────────────────

fun defineJavaPackageName(name: String): String {
    val beforeDash = name.substringBefore('-').lowercase()
    return Regex("^[a-z]+").find(beforeDash)?.value ?: beforeDash
}

fun buildGenerateApiTaskName(name: String): String = buildTaskName("generate", name)
fun buildJarTaskName(name: String): String = buildTaskName("jar", name)

fun buildTaskName(prefix: String, name: String): String {
    val cleanName = name.split(Regex("[^A-Za-z0-9]"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar(Char::uppercase) }
    return "$prefix$cleanName"
}