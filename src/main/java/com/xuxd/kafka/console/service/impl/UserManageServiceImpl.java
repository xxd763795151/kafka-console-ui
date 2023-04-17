package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;
import com.xuxd.kafka.console.beans.vo.SysPermissionVO;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.service.UserManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:24
 **/
@Slf4j
@Service
public class UserManageServiceImpl implements UserManageService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysPermissionMapper permissionMapper;

    public UserManageServiceImpl(ObjectProvider<SysUserMapper> userMapper,
                                 ObjectProvider<SysRoleMapper> roleMapper,
                                 ObjectProvider<SysPermissionMapper> permissionMapper) {
        this.userMapper = userMapper.getIfAvailable();
        this.roleMapper = roleMapper.getIfAvailable();
        this.permissionMapper = permissionMapper.getIfAvailable();
    }

    @Override
    public ResponseData addPermission(SysPermissionDTO permissionDTO) {
        permissionMapper.insert(permissionDTO.toSysPermissionDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData addRole(SysRoleDTO roleDTO) {
        roleMapper.insert(roleDTO.toDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData addUser(SysUserDTO userDTO) {
        userMapper.insert(userDTO.toDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData selectPermission() {
        QueryWrapper<SysPermissionDO> queryWrapper = new QueryWrapper<>();

        List<SysPermissionDO> permissionDOS = permissionMapper.selectList(queryWrapper);
        List<SysPermissionVO> vos = new ArrayList<>();
        Map<Long, Integer> posMap = new HashMap<>();
        Map<Long, SysPermissionVO> voMap = new HashMap<>();

        Iterator<SysPermissionDO> iterator = permissionDOS.iterator();
        while (iterator.hasNext()) {
            SysPermissionDO permissionDO = iterator.next();
            if (permissionDO.getParentId() == null) {
                // 菜单
                SysPermissionVO vo = SysPermissionVO.from(permissionDO);
                vos.add(vo);
                int index = vos.size() - 1;
                // 记录位置
                posMap.put(permissionDO.getId(), index);
                iterator.remove();
            }
        }
        // 上面把菜单都处理过了
        while (!permissionDOS.isEmpty()) {
            iterator = permissionDOS.iterator();
            while (iterator.hasNext()) {
                SysPermissionDO permissionDO = iterator.next();
                Long parentId = permissionDO.getParentId();
                if (posMap.containsKey(parentId)) {
                    // 菜单下的按扭
                    SysPermissionVO vo = SysPermissionVO.from(permissionDO);
                    Integer index = posMap.get(parentId);
                    SysPermissionVO menuVO = vos.get(index);
                    if (menuVO.getChildren() == null) {
                        menuVO.setChildren(new ArrayList<>());
                    }
                    menuVO.getChildren().add(vo);
                    voMap.put(permissionDO.getId(), vo);
                    iterator.remove();
                } else if (voMap.containsKey(parentId)) {
                    // 按钮下的按扭
                    SysPermissionVO vo = SysPermissionVO.from(permissionDO);
                    SysPermissionVO buttonVO = voMap.get(parentId);
                    if (buttonVO.getChildren() == null) {
                        buttonVO.setChildren(new ArrayList<>());
                    }
                    buttonVO.getChildren().add(vo);
                    voMap.put(permissionDO.getId(), vo);
                    iterator.remove();
                }
            }
        }
        return ResponseData.create().data(vos).success();
    }
}
