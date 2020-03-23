#!/usr/bin/env groovy

def getDescription(what) {
    return "```\n${what}\n```"
}

def feishuSend(channel, what) {
    withCredentials([string(variable: 'PUSH_URL', credentialsId: 'feishu-push')]) {
        discordSend description: '{"title": "Hello Feishu", "text": "Good Feishu"}', footer: "Footer Text", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "${PUSH_URL}"
    }
}

def call(Map params = [:]) {
    def channel = params.get('channel', 'feishu-push')
    def what = params.get('what', '')
    assert what

    feishuSend(channel, what)
}
