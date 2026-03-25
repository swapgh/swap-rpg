param(
    [string]$TargetZip = ''
)

$ErrorActionPreference = 'Stop'

$rootDir = Split-Path -Parent $PSScriptRoot
$distRoot = Join-Path $rootDir 'dist'
$distDir = Join-Path $distRoot 'demo'
$buildDir = Join-Path $rootDir 'build_classes'
$sourcesFile = Join-Path $rootDir 'build_sources.txt'

if ([string]::IsNullOrWhiteSpace($TargetZip)) {
    $TargetZip = Join-Path $rootDir '..\Web Page\www\assets\downloads\demo.zip'
}

if (Test-Path $distDir) {
    Remove-Item -Recurse -Force $distDir
}

New-Item -ItemType Directory -Path (Join-Path $distDir 'Swap') -Force | Out-Null

Get-ChildItem -Recurse -Filter *.java (Join-Path $rootDir 'Swap\src') |
    Sort-Object FullName |
    ForEach-Object { $_.FullName } |
    Set-Content $sourcesFile

if (Test-Path $buildDir) {
    Remove-Item -Recurse -Force $buildDir
}
New-Item -ItemType Directory -Path $buildDir -Force | Out-Null

cmd /c "javac --release 17 -d `"$buildDir`" @`"$sourcesFile`""
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

Copy-Item $buildDir -Destination $distDir -Recurse
Copy-Item (Join-Path $rootDir 'Swap\res') -Destination (Join-Path $distDir 'Swap') -Recurse
Copy-Item (Join-Path $rootDir 'README.md') -Destination (Join-Path $distDir 'README.md')

@'
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
java -cp build_classes:Swap/res app.Main
'@ | Set-Content (Join-Path $distDir 'run.sh')

@'
@echo off
cd /d %~dp0
java -cp "build_classes;Swap/res" app.Main
pause
'@ | Set-Content (Join-Path $distDir 'run.bat')

if (Test-Path $TargetZip) {
    Remove-Item -Force $TargetZip
}

Compress-Archive -Path $distDir -DestinationPath $TargetZip
Write-Output "Demo package created at: $TargetZip"
