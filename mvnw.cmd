@echo off
setlocal
set DIR=%~dp0

rem Execute Maven Wrapper jar
java -jar "%DIR%.mvn\wrapper\maven-wrapper.jar" %*
