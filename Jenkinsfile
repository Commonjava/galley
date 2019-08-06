pipeline {
   agent { maven }
   stages {
      stage('Build') {
         when {
             expression { env.CHANGE_ID != null }  // Pull request
         }
         withMaven() {
            sh "mvn -DskipTests -B clean verify"
         }
      }
   }
}