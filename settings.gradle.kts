import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/lanlacope/android-rewheel")
            credentials {
                val localProperties = Properties()
                localProperties.load(file("${rootProject.projectDir}/local.properties").inputStream())
                username = localProperties.getProperty("gpr.user")
                password = localProperties.getProperty("gpr.token")
            }
        }
    }
}

rootProject.name = "NXSharingHelper"
include(":app")
 