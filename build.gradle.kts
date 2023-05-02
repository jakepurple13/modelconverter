import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven(url = "https://repository.aspose.com/repo/")
    maven(url = "https://jitpack.io")
}

javafx {
    version = "20"
    modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

kotlin {
    jvm {
        jvmToolchain(11)
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation("com.aspose:aspose-3d:23.4.0")
                implementation("net.imagej:ij:1.53j")
                implementation("me.friwi:jcefmaven:110.0.25")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        args += listOf(
            "--add-opens java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        )

        jvmArgs += listOf(
            "--add-opens java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        )
        mainClass = "MainKt"
        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "modelconverter"
            packageVersion = "1.0.0"
        }
    }
}
