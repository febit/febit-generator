@echo off
cd /d %~dp0

if '%1=='## goto ENVSET 

rem set JAVA_HOME=K:\java\jdk1.6.0_21

set PATH=%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin
set MAIN_CLASS=webit.generator.core.Main
set PROPS=%~dp0generator.props

rem 设定CLSPATH
SET CLSPATH=.;%JAVA_HOME%\lib\dt.jar
FOR %%c IN (./lib/*.jar) DO CALL %0 ## %%c  

rem 运行  
GOTO END

:ENVSET  
set CLSPATH=%CLSPATH%;%~dp0lib\%2 
goto END  

:END  


