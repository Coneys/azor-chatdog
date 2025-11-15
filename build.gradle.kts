plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

group = "com.github.coneys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.genai:google-genai:1.27.0")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:3.3.2")
    implementation("io.ktor:ktor-client-cio:3.3.2")
    implementation("io.ktor:ktor-client-logging:3.3.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.5.21")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("com.github.coneys.kazor.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}