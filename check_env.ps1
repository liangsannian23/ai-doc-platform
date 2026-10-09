# 环境自检：一次性检查后端/前端依赖是否在跑
# 用法：在 PowerShell 里执行  powershell -ExecutionPolicy Bypass -File check_env.ps1
#   或  Get-Content check_env.ps1 -Raw | Invoke-Expression

$ErrorActionPreference = 'SilentlyContinue'

# 需要探测的端口：名称 / 端口 / 用途 / 不通时怎么办
$targets = @(
  @{ Name = 'MySQL';     Port = 3306; Role = '数据库(硬依赖)';       Fix = '启动 MySQL 服务：Get-Service *mysql* | Start-Service' },
  @{ Name = 'Redis';     Port = 6379; Role = '缓存/限流(登录硬依赖)'; Fix = '开 Docker Desktop 后 docker start aidoc-redis；或 WSL sudo service redis-server start' },
  @{ Name = 'RabbitMQ';  Port = 5672; Role = '异步通知';             Fix = '启动服务：Start-Service RabbitMQ（不影响登录）' },
  @{ Name = '后端应用';   Port = 8080; Role = 'Spring Boot';          Fix = 'IDEA 运行 AiDocPlatformApplication，或 docker start aidoc' },
  @{ Name = '前端Dev';    Port = 5173; Role = 'Vite 开发服务器';       Fix = 'cd frontend; npm run dev' }
)

function Test-Port($port) {
  # 1) 权威：本机监听表（避免 CanConnect 假阴性）
  if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
    return $true
  }
  # 2) 兜底：真正建一次 TCP 连接（必须 EndConnect），覆盖少数不在 Listen 表的情况
  $client = New-Object System.Net.Sockets.TcpClient
  try {
    $iar = $client.BeginConnect('127.0.0.1', $port, $null, $null)
    if ($iar.AsyncWaitHandle.WaitOne(800, $false)) {
      $client.EndConnect($iar)
      return $true
    }
    return $false
  } catch {
    return $false
  } finally {
    $client.Close()
  }
}

Write-Host ''
Write-Host '================ 环境自检 ================' -ForegroundColor Cyan
$fail = 0
foreach ($t in $targets) {
  $up = Test-Port $t.Port
  if ($up) {
    Write-Host ('[ OK ]  {0,-9} :{1,-5} {2}' -f $t.Name, $t.Port, $t.Role) -ForegroundColor Green
  } else {
    $fail++
    Write-Host ('[DOWN]  {0,-9} :{1,-5} {2}' -f $t.Name, $t.Port, $t.Role) -ForegroundColor Red
    Write-Host ('        └─ 修复：' + $t.Fix) -ForegroundColor DarkGray
  }
}
Write-Host '=========================================' -ForegroundColor Cyan
if ($fail -eq 0) {
  Write-Host '全部就绪，可以登录使用了。' -ForegroundColor Green
} else {
  Write-Host ("有 {0} 项未就绪，按上面『修复』提示处理后重跑本脚本。" -f $fail) -ForegroundColor Yellow
}
Write-Host ''
