package com.h13.slg.equip.helper;

import com.h13.slg.core.CodeConstants;
import com.h13.slg.core.log.SlgLogger;
import com.h13.slg.core.log.SlgLoggerEntity;
import com.h13.slg.equip.EquipConstants;
import com.h13.slg.pkg.helper.UserPackageHelper;
import com.h13.slg.config.co.EquipCO;
import com.h13.slg.config.co.StrengthenCO;
import com.h13.slg.config.fetcher.EquipConfigFetcher;
import com.h13.slg.config.fetcher.StrengthenConfigFetcher;
import com.h13.slg.core.RequestErrorException;
import com.h13.slg.equip.co.UserEquipCO;
import com.h13.slg.pkg.co.UserPackageCO;
import com.h13.slg.equip.dao.UserEquipDAO;
import com.h13.slg.role.helper.FightForceHelper;
import com.h13.slg.user.co.UserStatusCO;
import com.h13.slg.user.hepler.UserStatusHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sunbo
 * Date: 14-2-25
 * Time: 下午7:50
 * To change this template use File | Settings | File Templates.
 */
@Service
public class UserEquipHelper {

    @Autowired
    UserEquipDAO userEquipDAO;
    @Autowired
    StrengthenConfigFetcher strengthenConfigFetcher;

    @Autowired
    UserStatusHelper userStatusHelper;
    @Autowired
    EquipConfigFetcher equipConfigFetcher;

    @Autowired
    UserPackageHelper userPackageHelper;
    @Autowired
    FightForceHelper fightForceHelper;

    /**
     * 获取用户装备
     * <p>
     * 从数据库中读取
     * </p>
     *
     * @param ueId 用户装备id
     * @return
     */
    public UserEquipCO getUserEquip(long uid, long ueId) {
        return userEquipDAO.get(uid, ueId);
    }

    /**
     * 更新用户装备
     * <p>
     * 直接修改数据库，修改传入的对象中的所有的字段
     * </p>
     *
     * @param ue
     */
    public void updateUserEquip(UserEquipCO ue) {
        userEquipDAO.update(ue.getId(), ue.getLevel(), ue.getGemsMap(), ue.getStrength(),
                ue.getFail(), ue.getRefine(), ue.getStar(), ue.getUrid(), ue.getName());
    }


    /**
     * 强化装备
     *
     * @param ueId
     * @return int 下一个等级是多少
     */
    public int strengthen(long uid, long ueId) throws RequestErrorException {
        // 获得当前的强化等级
        UserEquipCO ue = getUserEquip(uid, ueId);
        int curStrength = ue.getStrength();
        String type = ue.getType();

        // 查看下一级强化需要的金币数目
        int nextStrength = curStrength + 1;
        StrengthenCO strengthenCO = strengthenConfigFetcher.get(nextStrength + "");
        if (strengthenCO == null)
            throw new RequestErrorException(CodeConstants.Role.EQUIP_STRENGTH_LEVEL_TO_TOP, "");
        int cost = 0;
        if (type.equals(EquipConstants.EquipType.WEAPON)) {
            cost = strengthenCO.getWeaponCost();
        } else if (type.equals(EquipConstants.EquipType.ARMOR)) {
            cost = strengthenCO.getArmorCost();
        } else {
            cost = strengthenCO.getAccessoryCost();
        }

        // 扣除对应的金币
        UserStatusCO userStatusCO = userStatusHelper.getUserStatus(uid);
        if (userStatusCO.getGold() >= cost) {
            userStatusCO.setGold(userStatusCO.getGold() - cost);
        }
        userStatusHelper.updateUserStatus(userStatusCO);

        // 增加强化等级
        ue.setStrength(nextStrength);
        updateUserEquip(ue);


        long urid = ue.getUrid();
        // 更新fightforce
        fightForceHelper.updateUserRoleFightForce(uid, urid);
        return nextStrength;
    }


    /**
     * 合成装备 （升级）
     */
    public int make(long uid, int lucky, long ueId) throws RequestErrorException {
        // 获得当前的装备等级
        UserEquipCO ue = getUserEquip(uid, ueId);
        int curLevel = ue.getLevel();
        int nextLevel = curLevel + 1;
        // 获得升级需要的资源数量
        EquipCO equipCO = equipConfigFetcher.get(curLevel + "");
        int materialId1 = 0;
        int materialId2 = 0;
        int materialCount1 = 0;
        int materialCount2 = 0;
        String type = ue.getType();
        if (type.equals(EquipConstants.EquipType.WEAPON)) {
            materialId1 = equipCO.getWeaponMaterialType1();
            materialId2 = equipCO.getWeaponMaterialType2();
            materialCount1 = equipCO.getWeaponMaterial1();
            materialCount2 = equipCO.getWeaponMaterial2();
        } else if (type.equals(EquipConstants.EquipType.ARMOR)) {
            materialId1 = equipCO.getArmorMaterialType1();
            materialId2 = equipCO.getArmorMaterialType2();
            materialCount1 = equipCO.getArmorMaterial1();
            materialCount2 = equipCO.getArmorMaterial2();
        } else {
            materialId1 = equipCO.getAccessoryMaterialType1();
            materialId2 = equipCO.getAccessoryMaterialType2();
            materialCount1 = equipCO.getAccessoryMaterial1();
            materialCount2 = equipCO.getAccessoryMaterial2();
        }

        // 查看背包中是否有这些资源
        UserPackageCO userPackageCO = userPackageHelper.get(uid);
        int packageMCount1 = userPackageHelper.getMaterialCount(uid, materialId1);
        int packageMCount2 = userPackageHelper.getMaterialCount(uid, materialId2);

        if (packageMCount1 < materialCount1 || packageMCount2 < materialCount2) {
            // 资源不够
            throw new RequestErrorException(CodeConstants.Role.RESOURCE_IS_NOT_ENOUGH, "");
        }
        Map d1 = userPackageCO.getMaterial();
        d1.put(materialId1 + "", packageMCount1 - materialCount1);
        d1.put(materialId2 + "", packageMCount2 - materialCount2);
        userPackageHelper.updateMaterial(userPackageCO);
        ue.setLevel(nextLevel);
        ue.setName(getEquipNameFromConfig(type, nextLevel));
        updateUserEquip(ue);

        fightForceHelper.updateUserRoleFightForce(uid, ue.getUrid());
        return nextLevel;
    }


    /**
     * 获得一个新的装备，从商店通过军功购买，或者通过程序获得
     *
     * @param uid
     * @param level
     * @param type EquipConstants.EquipType
     */
    public long addNewUserEquip(long uid, int level, String type) throws RequestErrorException {
        if (!EquipConstants.EquipType.ACCESSORY.equals(type)
                &&
                !EquipConstants.EquipType.ARMOR.equals(type)
                &&
                !EquipConstants.EquipType.WEAPON.equals(type)) {
            SlgLogger.error(SlgLoggerEntity.p("userequip", "getANewUserEquip", uid, "type is error.")
                    .addParam("level", level).addParam("type", type));
            throw new RequestErrorException(CodeConstants.SYSTEM.COMMON_ERROR, "type is error");
        }
        String name = getEquipNameFromConfig(type, EquipConstants.USER_EQUIP_DEFAULT_LEVEL);


        long ueid = userEquipDAO.insert(uid, type, level, "{}", 1, 0, 0, 0, EquipConstants.NO_USER_ROLE, name);
        userPackageHelper.addEquipItem(uid, level, ueid);
        return ueid;
    }


    /**
     * 获取某个uid，某个urid下，某种类型的装备的信息
     *
     * @param uid
     * @param urid
     * @param type EquipConstants.EquipType下面的类型
     * @return
     */
    public UserEquipCO getUserEquip(long uid, long urid, String type) {
        UserEquipCO co = userEquipDAO.get(uid, urid, type);
        return co;
    }

    public List<UserEquipCO> equipList(long uid, String type) {

        List<UserEquipCO> equipList = userEquipDAO.equipList(uid, type);

        return equipList;
    }

    /**
     * 通过type和level，读取配置文件，获得对应的name
     *
     * @param type
     * @param level
     * @return
     */
    public String getEquipNameFromConfig(String type, int level) {
        EquipCO equipCO = equipConfigFetcher.get(level + "");

        if (type.equals(EquipConstants.EquipType.WEAPON)) {
            return equipCO.getWeaponName();
        } else if (type.equals(EquipConstants.EquipType.ACCESSORY)) {
            return equipCO.getAccessoryName();
        } else {
            return equipCO.getArmorName();
        }
    }

    /**
     * 获得为使用的装备列表
     *
     * @param uid
     * @param type
     * @return
     */
    public List<UserEquipCO> noUsedEquipList(long uid, String type) {
        List<UserEquipCO> list = userEquipDAO.noUsedEquipList(uid, type);
        return list;
    }
}
