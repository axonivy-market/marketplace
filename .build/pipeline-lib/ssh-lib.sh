#!/usr/bin/env bash
# Shared SSH helpers for scripts that connect to remote nodes.

# Populates SSH_USER and SSH_OPTS from environment for remote operations.
setup_ssh_opts() {
    SSH_USER="${SSH_REMOTE_USER:-ec2-user}"
    SSH_OPTS=( -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o UserKnownHostsFile=~/.ssh/known_hosts )
    if [[ -n "${SSH_PRIVATE_KEY_FILE:-}" ]]; then
        SSH_OPTS+=( -i "${SSH_PRIVATE_KEY_FILE}" )
    fi
}
