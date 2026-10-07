package com.xuxd.kafka.console.dao.init;

import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataInitTest {

    @Test
    void newRoleGetsIdAfterSeededRoles() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:role_seed_test;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t_sys_role (id IDENTITY PRIMARY KEY, role_name VARCHAR(100), "
                    + "description VARCHAR(100), permission_ids VARCHAR(500))");
        }

        AuthConfig authConfig = new AuthConfig();
        authConfig.setEnable(true);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysPermissionMapper permissionMapper = mock(SysPermissionMapper.class);
        when(userMapper.selectCount(null)).thenReturn(1L);
        when(roleMapper.selectCount(null)).thenReturn(0L);
        when(permissionMapper.selectCount(null)).thenReturn(1L);

        new DataInit(authConfig, userMapper, roleMapper, permissionMapper, dataSource,
                mock(ApplicationEventPublisher.class)).afterSingletonsInstantiated();

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO t_sys_role(role_name) VALUES ('new role')");
            try (ResultSet resultSet = statement.executeQuery("SELECT id FROM t_sys_role WHERE role_name = 'new role'")) {
                resultSet.next();
                assertEquals(3L, resultSet.getLong(1));
            }
        }
    }
}
