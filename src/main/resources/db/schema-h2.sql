-- DROP TABLE IF EXISTS T_KAKFA_USER;

-- kafka ACL启用SASL_SCRAM中的用户
CREATE TABLE IF NOT EXISTS T_KAFKA_USER
(
    ID              IDENTITY    NOT NULL COMMENT '主键ID',
    USERNAME        VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名',
    PASSWORD        VARCHAR(50) NOT NULL DEFAULT '' COMMENT '密码',
    UPDATE_TIME     TIMESTAMP   NOT NULL DEFAULT NOW() COMMENT '更新时间',
    CLUSTER_INFO_ID BIGINT      NOT NULL COMMENT '集群信息里的集群ID',
    PRIMARY KEY (ID),
    UNIQUE (USERNAME)
);
-- 消息同步解决方案中使用的位点对齐信息
CREATE TABLE IF NOT EXISTS T_MIN_OFFSET_ALIGNMENT
(
    ID          IDENTITY     NOT NULL COMMENT '主键ID',
    GROUP_ID    VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'groupId',
    TOPIC       VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'topic',
    THAT_OFFSET VARCHAR(512) NOT NULL DEFAULT '' COMMENT 'min offset for that kafka cluster',
    THIS_OFFSET VARCHAR(512) NOT NULL DEFAULT '' COMMENT 'min offset for this kafka cluster',
    UPDATE_TIME TIMESTAMP    NOT NULL DEFAULT NOW() COMMENT '更新时间',
    PRIMARY KEY (ID),
    UNIQUE (GROUP_ID, TOPIC)
);

-- 多集群管理，每个集群的配置信息
CREATE TABLE IF NOT EXISTS T_CLUSTER_INFO
(
    ID           IDENTITY     NOT NULL COMMENT '主键ID',
    CLUSTER_NAME VARCHAR(128) NOT NULL DEFAULT '' COMMENT '集群名',
    ADDRESS      VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '集群地址',
    PROPERTIES   VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '集群的其它属性配置',
    UPDATE_TIME  TIMESTAMP    NOT NULL DEFAULT NOW() COMMENT '更新时间',
    PRIMARY KEY (ID),
    UNIQUE (CLUSTER_NAME)
);

-- 登录用户的角色权限配置
CREATE TABLE IF NOT EXISTS t_sys_permission
(
    ID         IDENTITY   NOT NULL COMMENT '主键ID',
    name       varchar(100)        DEFAULT NULL COMMENT '权限名称',
    type       tinyint(1) NOT NULL DEFAULT 0 COMMENT '权限类型: 0：菜单，1：按钮',
    parent_id  bigint(20)          DEFAULT NULL COMMENT '所属父权限ID',
    permission varchar(100)        DEFAULT NULL COMMENT '权限字符串',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_sys_role
(
    ID             IDENTITY     NOT NULL COMMENT '主键ID',
    role_name      varchar(100) NOT NULL COMMENT '角色名称',
    description    varchar(100) DEFAULT NULL COMMENT '角色描述',
    permission_ids varchar(500) DEFAULT NULL COMMENT '分配的权限ID',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_sys_user
(
    ID       IDENTITY NOT NULL COMMENT '主键ID',
    username varchar(100) DEFAULT NULL COMMENT '用户名',
    password varchar(100) DEFAULT NULL COMMENT '用户密码',
    salt     varchar(100) DEFAULT NULL COMMENT '加密的盐值',
    role_ids varchar(100) DEFAULT NULL COMMENT '分配角色的ID',
    PRIMARY KEY (id),
    UNIQUE (username)
);