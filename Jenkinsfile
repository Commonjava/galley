pipeline {
   agent { label 'maven' }
   stages {
      stage('Build') {
         when {
             expression { env.CHANGE_ID != null }  // Pull request
         }
         steps {
            sh 'mvn -DskipTests -B clean verify'
         }
      }
   }
}