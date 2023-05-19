#!/bin/bash

SCRIPT_DIR=$(dirname "`pwd`/$0")
PROJECT_DIR=`dirname "$SCRIPT_DIR"`
# 不要修改进程标记，作为进程属性关闭使用
PROCESS_FLAG="kafka-console-ui-process-flag:${PROJECT_DIR}"
pkill -f $PROCESS_FLAG
echo 'Stop Kafka-console-ui!'