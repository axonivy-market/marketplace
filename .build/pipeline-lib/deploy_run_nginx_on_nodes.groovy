private String escapeForDoubleQuotes(String value) {
    return (value ?: '')
        .replace('\\', '\\\\')
        .replace('"', '\\"')
        .replace('$', '\\$')
        .replace('`', '\\`')
}

def runNginxScriptOnNodes(List nodesToDeploy, String actionLabel, String scriptPath, Closure scriptArgumentsBuilder = null) {
    for (node in (nodesToDeploy ?: [])) {
        echo "${actionLabel} for node: ${node.name} (${node.ip})"

        withCredentials([
            string(credentialsId: node.credId, variable: 'EC2_NODE'),
            sshUserPrivateKey(
                credentialsId: 'aws-ec2-ssh-key',
                keyFileVariable: 'SSH_PRIVATE_KEY_FILE',
                usernameVariable: 'SSH_REMOTE_USER'
            )
        ]) {
            def nodeArgs = scriptArgumentsBuilder ? (scriptArgumentsBuilder(node) ?: []) : []
            def safeScriptPath = escapeForDoubleQuotes(scriptPath)
            def safeArgs = nodeArgs.collect { arg ->
                "\"${escapeForDoubleQuotes((arg ?: '').toString())}\""
            }
            def joinedArgs = safeArgs.join(" \\\n                                    ")

            sh """
                set +x
                export SSH_PRIVATE_KEY_FILE="\${SSH_PRIVATE_KEY_FILE}"
                export SSH_REMOTE_USER="\${SSH_REMOTE_USER}"
                bash \"${safeScriptPath}\" \\
                    "\${EC2_NODE}"${joinedArgs ? " \\\n                    ${joinedArgs}" : ""}
            """
        }
    }
}

return this
