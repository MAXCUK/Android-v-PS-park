@ECHO OFF
SET DIR=%~dp0
SET JAVA_CMD=%JAVA_HOME%\bin\java.exe
IF NOT EXIST "%JAVA_CMD%" SET JAVA_CMD=java
"%JAVA_CMD%" -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
