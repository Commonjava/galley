pipeline {
   agent ("maven")
   def mvnHome
   stages {
      stage('Preparation') { // for display purposes
         // Get some code from a GitHub repository
         //git url: 'https://github.com/ruhan1/galley.git'
         // Get the Maven tool.
         // ** NOTE: This 'M3' Maven tool must be configured
         // **       in the global configuration.           
         mvnHome = tool 'M3'
      }
      stage('Build') {
         when {
             expression { env.CHANGE_ID != null }  // Pull request
         }
         // Run the maven build
         sh "mvn -DskipTests -B clean verify"
      }
   }
}