import org.gradle.internal.os.OperatingSystem

plugins {
    id("java")
    id("application")
}

group = "de.fabiexe"
version = "1.1.2"

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
    implementation("commons-io:commons-io:2.19.0")
}

sourceSets.main.get().resources.srcDirs(layout.buildDirectory.file("frontend"))

tasks {
    compileJava {
        doFirst {
            if (OperatingSystem.current() == OperatingSystem.WINDOWS) {
                ProcessBuilder("cmd.exe", "/c", "npm install && npm run build && tar -cf frontend.tar dist")
                    .inheritIO()
                    .directory(file("frontend"))
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("/bin/bash", "-c", "npm install && npm run build && tar -cf frontend.tar dist")
                    .inheritIO()
                    .directory(file("frontend"))
                    .start()
                    .waitFor()
            }
            copy {
                from(file("frontend/frontend.tar"))
                into(layout.buildDirectory.dir("frontend"))
            }
        }
        options.encoding = "UTF-8"
        options.release = 21
    }

    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes["Main-Class"] = "de.fabiexe.spind.client.Main"
            attributes["Implementation-Title"] = "Spind"
            attributes["Implementation-Version"] = version
            attributes["Implementation-Vendor"] = "Fabi.exe"
        }
        archiveFileName = "Spind.jar"
    }

    named<JavaExec>("run") {
        workingDir = file("run")
        doFirst {
            workingDir.mkdirs()
        }
    }
}

application {
    mainClass = tasks.jar.get().manifest.attributes["Main-Class"] as String
}
