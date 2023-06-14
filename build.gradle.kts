import korlibs.korge.gradle.korge

plugins {
    alias(libs.plugins.korge)
}

repositories {
    maven("https://repsy.io/mvn/yeicor/github-public")
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

    //serializationJson()

    dependencies {
        commonMainImplementation("com.github.yeicor:kraphviz:1.0.0-SNAPSHOT") { isChanging = true } // Needs maven repo
    }
}


dependencies {
    add("commonMainApi", project(":deps"))
}

