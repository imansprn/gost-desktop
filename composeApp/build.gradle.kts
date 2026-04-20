import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

/**
 * WiX/MSI product version must be MAJOR.MINOR.BUILD with segment maxima 255, 255, 65535.
 * Pads short versions (e.g. "1" -> "1.0.0") and uses the first three segments only.
 */
fun normalizeMsiProductVersion(raw: String): String {
    val parts = raw.trim().split('.').filter { it.isNotEmpty() }.take(3)
    val nums = (0 until 3).map { i -> parts.getOrNull(i)?.toIntOrNull() ?: 0 }
    val major = nums[0].coerceIn(0, 255)
    val minor = nums[1].coerceIn(0, 255)
    val build = nums[2].coerceIn(0, 65535)
    return "$major.$minor.$build"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Kotlinx
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Voyager Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.tabNavigator)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}



compose.desktop {
    application {
        mainClass = "xyz.gobliggg.gost.MainKt"

        // macOS only: custom Dock icon when running from Gradle / IDE (packaged .app uses macOS.iconFile).
        if (System.getProperty("os.name").orEmpty().lowercase().contains("mac")) {
            jvmArgs += "-Xdock:name=GOST Manager"
            jvmArgs += "-Xdock:icon=${project.file("icons/gost.png").absolutePath}"
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "xyz.gobliggg.gost"
            // Allow CI/workflows to set version from git tag: -PappVersion=1.2.3
            val rawAppVersion = (project.findProperty("appVersion") as? String)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "1.0.0"
            val distributionVersion = normalizeMsiProductVersion(rawAppVersion)
            packageVersion = distributionVersion
            linux {
                iconFile.set(project.file("icons/gost.png"))
            }
            macOS {
                iconFile.set(project.file("icons/gost.icns"))
                dockName = "GOST Manager"
            }
            windows {
                packageVersion = distributionVersion
                msiPackageVersion = distributionVersion
                iconFile.set(project.file("icons/gost.ico"))
            }
        }
    }
}
