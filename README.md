**By default applications will read credentials file from system environment property GOOGLE_APPLICATION_CREDENTIALS.<br>**
**If you want to specify custom credentials file you should use optional applications run parameter --credentialsFile /path/to/credentials_file.json<br>**

task1 run command:<br>
`java -jar task1-1.0-jar-with-dependencies.jar --projectId <project> --datasetId <dataset> --prefix <prefix> --count <count>`

task2 run command:<br>
`java -jar task2-1.0-jar-with-dependencies.jar --projectId <project> --datasetId <dataset>`
