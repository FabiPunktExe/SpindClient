plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("commons-io:commons-io:2.19.0")
}

sourceSets.main.get().resources.srcDirs(layout.buildDirectory.file("frontend"))

tasks {
    compileJava {
        doFirst {
            if (System.getProperty("os.name").lowercase().contains("win")) {
                ProcessBuilder("cmd.exe", "/c", "npm install && npm run build && tar -cf frontend.tar dist")
                    .inheritIO()
                    .directory(file("../frontend"))
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("/bin/bash", "-c", "npm install && npm run build && tar -cf frontend.tar dist")
                    .inheritIO()
                    .directory(file("../frontend"))
                    .start()
                    .waitFor()
            }
            copy {
                from(file("../frontend/frontend.tar"))
                into(layout.buildDirectory.dir("frontend"))
            }
        }
        options.encoding = "UTF-8"
        options.release = 21
    }

    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
