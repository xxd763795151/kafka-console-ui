package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import com.xuxd.kafka.console.beans.dos.SysUserDO;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;
import com.xuxd.kafka.console.beans.vo.SysPermissionVO;
import com.xuxd.kafka.console.beans.vo.SysRoleVO;
import com.xuxd.kafka.console.beans.vo.SysUserVO;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.service.UserManageService;
import com.xuxd.kafka.console.utils.RandomStringUtil;
import com.xuxd.kafka.console.utils.UUIDStrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    public ResponseData addOrUdpateRole(SysRoleDTO roleDTO) {
        SysRoleDO roleDO = roleDTO.toDO();
        if (roleDO.getId() == null) {
            roleMapper.insert(roleDO);
        } else {
            roleMapper.updateById(roleDO);
        }
        return ResponseData.create().success();
    }

    @Override
    public ResponseData addOrUpdateUser(SysUserDTO userDTO) {

        if (userDTO.getId() == null) {
            if (StringUtils.isEmpty(userDTO.getPassword())) {
                userDTO.setPassword(RandomStringUtil.random6Str());
            }
            SysUserDO userDO = userDTO.toDO();
            QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(true, "username", userDO.getUsername());
            SysUserDO exist = userMapper.selectOne(queryWrapper);
            if (exist != null) {
                return ResponseData.create().failed("用户已存在：" + userDO.getUsername());
            }
            userDO.setSalt(UUIDStrUtil.random());
            userDO.setPassword(UUIDStrUtil.generate(userDTO.getPassword(), userDO.getSalt()));
            userMapper.insert(userDTO.toDO());
        } else {
            SysUserDO userDO = userMapper.selectById(userDTO.getId());
            if (userDO == null) {
                log.error("查不到用户: {}", userDTO.getId());
                return ResponseData.create().failed("Unknown User.");
            }
            // 判断是否更新密码
            if (userDTO.getResetPassword()) {
                userDTO.setPassword(RandomStringUtil.random6Str());
                userDO.setSalt(UUIDStrUtil.random());
                userDO.setPassword(UUIDStrUtil.generate(userDTO.getPassword(), userDO.getSalt()));
            }
            userDO.setRoleIds(userDTO.getRoleIds());
            userDO.setUsername(userDTO.getUsername());
            userMapper.updateById(userDO);
        }
        return ResponseData.create().data(userDTO.getPassword()).success();
    }

    @Override
    public ResponseData selectRole() {
        List<SysRoleDO> dos = roleMapper.selectList(new QueryWrapper<>());
        return ResponseData.create().data(dos.stream().map(SysRoleVO::from).collect(Collectors.toList())).success();
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

    @Override
    public ResponseData selectUser() {
        QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
        List<SysUserDO> userDOS = userMapper.selectList(queryWrapper);
        List<SysRoleDO> roleDOS = roleMapper.selectList(null);
        Map<Long, SysRoleDO> roleDOMap = roleDOS.stream().collect(Collectors.toMap(SysRoleDO::getId, Function.identity(), (e1, e2) -> e1));
        List<SysUserVO> voList = userDOS.stream().map(SysUserVO::from).collect(Collectors.toList());
        voList.forEach(vo -> {
            if (vo.getRoleIds() != null) {
                Long roleId = Long.valueOf(vo.getRoleIds());
                vo.setRoleNames(roleDOMap.containsKey(roleId) ? roleDOMap.get(roleId).getRoleName() : null);
            }
        });
        return ResponseData.create().data(voList).success();
    }

    @Override
    public ResponseData updateUser(SysUserDTO userDTO) {
        userMapper.updateById(userDTO.toDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData updateRole(SysRoleDTO roleDTO) {
        roleMapper.updateById(roleDTO.toDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData deleteRole(Long id) {
        QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(true, "role_ids", id);
        Integer count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return ResponseData.create().failed("存在用户被分配为当前角色，不允许删除");
        }
        roleMapper.deleteById(id);
        return ResponseData.create().success();
    }

    @Override
    public ResponseData deleteUser(Long id) {
        userMapper.deleteById(id);
        return ResponseData.create().success();
    }

    @Override
    public ResponseData updatePassword(SysUserDTO userDTO) {
        SysUserDO userDO = userDTO.toDO();
        userDO.setSalt(UUIDStrUtil.random());
        userDO.setPassword(UUIDStrUtil.generate(userDTO.getPassword(), userDO.getSalt()));
        userMapper.updateById(userDO);
        return ResponseData.create().success();
    }
}
