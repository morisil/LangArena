import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.5.0-RC"
    kotlin("plugin.serialization") version "2.5.0-RC"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    listOf(
        macosArm64(),
        linuxX64(),
        linuxArm64(),
    ).forEach {
        it.binaries.executable {
            entryPoint = "main"
            baseName = "benchmarks"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.13.0")
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.3")
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()
}

val jvmMainCompilation = kotlin.jvm().compilations.getByName("main")

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("benchmarks")

    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    from(jvmMainCompilation.output.allOutputs)

    val runtimeClasspath = configurations.named("jvmRuntimeClasspath")
    dependsOn(runtimeClasspath)
    from({
        runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<JavaExec>("runDebug") {
    group = "application"
    description = "Run in debug mode"

    mainClass.set("MainKt")
    classpath = files(jvmMainCompilation.output.allOutputs, configurations.named("jvmRuntimeClasspath"))

    if (project.hasProperty("args")) {
        args((project.property("args") as String).split(" "))
    }
}

tasks.named("jvmTest", Test::class) {
    useJUnitPlatform()
}
