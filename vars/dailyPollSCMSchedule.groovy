#!/usr/bin/env groovy

def call() {
    def schedule = '''
            0 4 * * *
            0 12 * * *
            0 18 * * *
        '''
    return env.BRANCH_NAME == 'master' ? schedule : ''
}
