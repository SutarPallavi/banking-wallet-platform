@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
SET WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties

IF NOT EXIST "%WRAPPER_PROPERTIES%" (
  ECHO Cannot find %WRAPPER_PROPERTIES%
  EXIT /B 1
)

FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
  IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
)

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading Maven Wrapper JAR from %WRAPPER_URL%
  IF EXIST "%ProgramFiles%\Git\usr\bin\curl.exe" (
    "%ProgramFiles%\Git\usr\bin\curl.exe" -fsSL -o "%WRAPPER_JAR%" "%WRAPPER_URL%"
  ) ELSE IF EXIST "%ProgramFiles%\Git\mingw64\bin\wget.exe" (
    "%ProgramFiles%\Git\mingw64\bin\wget.exe" -q -O "%WRAPPER_JAR%" "%WRAPPER_URL%"
  ) ELSE (
    powershell -Command "& { $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' }"
  )
)

SET JAVA_EXE=java.exe
IF DEFINED JAVA_HOME (
  IF EXIST "%JAVA_HOME%\bin\java.exe" SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
