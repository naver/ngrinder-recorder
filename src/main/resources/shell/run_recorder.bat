@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java  -jar ngrinder-recorder-${project.version}.jar
