import groovy.json.JsonSlurperClassic

@NonCPS
def parseJsonData(String jsonContent) {
    return new JsonSlurperClassic().parseText(jsonContent)
}

def confirmProductionDeployment(String deploymentTarget, String releaseVersion) {
    if (deploymentTarget == 'PREVIEW node') {
        echo "Skipping approval for ${deploymentTarget}."
        return
    }

    def requester = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')?.first()?.userId ?: 'SYSTEM'
    def approverUsernames = []

    configFileProvider([configFile(fileId: 'deployment-approvers', variable: 'DEPLOYMENT_APPROVERS_FILE')]) {
        def approversContent = readFile(file: env.DEPLOYMENT_APPROVERS_FILE)
        def approversConfig = parseJsonData(approversContent)
        approverUsernames = approversConfig?.approvers ?: []
    }

    if (!approverUsernames) {
        error('No approvers configured. Please set at least one username in managed file deployment-approvers under key approvers.')
    }

    def submitterList = approverUsernames.join(',')
    def approver = input(
        message: "Deploy version ${releaseVersion} to ${deploymentTarget}?",
        submitter: submitterList,
        submitterParameter: 'APPROVED_BY',
        ok: 'Approve deployment'
    )

    if (approver == requester) {
        error("4-eyes policy violation: requester '${requester}' cannot self-approve. Approval must be done by another configured approver.")
    }

    echo "4-eyes approval passed: requested by '${requester}', approved by '${approver}' (allowed approvers: ${submitterList})."
}

return this
