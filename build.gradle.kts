import org.gradle.internal.os.OperatingSystem

plugins {
    id("java")
    id("application")
}

group = "de.fabiexe"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.casterlabs.co/maven")
    maven("https://github.com/NotJustAnna/webview_java/raw/maven")
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("net.notjustanna.webview:webview_java:1.5.0+wv0.12.0-nightly.1")
    implementation("net.notjustanna.webview:webview_java-all-natives:1.5.0+wv0.12.0-nightly.1")
    implementation("net.notjustanna.webview:webview_java-interop:1.5.0+wv0.12.0-nightly.1")
    implementation("com.google.code.gson:gson:2.13.1")
}

sourceSets.main.get().resources.srcDirs(layout.buildDirectory.file("frontend"))

tasks {
    register<Tar>("buildFrontend") {
        doFirst {
            if (OperatingSystem.current() == OperatingSystem.WINDOWS) {
                ProcessBuilder("cmd.exe", "/c", "npm install && npm run build")
                    .inheritIO()
                    .directory(file("frontend"))
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("/bin/bash", "-c", "npm install && npm run build")
                    .inheritIO()
                    .directory(file("frontend"))
                    .start()
                    .waitFor()
            }
        }
        from(file("frontend/dist"))
        destinationDirectory = layout.buildDirectory.dir("frontend")
        archiveFileName = "frontend.tar"
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        dependsOn("buildFrontend")
    }

    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    named<JavaExec>("run") {
        workingDir = file("run")
        doFirst {
            workingDir.mkdirs()
        }
    }
}

application {
    mainClass = "de.fabiexe.spind.client.Main"
}
