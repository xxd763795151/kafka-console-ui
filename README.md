# kafka可视化管理平台
目前支持部分acl功能管理操作  
实现：spring boot + scala + vue + kafka  
# 打包、部署
## 打包
环境要求  
* maven 3+
* jdk 8
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