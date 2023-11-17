plugins {
    id("gradlebuild.distribution.api-java")
    id("gradlebuild.update-init-template-versions")
}

description = """This project contains the Build Init plugin, which is automatically applied to the root project of every build, and provides the init and wrapper tasks.

This project should NOT be used as an implementation dependency anywhere (except when building a Gradle distribution)."""

dependencies {
    api(libs.inject)
    api(libs.jsr305)
    api(libs.maven3Core)
    api(libs.maven3Settings)

    api(project(":base-annotations"))
    api(project(":base-services"))
    api(project(":core"))
    api(project(":core-api"))
    api(project(":dependency-management"))
    api(project(":file-collections"))
    api(project(":logging"))
    api(project(":platform-jvm"))
    api(project(":toolchains-jvm"))
    api(project(":workers"))

    implementation(project(":logging-api"))
    implementation(project(":platform-native"))
    implementation(project(":plugins")) {
        because("Needs access to StartScriptGenerator.")
    }
    implementation(project(":plugins-jvm-test-suite"))
    implementation(project(":resources"))
    implementation(project(":wrapper"))
    implementation(project(":wrapper-shared"))

    implementation(libs.groovy)
    implementation(libs.groovyTemplates)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.commonsLang)
    implementation(libs.maven3SettingsBuilder)
    implementation(libs.maven3Artifact)
    implementation(libs.maven3Model)
    implementation(libs.mavenResolverApi)
    implementation(libs.slf4jApi)
    implementation(libs.plexusClassworlds)
    implementation(libs.plexusUtils)
    implementation(libs.eclipseSisuPlexus) {
        exclude(module = "cdi-api")
    }

    compileOnly(libs.maven3Compat)
    compileOnly(libs.maven3PluginApi)

    compileOnly(project(":platform-base"))

    testRuntimeOnly(libs.maven3Compat)
    testRuntimeOnly(libs.maven3PluginApi)

    testImplementation(project(":cli"))
    testImplementation(project(":base-services-groovy"))
    testImplementation(project(":native"))
    testImplementation(project(":snapshots"))
    testImplementation(project(":process-services"))
    testImplementation(testFixtures(project(":core")))
    testImplementation(testFixtures(project(":platform-native")))

    testFixturesImplementation(project(":base-services"))
    testFixturesImplementation(project(":platform-base"))
    testFixturesImplementation(project(":core-api"))
    testFixturesImplementation(project(":logging"))
    testFixturesImplementation(project(":plugins"))
    testFixturesImplementation(project(":plugins-java"))
    testFixturesImplementation(project(":testing-base"))
    testFixturesImplementation(project(":test-suites-base"))
    testFixturesImplementation(project(":plugins-jvm-test-suite"))

    integTestImplementation(project(":native"))
    integTestImplementation(libs.jetty)

    testRuntimeOnly(project(":distributions-jvm")) {
        because("ProjectBuilder tests load services from a Gradle distribution.  Toolchain usage requires JVM distribution.")
    }
    integTestDistributionRuntimeOnly(project(":distributions-full"))
}

packageCycles {
    excludePatterns.add("org/gradle/api/tasks/wrapper/internal/*")
}
