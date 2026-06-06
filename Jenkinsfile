pipeline
{
    agent any

    environment {
        // Securely injects the PAT from Jenkins Credentials
        GITHUB_TOKEN = credentials('github-token')
        // Your GitHub username
        GITHUB_USERNAME = 'sovon9' 
    }

    stages {
        stage("Build Stage") {
            steps {
                echo "Build the java project"
                sh "chmod +x mvnw"
                // The -s flag tells Maven to use your custom settings file
                sh "./mvnw clean install -s settings.xml"
            }
        }
    }
}