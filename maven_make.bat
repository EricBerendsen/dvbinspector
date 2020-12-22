echo "maven build============================================="
@REM set CUR_PATH=%cd%
@REM set RESOURCES_PATH=src/main/resources
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-hotspot
@REM native2ascii -encoding utf8 %RESOURCES_PATH%/res_zh_CN_src.properties %RESOURCES_PATH%/res_zh_CN.properties
mvn package -f pom.xml -Dmaven.test.skip=true
