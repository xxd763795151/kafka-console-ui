package com.xuxd.kafka.console.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统权限 .
 *
 * @author: xuxd
 * @date: 2023/4/11 21:21
 **/
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionDO> {
}
