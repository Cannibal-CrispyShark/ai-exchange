@echo off
echo 启动前端服务...
cd /d %~dp0
if not exist node_modules (
    echo 正在安装依赖...
    call npm install
)
call npm run dev
pause
