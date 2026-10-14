# EDA HMAC Event Stream example rulebook and playbook

## Introduction

Certain enterprise services leverage HMAC hashing when making API calls; Event-Driven Ansible in Ansible Automation Platform can accept these calls qua event by using an Event Stream of type HMAC. To setup an HMAC Event Stream, we require the following: 

- EDA credential to store the HMAC secret
- An Event Stream using that secret
- A rulebook activation to take in the events sent to the Event Stream
- A job template to perform the work needed based on the received event payload

## EDA Credentials

### Source Control Credential Type

- Create a credential in EDA, using the Source Control credential type; this is to sync Projects in EDA (separate from Controller)
	* Name: [your credential name]
	* Organization: [a valid Organization]
	* Enter a username & password, or SCM private key & passphrase

### HMAC Event Stream Credential Type

- Create a credential in EDA, using the HMAC Event Stream credential type 
	* Name: [your credential name]
	* Organization: [a valid Organization]
	* Secret: the HMAC secret from the originating system
	* Hash algorithm: `sha256` is common; this should match the originating system
	* HMAC header key: `x-tfc-task-signature` is common for TFC "Run Task"
	* Signature encoding: `hex` is common for TFC "Run Task"

### Red Hat Ansible Automation Platform Credential Type

- Create a credential in EDA, using the RH AAP credential type; this is needed to launch jobs on the Controller
	* Name: [your credential name]
	* Organization: [a valid Organization]
	* Red Hat Ansible Automation Platform: **this is the fully qualified domain name of AAP, with /api/controller appended**, e.g., https://aap.apps.127.0.0.1.nip.io/api/controller
	* Enter a username & password, or OAuth token, that has access to run the target job template

## Project

- Create a Project in EDA (separate from Controller)
	* Name: [your Project name]
	* Organization: [a valid Organization]
	* Source control URL: a .git URL
	* Attach the Source Control credential created previously

## Event Stream

- Create an Event Stream of type HMAC
	* Name: [your Event Stream name]
	* Organization: [a valid Organization]
	* Choose `hmac` Event Stream type
	* Choose the Credential created in the previous step
	* Enter `*` for Headers for testing purposes; this can be removed later
	* Forward events to rulebook activation will be enabled by default

## Rulebook

### rulebooks/tfc_run_task_events.yml
	* This must reside in a `rulebooks/` subdirectory in the Project
	* Includes a simple debug statement for testing, commented out

## Rulebook Activation

- Create a rulebook activation in EDA using the tfc_run_task_events.yml rulebook
	* Name: [your rulebook activation name]
	* Organization: [a valid Organization]
	* Project: [the Project created previously]
	* Rulebook: Choose the tfc_run_task_events.yml rulebook from the dropdown
	* Event streams: Choose the HMAC event stream created previously
	* Credential: Choose the RH AAP credential created previously
	* Decision Environment: the "supported" or "minimal" DE will suffice, as it includes `ansible.eda` by default
	* Log level: for testing, choose Debug; do not run with Debug in Production
	* Options:
		* Skip audit events: you may wish to skip audit events in production, but not in testing
		* Auto-restart on Project update: if an update to the SCM project includes changes to this rulebook, it will automatically restart the rulebook activation
		* Enable event persistence: **this option requires database changes** 

## Playbook

### playbooks/tfc_demo.yml
- Requires "localhost" inventory in AAP to hit API
  * Alternatively hosts: localhost
- This playbook simply outputs specific key-value pairs that are contained in the sample event 

## Job Template

- Create a Job Template in Controller matching the value of `run_job_template.name` in `rulebooks/tfc_run_task_events.yml`
	* Name: [a name matching the invocation in the rulebook]
	* Job type: Run
	* Inventory: [an inventory that can target localhost]
	* Project: [a project which contains the playbook]
	* Playbook: Choose `playbooks/tfc_demo.yml` from the dropdown
	* Extra vars: **Important**, choose Prompt on launch, but do not enter any values; EDA job launches rely on this convention to populate the `event.payload` data

## Testing

- Included in this repo is a shell script `scripts/event_stream_test.sh` which will test this configuration
	* Set the `EDA_WEBHOOK_URL` variable to the Event Stream address displayed in the UI
	* Set the `HMAC_SECRET` variable to the value matching the HMAC credential type 
	* Set the `PAYLOAD` variable to some valid JSON; the playbook is hardcoded to display key/values matching the example data

- If this script runs successfully (no errors, `$?` of 0), you should see debug data in the Rulebook Activation history, showing the payload and headers (if configured with `*` as above)
- If the rulebook activation is configured to launch the Job Template, you should see the Action `run_job_template` under Rule Audit
- Click this Action to open the Job details in Controller and examine the Job Template run
