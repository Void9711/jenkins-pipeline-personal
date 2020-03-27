#!/usr/bin/env groovy

import groovy.json.JsonOutput

def loadFeishuConf() {
    def resource = libraryResource 'feishuConf.yaml'
    return readYaml(text: resource)
}

def call() {
    def feishuConf = loadFeishuConf()
    def body = [
        'app_id': appId,
        'app_secret': appSecret
    ]
    def response
    withCredentials([usernamePassword(credentialsId: feishuConf.credential, usernameVariable: 'APP_ID', passwordVariable: 'APP_SECRET')]) {
        response = httpRequest(
            url: tokenUrl,
            httpMode: 'POST',
            contentType: 'APPLICATION_JSON',
            requestBody: JsonOutput.toJson(body)
        )
    }
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
