-- DROP TABLE IF EXISTS T_KAKFA_USER;

-- kafka ACL启用SASL_SCRAM中的用户
CREATE TABLE IF NOT EXISTS T_KAFKA_USER
(
    ID              IDENTITY     NOT NULL COMMENT '主键ID',
    USERNAME        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '用户名',
    PASSWORD        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密码',
    UPDATE_TIME     TIMESTAMP    NOT NULL DEFAULT NOW() COMMENT '更新时间',
    CLUSTER_INFO_ID BIGINT       NOT NULL COMMENT '集群信息里的集群ID',
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
    ID           IDENTITY      NOT NULL COMMENT '主键ID',
    CLUSTER_NAME VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '集群名',
    ADDRESS      VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '集群地址',
    PROPERTIES   VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '集群的其它属性配置',
    UPDATE_TIME  TIMESTAMP     NOT NULL DEFAULT NOW() COMMENT '更新时间',
    PRIMARY KEY (ID),
    UNIQUE (CLUSTER_NAME)
);

-- 登录用户的角色权限配置
CREATE TABLE IF NOT EXISTS t_sys_permission
(
    ID         IDENTITY NOT NULL,
    name       varchar(100)      DEFAULT NULL,
    type       tinyint  NOT NULL DEFAULT 0,
    parent_id  bigint            DEFAULT NULL,
    permission varchar(100)      DEFAULT NULL,
    PRIMARY KEY (id)
);
COMMENT ON TABLE t_sys_permission IS '系统权限表';
COMMENT ON COLUMN t_sys_permission.ID IS '主键ID';
COMMENT ON COLUMN t_sys_permission.name IS '权限名称';
COMMENT ON COLUMN t_sys_permission.type IS '权限类型: 0：菜单，1：按钮';
COMMENT ON COLUMN t_sys_permission.parent_id IS '所属父权限ID';
COMMENT ON COLUMN t_sys_permission.permission IS '权限字符串';

CREATE TABLE IF NOT EXISTS t_sys_role
(
    ID             IDENTITY     NOT NULL,
    role_name      varchar(100) NOT NULL,
    description    varchar(100) DEFAULT NULL,
    permission_ids varchar(500) DEFAULT NULL,
    PRIMARY KEY (id)
);
COMMENT ON TABLE t_sys_role IS '系统角色表';
COMMENT ON COLUMN t_sys_role.ID IS '主键ID';
COMMENT ON COLUMN t_sys_role.role_name IS '角色名称';
COMMENT ON COLUMN t_sys_role.description IS '角色描述';
COMMENT ON COLUMN t_sys_role.permission_ids IS '分配的权限ID';

CREATE TABLE IF NOT EXISTS t_sys_user
(
    ID       IDENTITY NOT NULL,
    username varchar(100) DEFAULT NULL,
    password varchar(100) DEFAULT NULL,
    salt     varchar(100) DEFAULT NULL,
    role_ids varchar(100) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);
COMMENT ON TABLE t_sys_user IS '系统用户表';
COMMENT ON COLUMN t_sys_user.ID IS '主键ID';
COMMENT ON COLUMN t_sys_user.username IS '用户名';
COMMENT ON COLUMN t_sys_user.password IS '用户密码';
COMMENT ON COLUMN t_sys_user.salt IS '加密的盐值';
COMMENT ON COLUMN t_sys_user.role_ids IS '分配角色的ID';

CREATE TABLE IF NOT EXISTS t_cluster_role_relation
(
    ID              IDENTITY  NOT NULL,
    ROLE_ID         bigint    NOT NULL,
    CLUSTER_INFO_ID bigint    NOT NULL,
    UPDATE_TIME     TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    UNIQUE (ROLE_ID, CLUSTER_INFO_ID)
);
COMMENT ON TABLE t_cluster_role_relation IS '集群数据权限与角色绑定表';
COMMENT ON COLUMN t_cluster_role_relation.ID IS '主键ID';
COMMENT ON COLUMN t_cluster_role_relation.ROLE_ID IS '角色ID';
COMMENT ON COLUMN t_cluster_role_relation.CLUSTER_INFO_ID IS '集群信息的ID';
COMMENT ON COLUMN t_cluster_role_relation.UPDATE_TIME IS '更新时间';