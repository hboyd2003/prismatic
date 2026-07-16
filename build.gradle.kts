plugins {
    idea
    alias(libs.plugins.gitSimpleSemver)
    alias(libs.plugins.indra).apply(false)
}

tasks {
    jar {
        enabled = false
    }

    javadoc {
        enabled = false
    }
}
