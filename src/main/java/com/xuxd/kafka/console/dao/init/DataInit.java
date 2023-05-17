package com.xuxd.kafka.console.dao.init;

import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author: xuxd
 * @date: 2023/5/17 13:10
 **/
@Slf4j
@Component
public class DataInit implements SmartInitializingSingleton {

    private final AuthConfig authConfig;

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysPermissionMapper permissionMapper;

    private final DataSource dataSource;

    private final SqlParse sqlParse;


    public DataInit(AuthConfig authConfig,
                    SysUserMapper userMapper,
                    SysRoleMapper roleMapper,
                    SysPermissionMapper permissionMapper,
                    DataSource dataSource) {
        this.authConfig = authConfig;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.dataSource = dataSource;
        this.sqlParse = new SqlParse();
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!authConfig.isEnable()) {
            log.info("Disable login authentication, no longer try to initialize the data");
            return;
        }
        try {
            Connection connection = dataSource.getConnection();
            Integer userCount = userMapper.selectCount(null);
            if (userCount == null || userCount == 0) {
                initData(connection, SqlParse.USER_TABLE);
            }

            Integer roleCount = roleMapper.selectCount(null);
            if (roleCount == null || roleCount == 0) {
                initData(connection, SqlParse.ROLE_TABLE);
            }

            Integer permCount = permissionMapper.selectCount(null);
            if (permCount == null || permCount == 0) {
                initData(connection, SqlParse.PERM_TABLE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void initData(Connection connection, String table) throws SQLException {
        log.info("Init default data for {}", table);
        String sql = sqlParse.getMergeSql(table);
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.execute();
    }
}
