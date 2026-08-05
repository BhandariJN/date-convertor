# One step: build jar -> app-image -> setup.exe
$ErrorActionPreference = "Stop"
$iscc = "C:\Users\Dell\AppData\Local\Programs\Inno Setup 6\ISCC.exe"

Write-Host "[1/3] Building jar..."
.\mvnw.cmd -q -DskipTests package
if ($LASTEXITCODE -ne 0) { throw "maven build failed" }

Write-Host "[2/3] Packaging app image..."
if (Test-Path dist) { Remove-Item -Recurse -Force dist }
jpackage --type app-image --input target --main-jar dateconvertor_java-0.0.1-SNAPSHOT.jar --main-class org.springframework.boot.loader.launch.JarLauncher --name DateConvertor --dest dist --app-version 1.0.0 --icon icon.ico
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

Write-Host "[3/3] Building setup.exe..."
& $iscc installer.iss | Out-Null
if ($LASTEXITCODE -ne 0) { throw "iscc failed" }

Write-Host "Done -> D:\DATE CONVERTOR\DateConvertor-Setup.exe"
