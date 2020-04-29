#!/usr/bin/env groovy

// See: https://docs.gradle.org/current/userguide/installation.html#step_3_configure_your_system_environment
def call(String version, Closure body) {
    def gradleHome = tool name: "${version}", type: 'gradle'
    withEnv(["PATH+GRADLE=${gradleHome}/bin","GRADLE_HOME=${gradleHome}"], body)
}
