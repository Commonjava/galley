node ("maven") {
   //def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git branch: '${branch}', url: 'https://github.com/Commonjava/${project}.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      //mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      sh "mvn -DskipTests -X clean verify"
   }
}