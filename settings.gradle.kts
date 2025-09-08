pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "client"

include("common")
include("desktop")
// Temporarily exclude android for analysis
// include("android")
