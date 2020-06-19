#!/usr/bin/env groovy

def call(String project) {
    def projectSettings = loadProject(project)
    def schedule = ''
    if (!projectSettings) {
        echo "[ERROR] Failed to load pollscm schedule for project ${project}."
        currentBuild.result = 'FAILURE'
        return schedule
    }

    if (env.BRANCH_NAME == 'master') {
        print(projectSettings.pollscm-schedule)
        projectSettings.pollscm-schedule.each {
            buildTime -> 
            schedule += buildTime
            schedule.join('\n')
        }
    }
    echo schedule
    return schedule
}
