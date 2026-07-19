param(
    [int]$Port = 8080
)

$listenerProcessIds = @(
    Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
)

foreach ($listenerProcessId in $listenerProcessIds) {
    $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $listenerProcessId" -ErrorAction Stop
    $commandLine = [string]$processInfo.CommandLine

    if ($commandLine -notmatch 'org\.akhikhl\.gretty\.Runner') {
        throw "Port $Port is used by $($processInfo.Name) (PID $listenerProcessId), not Gretty. Refusing to stop it."
    }

    Write-Host "Stopping stale TeaVM/Gretty server on port $Port (PID $listenerProcessId)."
    Stop-Process -Id $listenerProcessId -Force -ErrorAction Stop
    Wait-Process -Id $listenerProcessId -Timeout 5 -ErrorAction SilentlyContinue
}

