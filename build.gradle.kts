import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
}

group = "com.slava_110"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "2.1.2"

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation("guru.nidi:graphviz-kotlin:0.18.1")

    implementation("com.github.yuchi:npm-semver:1.0.0")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}