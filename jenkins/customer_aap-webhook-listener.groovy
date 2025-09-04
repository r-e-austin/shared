// .groovy Jenkinsfile for a "listener" which takes webhooks and notifies originating job
import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.net.URLEncoder
pipeline {
    agent any
    
    // These parameters are populated by the Generic Webhook Trigger plugin
    // JSONPath style, $.status, $.extra_vars, etc.
    parameters {
        string(name: 'aapJobStatus', defaultValue: '', description: 'Status from AAP webhook')
        // extra_vars comes back as a string, even though it's formatted as a dictionary?
        string(name: 'extraVarsString', defaultValue: '{}', description: 'The extra_vars field from AAP, as a string')
    }

    stages {
        stage('Process Webhook') {
            steps {
                script {
                    echo "Webhook received!"
                    echo "AAP Job Status: ${params.aapJobStatus}"
                    echo "Received extra_vars string: ${params.extraVarsString}"

                    // Parse the extra_vars string into a Groovy object
                    def extraVars = new JsonSlurperClassic().parseText(params.extraVarsString)
                    
                    // Now you can access the properties of the parsed object as JSON
                    // originally this was just a JSONPath $.extra_vars.jenkins_build_tag, 
                    // but extra_vars does not come through as a dictionary, just a string
                    def jenkinsBuildTag = extraVars.jenkins_build_tag

                    if (jenkinsBuildTag == null || jenkinsBuildTag.isEmpty()) {
                        error("Webhook received without a jenkins_build_tag inside extra_vars. Cannot proceed.")
                    }

                    echo "Found Build Tag: ${jenkinsBuildTag}"

                    // The BUILD_TAG format is "jenkins-JOB_NAME-BUILD_NUMBER"
                    // We need to parse it to get the job name and build number.
                    def parts = jenkinsBuildTag.split('-')
                    def jobName = parts[1..-2].join('-') // Handles job names with hyphens
                    def buildNumber = parts[-1]

                    echo "Approving input for Job: '${jobName}', Build: #${buildNumber}"
                    // When you define an input in a pipeline, the REST API URL gets capitalized
                    // This can be seen most clearly in the developer console of the browser,
                    // but here is the code where this is happening:
                    // https://github.com/jenkinsci/pipeline-input-step-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/support/steps/input/InputStep.java#L104
                    def approvalUrl = "${env.JENKINS_URL}job/${jobName}/${buildNumber}/input/Aap-approval-gate/submit"
                    
                    // Create the JSON for the parameters as a map
                        def paramsMap = [
                            parameter: [
                                [name: 'aapJobStatus', value: aapJobStatus]
                            ]
                        ]
                        def paramsJson = JsonOutput.toJson(paramsMap)
                        
                        // Jenkins API expects a form submission, including the act of pushing the 'proceed' button
                        // This is poorly documented, again most easily seen in developer console of the browser
                        def payloadParts = [
                            "json=${URLEncoder.encode(paramsJson, 'UTF-8')}",
                            "proceed=Proceed"
                        ]
                        def approvalPayload = payloadParts.join('&')
                    
                    withCredentials([usernamePassword(credentialsId: 'jenkins-api-token', usernameVariable: 'USERNAME', passwordVariable: 'API_TOKEN')]) {
                        // A "crumb" is not needed when using an API token
                        //def crumbIssuerUrl = "${env.JENKINS_URL}/crumbIssuer/api/json"
                        //def crumbResponse = httpRequest(
                        //    url: crumbIssuerUrl,
                        //    authentication: 'jenkins-api-token'
                        //)
                        //def crumbData = new JsonSlurperClassic().parseText(crumbResponse.content)
                        //def crumbHeaderName = crumbData.crumbRequestField
                        //def crumbValue = crumbData.crumb
                        //echo "Successfully fetched CSRF crumb."

                        // Send the approval request
                        httpRequest(
                            url: approvalUrl,
                            httpMode: 'POST',
                            authentication: 'jenkins-api-token',
                            contentType: 'APPLICATION_FORM',
                            requestBody: approvalPayload,
                            quiet: true
                        )
                    }
                    
                    echo "Approval sent."
                }
            }
        }
    }
}