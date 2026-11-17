import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin
    id("org.jetbrains.intellij.platform") version "2.20.1"
    id("org.jetbrains.changelog") version "2.5.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(properties("javaVersion").toInt())
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(files("libs/swag.jar", "libs/swexpl.jar"))
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        val localPath = providers.gradleProperty("localIdeaPath").orNull?.trim()
        if (!localPath.isNullOrEmpty()) {
            local(localPath)
        } else {
            intellijIdea(properties("platformVersion")) {
                useInstaller = false
            }
            val localJbr = providers.gradleProperty("localJbrPath").orNull?.trim()
            if (!localJbr.isNullOrEmpty()) jetbrainsRuntimeLocal(localJbr) else jetbrainsRuntime()
        }
        bundledPlugins(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            // empty = open-ended, so new IntelliJ releases don't lock the plugin out
            properties("pluginUntilBuild").takeIf { it.isNotEmpty() }?.let { untilBuild = it }
        }

        description = file("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").let { markdownToHTML(it) }

        changeNotes = provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion"))
                        ?: runCatching { getLatest() }.getOrElse { getUnreleased() },
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}

changelog {
    groups.set(emptyList())
    repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    withType<JavaCompile> {
        options.release = properties("javaVersion").toInt()
        options.encoding = "UTF-8"
    }

    test {
        failOnNoDiscoveredTests = false
    }

    publishPlugin {
        dependsOn("patchChangelog")
    }
}
