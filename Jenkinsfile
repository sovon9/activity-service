pipeline {
    agent any
    stages {
        stage("Build Stage") {
            steps {
                // This block securely accesses the 'github' credential by its ID
                // and maps its parts to environment variables.
                withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
                    echo "Build the java project"
                    sh "chmod +x mvnw"

                    // Inside this block, GITHUB_USERNAME and GITHUB_TOKEN are now available
                    // as environment variables for your settings.xml file to use.
                    sh "./mvnw clean install -s settings.xml"
                }
            }
        }
    }
}