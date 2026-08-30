/*
 * Simple Freeze
 * Copyright (c) 2026 Harrison Boyd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

plugins {
    java
    `java-library`
    idea
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.licenser.spotless")
    id("org.gradlex.extra-java-module-info")
}

dependencies {
    implementation(libs.bundles.adventureAPI)

    compileOnly(libs.brigadier)
    compileOnly(libs.gson)
    compileOnly(libs.configurateCore)
    compileOnly(libs.configurateNBT)
    compileOnly(libs.configurateHocon)
    compileOnly(libs.guava)
    compileOnly(libs.chasm)

    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrainsAnnotations)
    compileOnly(libs.checkerFramework)

    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.brigadier)
    testImplementation(libs.gson)
    testImplementation(libs.configurateCore)
    testImplementation(libs.configurateNBT)
    testImplementation(libs.configurateHocon)
    testImplementation(libs.guava)
    testImplementation(libs.chasm)

    testImplementation(libs.jspecify)
    testImplementation(libs.jetbrainsAnnotations)
    testImplementation(libs.checkerFramework)
    testImplementation(libs.bundles.junitJupiterCompile)
    testRuntimeOnly(libs.bundles.junitJupiterRuntime)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

indra {
    javaVersions {
        target(25)
    }

    github("hboyd2003", "prismatic") {
        ci(true)
        scm(true)
        publishing(false)
    }

    lgpl3OrLaterLicense()

    configurePublications {
        pom {
            developers {
                developer {
                    id.set("hboyd2003")
                    name.set("Harrison Boyd")
                    email.set("8950185+hboyd2003@users.noreply.github.com")
                    timezone = "America/New_York"
                }
            }
        }
    }

    signWithKeyFromPrefixedProperties("hboyd")

    checkstyle(libs.versions.checkstyle.get())

    publishReleasesTo("hboydDev", "https://repo.hboyd.dev/releases")
    publishSnapshotsTo("hboydDev", "https://repo.hboyd.dev/snapshots")
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file(".spotless/license_header_template.txt"))
    newLine(true)
}

spotless {
    java {
        targetExclude("build/generated/**/*.java")
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

extraJavaModuleInfo {
    deriveAutomaticModuleNamesFromFileNames = true
}
