package com.xuxd.kafka.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xuxd
 * @date: 2023/5/9 21:08
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

    /**
     * 是否启用登录权限认证.
     */
    private boolean enable;

    /**
     * 认证生成Jwt token用的,随便写.
     */
    private String secret = "kafka-console-ui-default-secret";

    /**
     * token有效期，小时.
     */
    private long expireHours;

    /**
     * 隐藏集群的属性信息，如果当前用户没有集群切换里的编辑权限，就不能看集群的属性信息，有开启ACL的集群需要开启这个.
     * 不隐藏属性不行，开启ACL的时候，属性里需要配置认证信息，比如超管的用户名密码等，不等被普通角色看到.
     */
    private boolean hideClusterProperty;

    /**
     * 不要修改.与data-h2.sql里配置的一致即可.
     */
    private String hideClusterPropertyPerm = "op:cluster-switch:edit";

    /**
     * 是否启用集群的数据权限，如果启用，可以配置哪些角色看到哪些集群.
     * 默认false是为了兼容老版本.
     *
     * @since 1.0.9
     */
    private boolean enableClusterAuthority;
}
