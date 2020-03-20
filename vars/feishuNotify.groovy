#!/usr/bin/env groovy

def getDescription(what) {
    return "```\n${what}\n```"
}

def feishuSend(channel, what) {
    withCredentials([string(variable: 'PUSH_URL', credentialsId: channel)]) {
        discordSend webhookUrl: PUSH_URL, description: getDescription(what), link: env.BUILD_URL
    }
}

def call(Map params = [:]) {
    def channel = params.get('channel', 'feishu-push')
    def what = params.get('what', '')
    assert what

    feishuSend(channel, what)
}
