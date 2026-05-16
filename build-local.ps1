$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$maven = Join-Path $repoRoot "..\tools\apache-maven-3.9.9\bin\mvn.cmd"
$mavenRepo = Join-Path $repoRoot "..\tools\m2-repository"

if (-not (Test-Path $maven)) {
    throw "Local Maven was not found at: $maven"
}

& $maven -B "-Dmaven.repo.local=$mavenRepo" package --file (Join-Path $repoRoot "pom.xml") @args
exit $LASTEXITCODE
