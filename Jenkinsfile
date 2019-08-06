pipeline {
   agent { label 'maven' }
   stages {
      stage('Build') {
         when {
             expression { env.CHANGE_ID != null }  // Pull request
         }
         steps {
            withMaven(maven:'maven-3', jdk:'java-8', mavenLocalRepo: '.repository') {
               sh 'mvn -DskipTests -B clean verify'
            }
         }
      }
   }
}