#!/bin/bash

# 设置jvm堆大小及栈大小
JAVA_MEM_OPTS="-Xmx512m -Xms512m -Xmn256m -Xss256k"

# 获取脚本真实路径（兼容Linux和macOS）
if [ -L "$0" ]; then
    # 处理符号链接
    if command -v greadlink >/dev/null 2>&1; then
        # macOS使用greadlink（需brew install coreutils）
        SCRIPT_PATH=$(greadlink -f "$0")
    else
        # Linux使用readlink
        SCRIPT_PATH=$(readlink -f "$0")
    fi
else
    SCRIPT_PATH="$0"
fi

# 如果上述方法失败（如macOS无greadlink），使用替代方案
if [ -z "$SCRIPT_PATH" ] || [ ! -f "$SCRIPT_PATH" ]; then
    # 使用perl跨平台解决方案
    SCRIPT_PATH=$(perl -e 'use Cwd "abs_path"; print abs_path(shift)' "$0" 2>/dev/null)
fi

# 最终回退方案
if [ -z "$SCRIPT_PATH" ] || [ ! -f "$SCRIPT_PATH" ]; then
    SCRIPT_PATH="$0"
fi

# 计算项目根目录
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")
PROJECT_DIR=$(cd "$SCRIPT_DIR" && cd .. && pwd)
CONF_FILE="$PROJECT_DIR/config/application.yml"
TARGET="$PROJECT_DIR/lib/kafka-console-ui.jar"

# 设置h2文件根目录
DATA_DIR="$PROJECT_DIR"

# 这个是错误输出，如果启动命令有误，输出到这个文件，应用日志不会输出到error.out，应用日志输出到上面的kafka-console-ui.log中
ERROR_OUT="$PROJECT_DIR/error.out"
# 日志目录
LOG_HOME="$PROJECT_DIR"
PROCESS_FLAG="kafka-console-ui-process-flag:${PROJECT_DIR}"

JAVA_OPTS="$JAVA_OPTS $JAVA_MEM_OPTS -Dfile.encoding=utf-8"

# 启动应用
nohup java -jar $JAVA_OPTS "$TARGET" \
    --spring.config.location="$CONF_FILE" \
    --logging.home="$LOG_HOME" \
    --data.dir="$DATA_DIR" \
    "$PROCESS_FLAG" >/dev/null 2>"$ERROR_OUT" &

echo "Kafka-console-ui Started! PID: $!"