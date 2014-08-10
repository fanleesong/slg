package com.h13.slg.role.service;

import com.google.common.collect.Lists;
import com.h13.slg.config.cache.EquipCache;
import com.h13.slg.config.cache.RoleCache;
import com.h13.slg.config.co.EquipCO;
import com.h13.slg.config.co.RoleCO;
import com.h13.slg.config.fetcher.EquipConfigFetcher;
import com.h13.slg.core.RequestErrorException;
import com.h13.slg.core.SlgData;
import com.h13.slg.core.SlgRequestDTO;
import com.h13.slg.core.util.SlgBeanUtils;
import com.h13.slg.equip.EquipConstants;
import com.h13.slg.equip.co.UserEquipCO;
import com.h13.slg.equip.helper.UserEquipHelper;
import com.h13.slg.event.EventType;
import com.h13.slg.event.helper.UserEventHelper;
import com.h13.slg.role.RoleConstants;
import com.h13.slg.role.co.UserRoleCO;
import com.h13.slg.role.helper.UserRoleHelper;
import com.h13.slg.role.vo.UserRoleAccessoryVO;
import com.h13.slg.role.vo.UserRoleArmorVO;
import com.h13.slg.role.vo.UserRoleVO;
import com.h13.slg.role.vo.UserRoleWeaponVO;
import com.h13.slg.skill.RoleSkillConstants;
import com.h13.slg.skill.co.UserRoleSkillCO;
import com.h13.slg.skill.helper.RoleSkillHelper;
import com.h13.slg.skill.vo.UserRoleSkillVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sunbo
 * Date: 14-3-5
 * Time: 下午8:08
 * To change this template use File | Settings | File Templates.
 */
@Service("RoleService")
public class RoleServiceImpl implements RoleService {
    @Autowired
    UserRoleHelper userRoleHelper;
    @Autowired
    UserEventHelper userEventHelper;

    @Autowired
    RoleCache roleCache;
    @Autowired
    EquipConfigFetcher equipConfigFetcher;
    @Autowired
    UserEquipHelper userEquipHelper;
    @Autowired
    RoleSkillHelper roleSkillHelper;


    @Override
    public SlgData wear(SlgRequestDTO requestDTO) throws RequestErrorException {
        int uid = requestDTO.getUid();
        int ueid = new Integer(requestDTO.getArgs().get("ueid").toString());
        int urid = new Integer(requestDTO.getArgs().get("urid").toString());

        userRoleHelper.wear(uid, urid, ueid);

        userEventHelper.addEvent(uid, EventType.WearEquip, null);

        return SlgData.getData();
    }

    @Override
    public SlgData takeOff(SlgRequestDTO requestDTO) throws RequestErrorException {
        long uid = requestDTO.getUid();
        long ueid = new Long(requestDTO.getArgs().get("ueid").toString());
        long urid = new Long(requestDTO.getArgs().get("urid").toString());
        userRoleHelper.takeOff(uid, urid, ueid);
        return SlgData.getData();
    }


    @Override
    public SlgData userRoleList(SlgRequestDTO requestDTO) throws RequestErrorException {
        int uid = requestDTO.getUid();
        List<UserRoleCO> userRoleList = userRoleHelper.getUserRoleList(uid);
        List<UserRoleVO> voList = Lists.newLinkedList();
        // 获得将领的名称
        for (UserRoleCO ur : userRoleList) {
            UserRoleVO userRoleVO = new UserRoleVO();
            SlgBeanUtils.copyProperties(userRoleVO, ur);

            long roleId = ur.getRoleId();
            RoleCO roleCO = roleCache.get(roleId + "");
            ur.setRoleName(roleCO.getName());
            // 获取装备相关的数据
            // 武器
            if (ur.getWeapon() != RoleConstants.NO_EQUIP_ID) {
                UserEquipCO weaponCO = userEquipHelper.getUserEquip(uid, ur.getId(), EquipConstants.EquipType.WEAPON);
                UserRoleWeaponVO userRoleWeaponVO = new UserRoleWeaponVO();
                userRoleWeaponVO.setLevel(weaponCO.getLevel());
                userRoleWeaponVO.setUserEquipId(weaponCO.getId());
                EquipCO equipCO = equipConfigFetcher.get(weaponCO.getLevel() + "");
                userRoleWeaponVO.setName(equipCO.getWeaponName());
                userRoleVO.setWeaponInfo(userRoleWeaponVO);
            } else {
                userRoleVO.setWeaponInfo(null);
            }
            // 饰品
            if (ur.getAccessory() != RoleConstants.NO_EQUIP_ID) {
                UserEquipCO accessoryCO = userEquipHelper.getUserEquip(uid, ur.getId(), EquipConstants.EquipType.ACCESSORY);
                UserRoleAccessoryVO userRoleAccessoryVO = new UserRoleAccessoryVO();
                userRoleAccessoryVO.setLevel(accessoryCO.getLevel());
                userRoleAccessoryVO.setUserEquipId(accessoryCO.getId());
                EquipCO equipCO = equipConfigFetcher.get(accessoryCO.getLevel() + "");
                userRoleAccessoryVO.setName(equipCO.getAccessoryName());
                userRoleVO.setAccessoryInfo(userRoleAccessoryVO);
            } else {
                userRoleVO.setAccessoryInfo(null);
            }
            // 头盔
            if (ur.getArmor() != RoleConstants.NO_EQUIP_ID) {
                UserEquipCO armorCO = userEquipHelper.getUserEquip(uid, ur.getId(), EquipConstants.EquipType.ARMOR);
                UserRoleArmorVO userRoleArmorVO = new UserRoleArmorVO();
                userRoleArmorVO.setLevel(armorCO.getLevel());
                userRoleArmorVO.setUserEquipId(armorCO.getId());
                EquipCO equipCO = equipConfigFetcher.get(armorCO.getLevel() + "");
                userRoleArmorVO.setName(equipCO.getArmorName());
                userRoleVO.setArmorInfo(userRoleArmorVO);
            } else {
                userRoleVO.setArmorInfo(null);
            }

            UserRoleSkillCO putongUserRoleSkillCO = roleSkillHelper.getJiangling(uid, ur.getId());
            UserRoleSkillCO tianfuUserRoleSkillCO = roleSkillHelper.getTianfu(uid, ur.getId());
            UserRoleSkillVO putongVO = new UserRoleSkillVO();
            UserRoleSkillVO tianfuVO = new UserRoleSkillVO();

            if (putongUserRoleSkillCO != null) {
                SlgBeanUtils.copyProperties(putongVO, putongUserRoleSkillCO);
                userRoleVO.setPutongRoleSkill(putongVO);
            }
            if (tianfuUserRoleSkillCO != null) {
                SlgBeanUtils.copyProperties(tianfuVO, tianfuUserRoleSkillCO);
                userRoleVO.setTianfuRoleSkill(tianfuVO);
            }

            voList.add(userRoleVO);
        }

        return SlgData.getData().add("list", voList);
    }

    @Override
    public SlgData userRole(SlgRequestDTO requestDTO) throws RequestErrorException {

        long uid = requestDTO.getUid();
        long urid = new Long(requestDTO.getArgs().get("urid").toString());

        UserRoleCO userRoleCO = userRoleHelper.getUserRole(uid, urid);
        RoleCO roleCO = roleCache.get(userRoleCO.getRoleId() + "");
        userRoleCO.setRoleName(roleCO.getName());
        return SlgData.getData().add("role", userRoleCO);
    }


}
