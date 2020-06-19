#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(String id, boolean debug = false) {
    def resource = libraryResource 'projects.yml'
    def settings = readYaml(text: resource)
    def projects = settings.projects

    projects.each {
        project -> 
        if (project.id == id) {
            if (debug) {
                printProject(project)
            }
            return project
        }
    }
    echo "Requested project ${id} not found."
    return ''
}

def printProject(project) {
    def json = JsonOutput.toJson(project)
    json = JsonOutput.prettyPrint(json)
    echo "[loadProject] ${project.id} - ${project.name}\n${json}"
}
