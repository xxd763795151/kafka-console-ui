#!/bin/bash

# 获取脚本真实路径（兼容Linux和macOS）
if [ -L "$0" ]; then
    # 处理符号链接
    if command -v greadlink >/dev/null 2>&1; then
        SCRIPT_PATH=$(greadlink -f "$0")
    elif command -v readlink >/dev/null 2>&1; then
        SCRIPT_PATH=$(readlink -f "$0")
    else
        # 使用perl作为备选
        SCRIPT_PATH=$(perl -e 'use Cwd "abs_path"; print abs_path(shift)' "$0" 2>/dev/null)
    fi
else
    SCRIPT_PATH="$0"
fi

# 最终回退方案
if [ -z "$SCRIPT_PATH" ] || [ ! -f "$SCRIPT_PATH" ]; then
    SCRIPT_PATH="$0"
fi

# 计算项目根目录
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")
PROJECT_DIR=$(cd "$SCRIPT_DIR" && cd .. && pwd 2>/dev/null)

# 验证项目目录是否存在
if [ ! -d "$PROJECT_DIR" ]; then
    echo "ERROR: Failed to determine project directory!" >&2
    exit 1
fi

# 不要修改进程标记，作为进程属性关闭使用
PROCESS_FLAG="kafka-console-ui-process-flag:${PROJECT_DIR}"

# 停止前检查进程是否存在
if pgrep -f "$PROCESS_FLAG" >/dev/null; then
    # 先尝试正常停止
    pkill -f "$PROCESS_FLAG"

    # 等待进程退出
    TIMEOUT=10
    while [ $TIMEOUT -gt 0 ]; do
        if ! pgrep -f "$PROCESS_FLAG" >/dev/null; then
            break
        fi
        sleep 1
        TIMEOUT=$((TIMEOUT - 1))
    done

    # 检查是否仍有进程存在
    if pgrep -f "$PROCESS_FLAG" >/dev/null; then
        # 强制终止
        pkill -9 -f "$PROCESS_FLAG"
        echo "Stop Kafka-console-ui! (force killed)"
    else
        echo "Stop Kafka-console-ui! (gracefully stopped)"
    fi
else
    echo "Kafka-console-ui is not running."
fi