import groovy.json.JsonSlurperClassic

@NonCPS
def parseJsonData(String jsonContent) {
    return new JsonSlurperClassic().parseText(jsonContent)
}

@NonCPS
def resolveNodesToDeploy(String deploymentTarget, Map deploymentConfig) {
    return deploymentConfig[deploymentTarget]
}

def loadNodesToDeploy(String deploymentTarget) {
    def deploymentConfig = [:]
    def nodesToDeploy = []

    configFileProvider([configFile(fileId: 'deployment-target', variable: 'DEPLOYMENT_TARGETS_FILE')]) {
        def jsonContent = readFile(file: env.DEPLOYMENT_TARGETS_FILE)
        deploymentConfig = parseJsonData(jsonContent)
        nodesToDeploy = resolveNodesToDeploy(deploymentTarget, deploymentConfig) ?: []
    }

    echo "Resolved ${nodesToDeploy?.size() ?: 0} node(s) for ${deploymentTarget}"
    return nodesToDeploy
}

return this
