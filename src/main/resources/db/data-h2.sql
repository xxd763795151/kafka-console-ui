-- 不要随便修改下面的注释，要根据这个注释初始化加载数据
-- t_sys_permission start--
insert into t_sys_permission(id, name,type,parent_id,permission) values(0,'主页',0,null,'home');

insert into t_sys_permission(id, name,type,parent_id,permission) values(11,'集群',0,null,'cluster');
insert into t_sys_permission(id, name,type,parent_id,permission) values(12,'属性配置',1,11,'cluster:property-config');
insert into t_sys_permission(id, name,type,parent_id,permission) values(13,'日志配置',1,11,'cluster:log-config');
insert into t_sys_permission(id, name,type,parent_id,permission) values(14,'编辑配置',1,11,'cluster:edit');

insert into t_sys_permission(id, name,type,parent_id,permission) values(21,'Topic',0,null,'topic');
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
insert into t_sys_permission(id, name,type,parent_id,permission) values(34,'编辑属性配置',1,30,'topic:property-config:edit');
insert into t_sys_permission(id, name,type,parent_id,permission) values(35,'删除属性配置',1,30,'topic:property-config:del');

insert into t_sys_permission(id, name,type,parent_id,permission) values(41,'消费组',0,null,'group');
insert into t_sys_permission(id, name,type,parent_id,permission) values(42,'新增订阅',1,41,'group:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(43,'删除',1,41,'group:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(44,'消费端',1,41,'group:client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(45,'消费详情',1,41,'group:consumer-detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(46,'最小位点',1,45,'group:consumer-detail:min');
insert into t_sys_permission(id, name,type,parent_id,permission) values(47,'最新位点',1,45,'group:consumer-detail:last');
insert into t_sys_permission(id, name,type,parent_id,permission) values(48,'时间戳',1,45,'group:consumer-detail:timestamp');
insert into t_sys_permission(id, name,type,parent_id,permission) values(49,'重置位点',1,45,'group:consumer-detail:any');
insert into t_sys_permission(id, name,type,parent_id,permission) values(50,'位移分区',1,41,'group:offset-partition');


insert into t_sys_permission(id, name,type,parent_id,permission) values(61,'消息',0,null,'message');
insert into t_sys_permission(id, name,type,parent_id,permission) values(62,'根据时间查询',1,61,'message:search-time');
insert into t_sys_permission(id, name,type,parent_id,permission) values(63,'根据偏移查询',1,61,'message:search-offset');
insert into t_sys_permission(id, name,type,parent_id,permission) values(64,'在线发送',1,61,'message:send');
insert into t_sys_permission(id, name,type,parent_id,permission) values(65,'在线删除',1,61,'message:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(66,'消息详情',1,61,'message:detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(67,'重新发送',1,61,'message:resend');

insert into t_sys_permission(id, name,type,parent_id,permission) values(80,'限流',0,null,'quota');
insert into t_sys_permission(id, name,type,parent_id,permission) values(81,'用户',1,80,'quota:user');
insert into t_sys_permission(id, name,type,parent_id,permission) values(82,'新增配置',1,81,'quota:user:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(83,'客户端ID',1,80,'quota:client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(84,'新增配置',1,83,'quota:client:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(85,'用户和客户端ID',1,80,'quota:user-client');
insert into t_sys_permission(id, name,type,parent_id,permission) values(86,'新增配置',1,85,'quota:user-client:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(87,'删除',1,80,'quota:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(88,'修改',1,80,'quota:edit');


insert into t_sys_permission(id, name,type,parent_id,permission) values(100,'Acl',0,null,'acl');
insert into t_sys_permission(id, name,type,parent_id,permission) values(101,'资源授权',1,100,'acl:authority');
insert into t_sys_permission(id, name,type,parent_id,permission) values(102,'新增主体权限',1,101,'acl:authority:add-principal');
insert into t_sys_permission(id, name,type,parent_id,permission) values(103,'权限详情',1,101,'acl:authority:detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(104,'管理生产权限',1,101,'acl:authority:producer');
insert into t_sys_permission(id, name,type,parent_id,permission) values(105,'管理消费权限',1,101,'acl:authority:consumer');
insert into t_sys_permission(id, name,type,parent_id,permission) values(106,'增加权限',1,101,'acl:authority:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(107,'清除权限',1,101,'acl:authority:clean');
insert into t_sys_permission(id, name,type,parent_id,permission) values(108,'SaslScram用户管理',1,100,'acl:sasl-scram');
insert into t_sys_permission(id, name,type,parent_id,permission) values(109,'新增/更新用户',1,108,'acl:sasl-scram:add-update');
insert into t_sys_permission(id, name,type,parent_id,permission) values(110,'详情',1,108,'acl:sasl-scram:detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(111,'删除',1,108,'acl:sasl-scram:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(112,'管理生产权限',1,108,'acl:sasl-scram:producer');
insert into t_sys_permission(id, name,type,parent_id,permission) values(113,'管理消费权限',1,108,'acl:sasl-scram:consumer');
insert into t_sys_permission(id, name,type,parent_id,permission) values(114,'增加权限',1,108,'acl:sasl-scram:add-auth');
insert into t_sys_permission(id, name,type,parent_id,permission) values(115,'彻底删除',1,108,'acl:sasl-scram:pure');

insert into t_sys_permission(id, name,type,parent_id,permission) values(140,'用户',0,null,'user-manage');
insert into t_sys_permission(id, name,type,parent_id,permission) values(141,'用户列表',1,140,'user-manage:user');
insert into t_sys_permission(id, name,type,parent_id,permission) values(142,'新增',1,141,'user-manage:user:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(143,'删除',1,141,'user-manage:user:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(144,'重置密码',1,141,'user-manage:user:reset-pass');
insert into t_sys_permission(id, name,type,parent_id,permission) values(145,'分配角色',1,141,'user-manage:user:change-role');
insert into t_sys_permission(id, name,type,parent_id,permission) values(146,'角色列表',1,140,'user-manage:role');
insert into t_sys_permission(id, name,type,parent_id,permission) values(147,'保存',1,146,'user-manage:role:save');
insert into t_sys_permission(id, name,type,parent_id,permission) values(148,'删除',1,146,'user-manage:role:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(149,'权限列表',1,140,'user-manage:permission');
insert into t_sys_permission(id, name,type,parent_id,permission) values(150,'个人设置',1,140,'user-manage:setting');
insert into t_sys_permission(id, name,type,parent_id,permission) values(151,'集群权限',1,140,'user-manage:cluster-role');
insert into t_sys_permission(id, name,type,parent_id,permission) values(152,'新增',1,151,'user-manage:cluster-role:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(153,'删除',1,151,'user-manage:cluster-role:delete');

insert into t_sys_permission(id, name,type,parent_id,permission) values(160,'运维',0,null,'op');
insert into t_sys_permission(id, name,type,parent_id,permission) values(161,'集群切换',1,160,'op:cluster-switch');
insert into t_sys_permission(id, name,type,parent_id,permission) values(162,'新增集群',1,161,'op:cluster-switch:add');
insert into t_sys_permission(id, name,type,parent_id,permission) values(163,'切换',1,161,'op:cluster-switch:switch');
insert into t_sys_permission(id, name,type,parent_id,permission) values(164,'编辑',1,161,'op:cluster-switch:edit');
insert into t_sys_permission(id, name,type,parent_id,permission) values(165,'删除',1,161,'op:cluster-switch:del');
insert into t_sys_permission(id, name,type,parent_id,permission) values(166,'配置限流',1,160,'op:config-throttle');
insert into t_sys_permission(id, name,type,parent_id,permission) values(167,'解除限流',1,160,'op:remove-throttle');
insert into t_sys_permission(id, name,type,parent_id,permission) values(168,'首选副本作leader',1,160,'op:replication-preferred');
insert into t_sys_permission(id, name,type,parent_id,permission) values(169,'副本变更详情',1,160,'op:replication-update-detail');
insert into t_sys_permission(id, name,type,parent_id,permission) values(170,'副本重分配',1,160,'op:replication-reassign');
insert into t_sys_permission(id, name,type,parent_id,permission) values(171,'取消副本重分配',1,169,'op:replication-update-detail:cancel');
-- t_sys_permission end--

-- t_sys_role start--
insert into t_sys_role(id, role_name, description, permission_ids) VALUES (1,'超级管理员','超级管理员','12,13,14,22,23,24,25,26,27,28,29,30,34,35,31,32,33,42,43,44,45,46,47,48,49,50,62,63,64,65,66,67,81,82,83,84,85,86,87,88,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,141,142,143,144,145,146,147,148,149,150,151,152,153,161,162,163,164,165,166,167,168,169,171,170');
insert into t_sys_role(id, role_name, description, permission_ids) VALUES (2,'普通管理员','普通管理员，不能更改用户信息','12,13,14,22,23,24,25,26,27,28,29,30,34,35,31,32,33,42,43,44,45,46,47,48,49,50,62,63,64,65,66,67,81,82,83,84,85,86,87,88,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,141,146,149,150,161,162,163,164,165,166,167,168,169,171,170');
-- insert into t_sys_role(id, role_name, description, permission_ids) VALUES (2,'访客','访客','12,13,22,26,29,32,44,45,50,62,63,81,83,85,141,146,149,150,161,163');
-- t_sys_role end--

-- t_sys_user start--
insert into t_sys_user(id, username, password, salt, role_ids) VALUES (1,'super-admin','3a3e4d32-5247-321b-9efb-9cbf60b2bf6c','e6973cfc-7583-4baa-8802-65ded1268ab6','1' );
insert into t_sys_user(id, username, password, salt, role_ids) VALUES (2,'admin','3a3e4d32-5247-321b-9efb-9cbf60b2bf6c','e6973cfc-7583-4baa-8802-65ded1268ab6','2' );
-- t_sys_user end--