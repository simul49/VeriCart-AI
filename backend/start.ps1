

# VeriCart Backend Startup Script
# Automatically frees port 8080 and starts the Spring Boot backend

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Load .env (git-ignored) into the process environment so Spring Boot picks up
# the real LLM keys and AI_MOCK_ENABLED without committing secrets to source.
if (Test-Path "$PSScriptRoot\.env") {
    Get-Content "$PSScriptRoot\.env" | ForEach-Object {
        if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$') {
            $k = $matches[1].Trim()
            $v = $matches[2].Trim().Trim('"').Trim("'")
            if ($v -ne '') { Set-Item -Path "env:$k" -Value $v }
        }
    }
    Write-Host "Loaded environment variables from .env"
}

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
