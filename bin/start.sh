#!/bin/bash

# 设置jvm堆大小及栈大小，栈大小最少设置为256K，不要小于这个值，比如设置为128，太小了
JAVA_MEM_OPTS="-Xmx512m -Xms512m -Xmn256m -Xss256k"

SCRIPT_DIR=`dirname $0`
PROJECT_DIR="$SCRIPT_DIR/.."
CONF_FILE="$PROJECT_DIR/config/application.yml"
TARGET="$PROJECT_DIR/lib/kafka-console-ui.jar"

#设置h2文件根目录
DATA_DIR=$PROJECT_DIR

# 日志目录，默认为当前工程目录下
# 这个是错误输出，如果启动命令有误，输出到这个文件，应用日志不会输出到error.out，应用日志输出到上面的rocketmq-reput.log中
ERROR_OUT="$PROJECT_DIR/error.out"
# 不要修改进程标记，作为进程属性关闭使用，如果要修改，请把stop.sh里的该属性的值保持一致
PROCESS_FLAG="kafka-console-ui-process-flag:${PROJECT_DIR}"

JAVA_OPTS="$JAVA_OPTS $JAVA_MEM_OPTS"

nohup java -jar $JAVA_OPTS $TARGET --spring.config.location="$CONF_FILE" --logging.home="$PROJECT_DIR" --data.dir=$DATA_DIR $PROCESS_FLAG 1>/dev/null 2>$ERROR_OUT &

echo "Kafka-console-ui Started!"