import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
    java
    `maven-publish`
}

group = "net.azisaba"
version = "1.5.6+1.20.2"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven { url = uri("https://nexus.neetgames.com/repository/maven-releases/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    compileOnly(files("libs/MyPet-3.10.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.0.0") {
        exclude("org.bstats", "bstats-bukkit")
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.0") {
        exclude("org.bstats", "bstats-bukkit")
    }
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.196")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration.set(ReobfArtifactConfiguration.REOBF_PRODUCTION)

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url = uri(
                if (project.version.toString().endsWith("SNAPSHOT"))
                    project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "https://repo.acrylicstyle.xyz/repository/maven-snapshots/")
                else
                    project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "https://repo.acrylicstyle.xyz/repository/maven-releases/")
            )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

tasks {
    javadoc {
        options.encoding = "UTF-8"
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        doNotTrackState("plugin.yml should be updated every time")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            filter(ReplaceTokens::class, mapOf("tokens" to mapOf("version" to project.version.toString())))
            filteringCharset = "UTF-8"
        }
    }
}
