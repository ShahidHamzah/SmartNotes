Notes Desktop Application

Demonstrates how to structure a multi-project build in Gradle.
It includes a main JavaFX project in the application/ folder,
a console application in console/, and a shared library 
in the shared/ folder which extracts system information (like
the name of the current user). 

Supported gradle tasks:

| Tasks   | Description                                          |
|:--------|:-----------------------------------------------------|
| clean   | Remove build/ directory                              |
| build   | Build the application project in build/ directory    |
| run     | Run the application or console project               |
| distZip | Create run scripts in application/build/distribution |
| distTar | Create run scripts in application/build/distribution |