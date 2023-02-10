# Docker/DockerCompose部署

# 1.快速上手

## 1.1 镜像拉取

```shell
docker pull wdkang/kafka-console-ui
```



## 1.2 查看镜像

```shell
docker images
```



## 1.3 启动服务

由于Docker内不会对数据进行持久化 所以这里推荐将数据目录映射到实体机中 

详见 **2.数据持久**

```shell
docker run -d -p 7766:7766 wdkang/kafka-console-ui
```



## 1.4 查看状态

```shell
docker ps -a
```



## 1.5 查看日志

```shell
docker logs -f ${containerId}
```



## 1.6 访问服务

```shell
http://localhost:7766
```



# 2. 数据持久

推荐对数据进行持久化

## 2.1 新建目录

```shell
mkdir -p /home/kafka-console-ui/data /home/kafka-console-ui/log
cd /home/kafka-console-ui
```

## 2.2 启动服务

```shell
docker run -d -p 7766:7766 -v $PWD/data:/app/data -v $PWD/log:/app/log wdkang/kafka-console-ui
```



# 3.自主打包

## 3.1 构建镜像

**前置需求**

(可根据自身情况修改Dockerfile)

下载[kafka-console-ui.zip](https://github.com/xxd763795151/kafka-console-ui/releases)包

解压后 将Dockerfile放入文件夹的根目录

**Dockerfile**

```dockerfile
# jdk
FROM openjdk:8-jdk-alpine
# label
LABEL by="https://github.com/xxd763795151/kafka-console-ui"
# root
RUN mkdir -p /app && cd /app
WORKDIR /app
# config log data
RUN mkdir -p /app/config && mkdir -p /app/log && mkdir -p /app/data && mkdir -p /app/lib
# add file
ADD ./lib/kafka-console-ui.jar /app/lib
ADD ./config /app/config
# port
EXPOSE 7766
# start server
CMD java -jar -Xmx512m -Xms512m -Xmn256m -Xss256k /app/lib/kafka-console-ui.jar --spring.config.location="/app/config/" --logging.home="/app/log" --data.dir="/app/data"

```

**进行打包**

在文件夹根目录下

(注意末尾有个点)

```shell
docker build -t ${your_docker_hub_addr} .
```



## 3.2 上传镜像

```shell
docker push ${your_docker_hub_addr}
```



# 4.容器编排

```dockerfile
# docker-compose 编排
version: '3'
services:
  # 服务名
  kafka-console-ui:
  	# 容器名
    container_name: "kafka-console-ui"
		# 端口
    ports:
      - "7766:7766"
    # 持久化
    volumes:
      - ./data:/app/data
      - ./log:/app/log
		# 防止读写文件有问题
    privileged: true
    user: root
    # 镜像地址
    image: "wdkang/kafka-console-ui"
```



## 4.1 拉取镜像

```shell
docker-compose pull kafka-console-ui
```



## 4.2 构建启动

```shell
docker-compose up --detach --build kafka-console-ui
```



## 4.3 查看状态

```shell
docker-compose ps -a
```



## 4.3 停止服务

```shell
docker-compose down
```





