#!/usr/bin/env groovy

def call(String version, Closure body) {
    def levantHome = tool name: "levant-${version}", type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    withEnv(["PATH+LEVANT=${levantHome}"], body)
}
