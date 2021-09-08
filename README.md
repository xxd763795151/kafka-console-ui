# kafka可视化管理平台
## 功能支持
* 基于SASL_SCRAM认证授权管理
* Topic管理
* 消费组管理
* 运维
## 技术栈
* spring boot 
* java、scala 
* kafka
* h2  
* vue 
## kafka版本
* 当前使用的kafka 2.8.0
## 监控
仅提供运维管理功能，监控、告警需要配合其它组件，使用请查看：https://blog.csdn.net/x763795151/article/details/119705372
# 打包、部署
## 打包
环境要求  
* maven 3+
* jdk 8
* git  
```
git clone https://github.com/xxd763795151/kafka-console-ui.git
cd kafka-console-ui
sh package.sh
```
打包成功，输出文件：target/kafka-console-ui.tar.gz
## 部署
```
# 解压缩
tar -zxvf kafka-console-ui.tar.gz
# 进入解压缩后的目录
cd kafka-console-ui
# 编辑配置
vim config/application.yml
# 启动
sh bin/start.sh
# 停止
sh bin/shutdown.sh
```
# 开发环境
* jdk 8
* idea
* scala 2.13
* maven 3+
* webstorm