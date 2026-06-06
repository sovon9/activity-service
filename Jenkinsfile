pipeline
{
    agent any
    stages
    {
        stage("Build Stage")
        {
            steps{
                echo "Build the java project"
                sh "chmod +x mvnw"
                sh "./mvnw clean install"
            }
        }
    }
}