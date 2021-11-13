rem MAIN_CLASS=org.springframework.boot.loader.JarLauncher
rem JAVA_HOME=jre1.8.0_66
set JAVA_CMD=%JAVA_HOME%\bin\java
set JAVA_OPTS=-Xmx512m -Xms512m -Xmn256m -Xss256k
set CONFIG_FILE=../config/application.yml
set TARGET=../lib/kafka-console-ui.jar
set DATA_DIR=..
%JAVA_CMD% -jar %TARGET% --spring.config.location=%CONFIG_FILE% --data.dir=%DATA_DIR%