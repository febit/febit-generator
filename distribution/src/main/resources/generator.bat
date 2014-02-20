@echo off
cd /d %~dp0

call ./_.bat
java -cp "%CLSPATH%" %MAIN_CLASS% gen "%PROPS%"
