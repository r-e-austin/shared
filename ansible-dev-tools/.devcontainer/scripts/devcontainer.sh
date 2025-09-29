#!/usr/bin/env bash
set -e 

echo "Kicking off postStartCommamd devcontainer.sh" 

### Install certificates from .devcontainer/certs
cp ./.devcontainer/certs/custom-ca.crt /etc/pki/ca-trust/source/anchors/custom-ca.crt && update-ca-trust

### Install collections from project ./collections/requirements.yml

### Add environment variables for token processing via podman secrets
### https://docs.ansible.com/ansible/latest/collections_guide/collections_installing.html#configuring-the-ansible-galaxy-client
### https://docs.podman.io/en/latest/markdown/podman-secret-create.1.html#examples

### ANSIBLE_GALAXY_SERVER_LIST=SERVERNAME
###     If more than 1 server, comma-delimited
### ANSIBLE_GALAXY_SERVER_SERVERNAME_URL=https://servername/pulp_ansible/galaxy/rh-certified/
###     Duplicate for additional servers if needed
### ANSIBLE_GALAXY_SERVER_SERVERNAME_TOKEN=0123456789abcdef
###     Duplicate for additional servers if needed

### Add podman secrets to container runtime in devcontainer.json runArgs
### https://docs.podman.io/en/latest/markdown/podman-run.1.html#secret-secret-opt-opt

if [[ -r ./collections/requirements.yml ]]; then
    ansible-galaxy install -r ./collections/requirements.yml -c
fi

if [[ -r ./roles/requirements.yml ]]; then
    ansible-galaxy install -r ./roles/requirements.yml -c
fi
