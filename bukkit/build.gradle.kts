import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyCommonArtifactoryConfig()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "arim-mvn-lgpl3"
        url = uri("https://mvn-repo.arim.space/lesser-gpl3/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    "compileOnly"("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    "api"(project(":nuvotifier-api"))
    "api"(project(":nuvotifier-common"))
    // MorePaperLib provides Folia-compatible scheduling; shaded into the plugin jar
    "shadeOnly"("space.arim.morepaperlib:morepaperlib:0.4.3")
}


tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(dependency(":nuvotifier-api"))
        include(dependency(":nuvotifier-common"))
        include(dependency("space.arim.morepaperlib:morepaperlib:.*"))
    }
    // Relocate MorePaperLib to avoid conflicts with other plugins that shade it
    relocate("space.arim.morepaperlib", "com.vexsoftware.votifier.libs.morepaperlib")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
