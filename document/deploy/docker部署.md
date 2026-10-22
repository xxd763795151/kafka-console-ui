# Docker/DockerCompose部署

## 当前项目镜像：xiaodonggg/kafka-console-ui

以下命令在项目根目录执行。镜像包含前后端，使用 Java 17，监听 `7766`，以 UID/GID `10001:10001` 运行。

### 拉取和启动

```shell
docker pull xiaodonggg/kafka-console-ui:1.2.0
docker run -d --name kafka-console-ui --restart unless-stopped -p 7766:7766 -v kafka-console-ui-data:/app/data -v kafka-console-ui-logs:/app/logs xiaodonggg/kafka-console-ui:1.2.0
```

访问 `http://localhost:7766`，在页面上添加 Kafka 集群。集群地址需要能从容器内访问。

`/app/data` 保存数据库，`/app/logs` 保存日志。以上使用 Docker 命名卷，容器删除后数据仍保留。如果改用宿主机目录挂载，需要让 UID `10001` 拥有目录写权限。

需要自定义配置时，将配置文件只读挂载到 `/app/config/application.yml`；应用会在内置配置基础上加载它。也可以通过 Spring Boot 环境变量覆盖配置，例如 `AUTH_ENABLE=true`。默认配置未开启登录认证，可按部署需要开启。

### Docker Compose

```yaml
services:
  kafka-console-ui:
    image: xiaodonggg/kafka-console-ui:1.2.0
    container_name: kafka-console-ui
    restart: unless-stopped
    ports:
      - "7766:7766"
    volumes:
      - kafka-console-ui-data:/app/data
      - kafka-console-ui-logs:/app/logs

volumes:
  kafka-console-ui-data:
  kafka-console-ui-logs:
```

保存为 `compose.yaml` 后执行 `docker compose up -d`。

### 从源码构建和推送

本机需要 JDK 17、Maven 和正在运行的 Docker Linux 引擎；Maven 的 `deploy` profile 会安装 Node/npm 并构建前端。

```shell
mvn -B -ntp clean package -Pdeploy "-Dtest=!ClientQuotaConsoleTest,!TopicServiceImplTest"
docker build --platform linux/amd64 --build-arg APP_VERSION=1.2.0 -t xiaodonggg/kafka-console-ui:1.2.0 -t xiaodonggg/kafka-console-ui:latest .
docker login
docker push xiaodonggg/kafka-console-ui:1.2.0
docker push xiaodonggg/kafka-console-ui:latest
```

`ClientQuotaConsoleTest` 需要本机 `localhost:9092` 的 Kafka；`TopicServiceImplTest` 直接调用服务，但没有设置所需的集群线程上下文，会触发空指针异常。上面的构建命令排除这两组现有测试，其余测试仍执行。准备好 Kafka 并补齐测试上下文后，可去掉 `-Dtest` 参数运行完整测试。

Dockerfile 只复制 `target/kafka-console-ui.jar`；`.dockerignore` 只允许该构建产物进入上下文，本机数据库、日志和其他文件不会进入镜像。

默认 JVM 参数为 `-Xms128m -Xmx512m -Dfile.encoding=UTF-8`，可通过 `JAVA_TOOL_OPTIONS` 环境变量调整。

---

以下保留原第三方镜像 `wdkang/kafka-console-ui` 的部署说明；其路径和构建方式与上面的当前项目镜像不同。

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
# jdk 17+
FROM eclipse-temurin:17-jre-alpine
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
# start server (jdk17+ 需要添加 --add-opens 参数)
CMD java -jar -Xmx512m -Xms512m -Xmn256m -Xss256k \
    --add-opens java.base/java.io=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.net=ALL-UNNAMED \
    /app/lib/kafka-console-ui.jar --spring.config.location="/app/config/" --logging.home="/app/log" --data.dir="/app/data"

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





