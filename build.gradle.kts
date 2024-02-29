plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "io.github.pdkst"
version = "1.1.2"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

dependencies {
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.13.0")
    implementation("ch.qos.logback:logback-classic:1.5.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
