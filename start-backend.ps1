# 校园二手交易平台后端启动脚本
# 由 start-backend.cmd 调用：预检查端口/依赖 -> 启动 Spring Boot -> 全量日志落盘
$ErrorActionPreference = 'Continue'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = Join-Path $root '.tools\jdk-17.0.21+9'
$mvn = Join-Path $root '.tools\apache-maven-3.9.16\bin\mvn.cmd'
$settings = Join-Path $root '.tools\maven-settings.xml'
$logDir = Join-Path $root 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$log = Join-Path $logDir ('startup-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')

$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'

# 从项目根目录的 .env.local 读取敏感配置（该文件已加入 .gitignore，不会提交）
$envFile = Join-Path $root '.env.local'
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*DEEPSEEK_API_KEY\s*=\s*(.+)$') {
            $env:DEEPSEEK_API_KEY = $matches[1].Trim().Trim('"').Trim("'")
        }
    }
}
if (-not $env:DEEPSEEK_API_KEY) {
    Write-Host "[提示] 未设置 DEEPSEEK_API_KEY，AI 接口将返回模拟文案" -ForegroundColor Yellow
}

# 系统 %TEMP% 在本机对普通进程可能拒绝写入（Tomcat 建临时目录会失败），
# 统一改用项目内目录，保证双击启动和脚本启动行为一致。
$tmpDir = Join-Path $root '.run\temp'
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
$env:TEMP = $tmpDir
$env:TMP = $tmpDir

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " 校园二手交易平台 - 后端启动" -ForegroundColor Cyan
Write-Host " 日志文件: $log" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# ---------- 1. 预检：8080 是否被占用 ----------
$busy = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
if ($busy) {
    $owner = ($busy | Select-Object -First 1).OwningProcess
    $proc = Get-Process -Id $owner -ErrorAction SilentlyContinue
    Write-Host "[错误] 8080 端口已被占用，PID=$owner ($($proc.ProcessName))" -ForegroundColor Red
    Write-Host "       多半是已经启动过一个实例。可运行 stop-backend.cmd 先停掉它。" -ForegroundColor Yellow
    Read-Host "按回车键关闭窗口"
    exit 1
}

# ---------- 2. 预检：MySQL / Redis 是否可连 ----------
foreach ($port in 3306, 6379) {
    $ok = Test-NetConnection -ComputerName 127.0.0.1 -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
    if (-not $ok) {
        $name = if ($port -eq 3306) { 'MySQL' } else { 'Redis' }
        Write-Host "[错误] 端口 $port ($name) 连不上：容器没启动或 Docker Desktop 没开。" -ForegroundColor Red
        Write-Host "       解决办法：打开 Docker Desktop，等它变成 Running，然后在 D:\campus-market 执行：" -ForegroundColor Yellow
        Write-Host "                 docker compose up -d" -ForegroundColor Yellow
        Read-Host "按回车键关闭窗口"
        exit 1
    }
}
Write-Host "[预检] 8080 空闲，MySQL(3306) 与 Redis(6379) 可连接" -ForegroundColor Green
Write-Host "[启动] 正在启动 Spring Boot（首次可能稍慢），日志同时写入上面的文件..." -ForegroundColor Green
Write-Host ""

# ---------- 3. 启动，并同时输出到控制台和日志文件 ----------
Set-Location (Join-Path $root 'backend')
& $mvn '-s' $settings spring-boot:run 2>&1 | ForEach-Object { "$_" } | Tee-Object -FilePath $log
$exitCode = $LASTEXITCODE

Write-Host ""
if ($exitCode -ne 0) {
    Write-Host "[失败] 启动失败，退出码 $exitCode" -ForegroundColor Red
    Write-Host "       完整日志: $log" -ForegroundColor Yellow
    Write-Host "       日志最后 30 行（把这段发给我即可定位）:" -ForegroundColor Yellow
    Write-Host "--------------------------------------------" -ForegroundColor DarkGray
    Get-Content $log -Tail 30 -ErrorAction SilentlyContinue
    Write-Host "--------------------------------------------" -ForegroundColor DarkGray
} else {
    Write-Host "[退出] 应用已停止。" -ForegroundColor Yellow
}
Read-Host "按回车键关闭窗口"
