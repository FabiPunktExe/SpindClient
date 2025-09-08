plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("commons-io:commons-io:2.20.0")
    implementation("com.warrenstrange:googleauth:1.5.0")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("com.squareup.okhttp3:okhttp:5.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets.main.get().resources.srcDirs(layout.buildDirectory.file("frontend"))

tasks {
    test {
        useJUnitPlatform()
    }
    
    compileJava {
        doFirst {
            if (System.getProperty("os.name").lowercase().contains("win")) {
                ProcessBuilder("cmd.exe", "/c", "npm i && npm run build && tar -cf frontend.tar dist")
                    .inheritIO()
                    .directory(file("../frontend"))
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("/bin/bash", "-c", "npm i && npm run build && tar -cf frontend.tar dist")
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
    }

    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
