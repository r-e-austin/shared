// .groovy Jenkinsfile for launching AAP Job

pipeline {
    agent any
    environment {
        AAP_URL        = 'https://aap-host' 
        TEMPLATE_ID    = '12'
        CREDENTIALS_ID = 'aap-admin-token'
        LIMIT          = 'target-server'
    }

    stages {
        stage('1. Prepare for Launch') {
            steps {
                script {
                    echo "-------------------------------------------"
                    echo "AAP Controller URL: ${env.AAP_URL}"
                    echo "Job Template ID: ${env.TEMPLATE_ID}"
                    echo "Jenkins Build Tag: ${env.BUILD_TAG}"
                    echo "Job limit: ${env.LIMIT}"
                    echo "-------------------------------------------"
                }
            }
        }

        stage('2. Launch AAP Job Template') {
            steps {
                withCredentials([string(credentialsId: env.CREDENTIALS_ID, variable: 'aapToken')]) {
                    script {
                        def launchUrl = "${env.AAP_URL}/api/controller/v2/job_templates/${env.TEMPLATE_ID}/launch/"

                        def payload = """
                        {
                            "limit": "${env.LIMIT}",
                            "extra_vars": "{ jenkins_build_tag: ${env.BUILD_TAG} }"
                        }
                        """

                        echo "Sending POST request to: ${launchUrl}"
                        echo "Payload: ${payload}"

                        try {
                            def response = httpRequest(
                                url: launchUrl,
                                httpMode: 'POST',
                                // Concatenate to avoid string interpolation
                                customHeaders: [
                                    [name: 'Authorization', value: 'Bearer ' + aapToken],
                                    [name: 'Content-Type', value: 'application/json']
                                ],
                                requestBody: payload,
                                quiet: false, // Set to 'true' for production / non-troubleshooting
                                ignoreSslErrors: true // Set to 'false' for production environment with real certs
                            )

                            echo "Success!"
                            echo "Response Status: ${response.status}"
                            echo "Response Content: ${response.content}"

                        } catch (Exception e) {
                            echo "ERROR: Failed to launch AAP Job."
                            echo "Exception: ${e.getMessage()}"
                            currentBuild.result = 'FAILURE'
                            error("Aborting pipeline due to API error.")
                        }
                    }
                }
            }
        }
        
       stage('3. Wait for AAP Webhook') {
            steps {
                script {
                        
                            def approval = input(
                                id: 'aap-approval-gate',
                                message: "Waiting for AAP job completion webhook...",
                                parameters: [
                                    string(name: 'aapJobStatus', defaultValue: 'failed', description: 'Final status from the AAP job')
                                ]
                            )
                            
                            // When the input is approved, continue from here
                            echo "Webhook received! Resuming pipeline."
                            echo "Final AAP Job Status: ${approval}"
                            // Do stuff
                            if (approval != 'successful') {
                                error("AAP Job did not complete successfully. Final status: ${approval}")
                            }
                        
                    }
                }
            }
        }
    }