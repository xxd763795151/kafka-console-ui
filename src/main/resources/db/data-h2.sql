-- DELETE FROM t_kafka_user;
--
-- INSERT INTO t_kafka_user (id, username, password) VALUES
-- (1, 'Jone', 'p1'),
-- (2, 'Jack', 'p2');

insert into t_sys_permission(id, name,type,parent_id,permission) values(0,'主页',0,null,'home');

insert into t_sys_permission(id, name,type,parent_id,permission) values(10,'集群',0,null,'cluster');
insert into t_sys_permission(id, name,type,parent_id,permission) values(11,'集群信息',1,10,'cluster');
insert into t_sys_permission(id, name,type,parent_id,permission) values(12,'属性配置',1,11,'cluster:property-config');
insert into t_sys_permission(id, name,type,parent_id,permission) values(13,'日志配置',1,11,'cluster:log-config');

insert into t_sys_permission(id, name,type,parent_id,permission) values(20,'Topic',0,null,'topic');
insert into t_sys_permission(id, name,type,parent_id,permission) values(21,'topic',1,20,'topic');
insert into t_sys_permission(id, name,type,parent_id,permission) values(22,'刷新',1,21,'topic:load');
insert into t_sys_permission(id, name,type,parent_id,permission) values(23,'新增',1,21,'topic:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(24,'批量删除',1,21,'topic:batch-del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(25,'删除',1,21,'topic:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(26,'分区详情',1,21,'topic:partition-detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(27,'首选副本作leader',1,26,'topic:partition-detail:preferred');
insert into t_sys_permission(id, name,type,parent_id,permission) values(28,'增加分区',1,21,'topic:partition-add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(29,'消费详情',1,21,'topic:consumer-detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(30,'属性配置',1,21,'topic:property-config');
insert into t_sys_permission(id, name,type,parent_id,permission) values(31,'变更副本',1,21,'topic:replication-modify');
insert into t_sys_permission(id, name,type,parent_id,permission) values(32,'发送统计',1,21,'topic:send-count');
insert into t_sys_permission(id, name,type,parent_id,permission) values(33,'限流',1,21,'topic:replication-sync-throttle');

insert into t_sys_permission(id, name,type,parent_id,permission) values(40,'消费组',0,null,'group');
insert into t_sys_permission(id, name,type,parent_id,permission) values(41,'消费组',1,40,'group:load');
insert into t_sys_permission(id, name,type,parent_id,permission) values(42,'新增订阅',1,41,'group:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(43,'删除',1,41,'group:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(44,'消费端',1,41,'group:client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(45,'消费详情',1,41,'group:consumer-detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(46,'最小位点',1,45,'group:consumer-detail:min');
insert into t_sys_permission(id, name,type,parent_id,permission) values(47,'最新位点',1,45,'group:consumer-detail:last');
insert into t_sys_permission(id, name,type,parent_id,permission) values(48,'时间戳',1,45,'group:consumer-detail:timestamp');
insert into t_sys_permission(id, name,type,parent_id,permission) values(49,'重置位点',1,45,'group:consumer-detail:any');
insert into t_sys_permission(id, name,type,parent_id,permission) values(50,'位移分区',1,41,'group:offset-partition');


insert into t_sys_permission(id, name,type,parent_id,permission) values(60,'消息',0,null,'message');
insert into t_sys_permission(id, name,type,parent_id,permission) values(61,'消息',1,60,'message');
insert into t_sys_permission(id, name,type,parent_id,permission) values(62,'根据时间查询',1,61,'message:search-time');
insert into t_sys_permission(id, name,type,parent_id,permission) values(63,'根据偏移查询',1,61,'message:search-offset');
insert into t_sys_permission(id, name,type,parent_id,permission) values(64,'在线发送',1,61,'message:send');
insert into t_sys_permission(id, name,type,parent_id,permission) values(65,'在线删除',1,61,'message:del');

insert into t_sys_permission(id, name,type,parent_id,permission) values(80,'限流',0,null,'quota');
insert into t_sys_permission(id, name,type,parent_id,permission) values(81,'用户',1,80,'quota:user');
insert into t_sys_permission(id, name,type,parent_id,permission) values(82,'新增配置',1,81,'quota:user:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(83,'客户端ID',1,80,'quota:client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(84,'新增配置',1,83,'quota:client:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(85,'用户和客户端ID',1,80,'quota:user-client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(86,'新增配置',1,85,'quota:user-client:add');


insert into t_sys_permission(id, name,type,parent_id,permission) values(100,'Acl',0,null,'acl');
insert into t_sys_permission(id, name,type,parent_id,permission) values(101,'资源授权',1,100,'acl:authority');
insert into t_sys_permission(id, name,type,parent_id,permission) values(102,'新增权限',1,101,'acl:authority:add');

insert into t_sys_permission(id, name,type,parent_id,permission) values(120,'用户',0,null,'user-manage');
insert into t_sys_permission(id, name,type,parent_id,permission) values(121,'用户列表',1,120,'user-manage:user');
insert into t_sys_permission(id, name,type,parent_id,permission) values(122,'新增',1,121,'user-manage:user:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(123,'删除',1,121,'user-manage:user:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(124,'重置密码',1,121,'user-manage:user:reset-pass');
insert into t_sys_permission(id, name,type,parent_id,permission) values(125,'分配角色',1,121,'user-manage:user:change-role');
insert into t_sys_permission(id, name,type,parent_id,permission) values(126,'角色列表',1,120,'user-manage:role');
insert into t_sys_permission(id, name,type,parent_id,permission) values(127,'保存',1,126,'user-manage:role:save');
insert into t_sys_permission(id, name,type,parent_id,permission) values(128,'删除',1,126,'user-manage:role:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(129,'权限列表',1,120,'user-manage:permission');
insert into t_sys_permission(id, name,type,parent_id,permission) values(130,'个人设置',1,120,'user-manage:setting');
