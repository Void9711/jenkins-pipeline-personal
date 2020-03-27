#!/usr/bin/env groovy

import groovy.json.JsonOutput

def loadFeishuConf() {
    def resource = libraryResource 'feishuConf.yaml'
    return readYaml(text: resource)
}

def getAccessToken(tokenUrl, appId, appSecret) {
    def body = [
        'app_id': appId,
        'app_secret': appSecret
    ]
    def response = httpRequest(
        url: tokenUrl,
        httpMode: 'POST',
        contentType: 'APPLICATION_JSON',
        requestBody: JsonOutput.toJson(body)
    )
    def statusCode = response.status
    def contentJson = readJSON(text: response.content)
    def code = contentJson.code
    if (code == 0) {
        return contentJson.tenant_access_token
    } else {
        echo "Get access token failed, error ${code}, message: ${contentJson.msg}"
        return ''
    }
}

def getReadableDuration() {
    def duration = currentBuild.duration / 1000
    int hours = duration / 3600
    int minutes = duration / 60 - hours * 60
    int seconds = duration - minutes * 60 - hours * 3600
    if (hours > 0) {
        return "${hours} hr ${minutes} min"
    } else if (minutes > 0) {
        return "${minutes} min ${seconds} sec"
    } else {
        return "${seconds} sec"
    }
}

def getSummaryText() {
    def fileList = []
    def authorList = []
    currentBuild.changeSets.each {
        changeLogSet -> changeLogSet.items.each {
            entry -> authorList += entry.author.toString()
            entry.affectedFiles.each {
                file -> fileList += file.path.toString()
            }
        }
    }
    fileList.unique()
    authorList.unique()
    return "${fileList.size()} file(s) changed - ${authorList.join(',')}"
}

def getChangesText(changesMaxNum) {
    // Without summary, the changes could print more line
    def i = 1
    def msgList = []
    currentBuild.changeSets.each {
        changeLogSet -> changeLogSet.items.each {
            entry -> 
            if (i <= changesMaxNum) { 
                msgList += " • ${entry.msg} [${entry.author}]"
            }
            i++
        }
    }
    if (i <= changesMaxNum) {
        return msgList.join('\n')
    } else {
        msgList += "${i - changesMaxNum - 1} more... "
        return msgList.join('\n')
    }
}

def getFeishuMessage(what, duration) {
    return "[${env.JOB_NAME}](${env.JOB_URL}) - [#${env.BUILD_NUMBER}](${env.BUILD_URL}) ${what}${duration}"
}

def sendFeishu(channel, what, attachmentText, color) {
    def feishuConf = loadFeishuConf()
    def retryMax = 3

    if (channel == '') {
        channel = feishuConf.defaultChannel
    }

    def channels = feishuConf.channels
    def channelNum = channels.size()
    def chatId = ''
    for (int i = 0; i < channelNum; i++) {
        if (channels[i].id == channel) {
            chatId = channels[i].chatId
        }
    }

    def body = [
        'chat_id': chatId,
        'msg_type': 'interactive',
        'card': getMessageCard(what, attachmentText, color)
    ]

    echo JsonOutput.prettyPrint(JsonOutput.toJson(body))
    def requestBody = JsonOutput.toJson(body)

    sendMessage(feishuConf.pushUrl, requestBody, retryMax)
}

// See https://open.feishu.cn/document/ukTMukTMukTM/uADOwUjLwgDM14CM4ATN
def getMessageCard(what, attachmentText, color) {
    def elements = []
    def fields = []

    def messageCard = [
        'elements': elements
    ]

    def title = ''
    if (color == '') {
        title = what
    } else {
        title = '<font color="'+color+'">●</font>  '+what
    }

    def content = ''
    if (attachmentText != '') {
       content = attachmentText
    }

    def field = [
        "is_short": false,
        "text": getTextField('lark_md', content)
    ]
    fields.add(field)

    def element = [
        "tag": "div",
        "text": getTextField('lark_md', title),
        "fields": fields
    ]
    elements.add(element)

    return messageCard
}

def getTextField(tag, content) {
    return [
        'tag': tag,
        'content': content
    ]
}

def sendMessage(url, requestBody, retry) {
    if (retry < 0) {
        echo 'Failed to send message, retry reached max value.'
        return
    }
    def feishuConf = loadFeishuConf()
    withCredentials([usernamePassword(credentialsId: feishuConf.credential, usernameVariable: 'APP_ID', passwordVariable: 'APP_SECRET')]) {
        def token = getAccessToken(feishuConf.tokenUrl, env.APP_ID, env.APP_SECRET)
        def response = httpRequest(
            url: url,
            customHeaders: [[name: 'Authorization', value: "Bearer ${token}", maskValue: true]],
            httpMode: 'POST',
            contentType: 'APPLICATION_JSON',
            requestBody: requestBody
        )
        def statusCode = response.status
        def contentJson = readJSON(text: response.content)
        def code = contentJson.code
        if (code != 0) {
            echo "Failed to send message, error code: ${code}, message: ${contentJson.msg}."
            retry--
            sendMessage(url, requestBody, retry)
        }
    }
}

def call(channel, message, attachmentText='', color='') {
    assert message
    if (color != '') {
        color = color == "red" ? "warning" : "info"
    }
    sendFeishu(channel, message, attachmentText, color)
}

def call(Map params = [:]) {
    def channel = params.get('channel','') // optional
    def what = params.get('what', '')
    assert what

    def withSummary = params.get('withSummary', false)
    def withChanges = params.get('withChanges', false)

    def summaryText = withSummary ? getSummaryText() : ''
    def changesText = withChanges ? getChangesText(withSummary ? 4 : 5) : ''

    def withDuration = params.get('withDuration', false)
    def duration = withDuration ? " after ${getReadableDuration()}" : ''

    def attachmentText = "${summaryText} \n${changesText}"

    // Prevent blank lines
    if (!withSummary && withChanges) {
        attachmentText = " ${summaryText}${changesText}"
    }
    if (currentBuild.changeSets.size() == 0) {
        attachmentText = "No changes"
    }
    def color = currentBuild.result == "FAILURE" ? "warning" : "info"

    def message = getFeishuMessage(what, duration)
    if (withSummary || withChanges) {
        sendFeishu(channel, message, attachmentText, color)
    } else {
        sendFeishu(channel, message, '', '')
    }
}

// what can be: Started, Success, Failure
def call(String what) {
    call what: what
}
