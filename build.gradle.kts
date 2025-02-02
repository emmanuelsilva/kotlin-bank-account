plugins {
    kotlin("jvm") version "2.1.0"
}

group = "dev.emmanuel.bank"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("io.mockk:mockk-jvm:1.13.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}