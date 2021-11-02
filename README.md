# kafka可视化管理平台
一款轻量级的kafka可视化管理平台，安装配置快捷、简单易用。  
为了开发的省事，没有多语言支持，只支持中文展示。  
用过rocketmq-console吧，对，前端展示风格跟那个有点类似。
## 功能支持
* 集群信息
* Topic管理
* 消费组管理
* 基于SASL_SCRAM认证授权管理
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
除了webstorm是开发前端的ide可以根据自己需要代替，jdk scala是必须有的。
# 本地开发配置
以我自己为例，开发环境里的工具准备好，然后代码clone到本地。
## 后端配置
1. 用idea打开项目
2. 打开idea的Project Structure(Settings) ->  Modules -> 设置src/main/scala为Sources，因为约定src/main/java是源码目录，所以这里要再加一个
3. 打开idea的Project Structure(Settings) -> Libraries 添加scala sdk，然后选择本地下载的scala 2.13的目录，确定添加进来
## 前端
前端代码在工程的ui目录下，找个前端开发的ide打开进行开发即可。