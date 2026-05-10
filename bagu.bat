@echo off
chcp 65001 >nul
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar "%~dp0target\bagu-cli-1.0.0.jar" %*
