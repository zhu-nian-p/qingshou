# 停止 8080 端口上的后端进程（新手友好：双击即可）
$ErrorActionPreference = 'Continue'
$busy = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
if (-not $busy) {
    Write-Host "8080 端口上没有正在运行的程序，无需停止。" -ForegroundColor Green
    Read-Host "按回车键关闭窗口"
    exit 0
}
$pids = $busy | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($procId in $pids) {
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($proc) {
        Write-Host "正在停止 PID=$procId ($($proc.ProcessName)) ..." -ForegroundColor Yellow
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
}
Start-Sleep -Seconds 2
if (Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue) {
    Write-Host "[失败] 8080 仍被占用，可能需要在管理员终端里结束进程。" -ForegroundColor Red
} else {
    Write-Host "[完成] 8080 已释放。" -ForegroundColor Green
}
Read-Host "按回车键关闭窗口"
