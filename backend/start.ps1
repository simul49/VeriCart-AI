

# VeriCart Backend Startup Script
# Automatically frees port 8080 and starts the Spring Boot backend

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Kill any process already on port 8080
$existing = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($existing) {
    $existing | ForEach-Object {
        Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
        Write-Host "Stopped existing process (PID $($_.OwningProcess)) on port 8080"
    }
    Start-Sleep -Seconds 2
}

# Start backend
Set-Location $PSScriptRoot
$mvn = "$PSScriptRoot\maven\apache-maven-3.9.9\bin\mvn.cmd"
& $mvn spring-boot:run
