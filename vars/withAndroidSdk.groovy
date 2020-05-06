#!/usr/bin/env groovy

// AndroidSdk is configured via Custom Tools Plugin: https://plugins.jenkins.io/custom-tools-plugin
// Manage Jenkins -> Global Tool Configuration
def call(String version, Closure body) {
    def androidSdkHome = tool name: "AndroidSdk-${version}", type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    withEnv(["PATH+ANDROID_HOME=${androidSdkHome}"], body)
}
