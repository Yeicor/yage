import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import korlibs.korge.gradle.korge
import korlibs.korge.gradle.util.getByName

plugins {
    alias(libs.plugins.korge)
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

repositories {
    maven("https://repsy.io/mvn/yeicor/github-public")
    maven("https://repository.openmindonline.it")
}

korge {
    id = "com.github.yeicor.yage"
    name = "Yet Another Graphviz Editor"
    title = name

// To enable all targets at once

    //targetAll()

// To enable targets based on properties/environment variables
    //targetDefault()

// To selectively enable targets

    targetJvm()
    targetJs()
    //targetDesktop() //linuxArm64 is not supported yet (need to configure github actions for cross-compilation)
    targetIos()
    targetAndroid()
    //targetWasm() // Supported in future versions of KorGE

    //serializationJson()

    dependencyMulti("org.apache.xmlgraphics:batik-transcoder:1.16", targets = listOf("jvm", "android"))
    dependencyMulti("org.apache.xmlgraphics:batik-codec:1.16", targets = listOf("jvm", "android"))

    extensions.getByName<TestedExtension>("android").apply {
        @Suppress("UnstableApiUsage")
        packagingOptions {
            resources.excludes.add("license/*.txt") // Causes conflicts with batik
            resources.excludes.add("license/*.md") // Causes conflicts with batik
            resources.excludes.add("license/NOTICE") // Causes conflicts with batik
            resources.excludes.add("license/LICENSE") // Causes conflicts with batik
        }
    }
}

dependencies {
    add("commonMainApi", project(":deps"))
    commonMainImplementation("com.github.yeicor:kraphviz:1.0.0-SNAPSHOT") { isChanging = true } // Needs maven repo
}

tasks.create("shadowJar", ShadowJar::class) {
    archiveBaseName.set("yage-shadow")
    archiveClassifier.set("")
    archiveVersion.set("")
    from(tasks.getByName("jvmJar").outputs)
    configurations = project.configurations.filter {
        it.isCanBeResolved && it.name.run {
            contains("jvm", true) && contains("runtime", true)
        }
    }
    manifest { attributes(mapOf("Main-Class" to "MainKt")) }
}
tasks.build.dependsOn("shadowJar")
