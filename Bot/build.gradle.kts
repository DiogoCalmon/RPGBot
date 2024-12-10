plugins {
    application
    id("com.gradleup.shadow") version "8.3.1"
}

application.mainClass = "com.dev.Main" //
group = "org.example"
version = "1.0"

val jdaVersion = "5.2.1" //

repositories {
    mavenCentral()
    maven {url = uri("https://jitpack.io")}
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    implementation("net.dv8tion:JDA:5.2.1")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // Para fazer as requisições HTTP
    implementation("org.json:json:20230227") // Para processar JSON
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    sourceCompatibility = "11"
    targetCompatibility = "11"
}