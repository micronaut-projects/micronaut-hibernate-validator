pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "6.1.0"
}

rootProject.name = "hibernate-validator-parent"

include("hibernate-validator-bom")
include("hibernate-validator")

configure<io.micronaut.build.MicronautBuildSettingsExtension> {
    // Required for 4.0.0-SNAPSHOT, can be removed after
    addSnapshotRepository()
    importMicronautCatalog()
    importMicronautCatalog("micronaut-serde")
}
