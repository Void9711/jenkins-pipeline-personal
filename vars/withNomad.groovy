#!/usr/bin/env groovy

def call(String version, Closure body) {
    def nomadHome = tool name: "nomad-${version}", type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    withEnv(["PATH+NOMAD=${nomadHome}"], body)
}
