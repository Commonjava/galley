node ("maven") {
   def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git branch: 'master-failed-test', url: 'https://github.com/ruhan1/galley.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      sh "mvn -DskipTests -B clean verify"
   }
}