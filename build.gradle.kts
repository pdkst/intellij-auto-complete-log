plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "io.github.pdkst"
version = "1.1.3"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

intellij {
    version.set("2022.1.4")
    type.set("IC")
    plugins.set(listOf("com.intellij.java"))
    downloadSources.set(false)
}

dependencies {
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.13.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    
    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("251.*")
    }
    
    runIde {
        // 禁用插件验证器下载
        jvmArgs("-Dide.plugins.snapshot.on.unload.fail=false")
    }
}
