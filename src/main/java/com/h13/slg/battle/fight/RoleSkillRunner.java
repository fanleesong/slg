package com.h13.slg.battle.fight;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.h13.slg.battle.FightConstants;
import com.h13.slg.battle.buffs.Buff;
import com.h13.slg.battle.buffs.BuffEvent;
import com.h13.slg.battle.buffs.BuffStoppedException;
import com.h13.slg.battle.buffs.SanWeiBuff;
import com.h13.slg.config.co.RoleSkillCO;
import com.h13.slg.config.fetcher.RoleSkillConfigFetcher;
import com.h13.slg.core.log.SlgLogger;
import com.h13.slg.core.log.SlgLoggerEntity;
import com.h13.slg.core.util.SlgStrings;
import com.h13.slg.role.RoleConstants;
import com.h13.slg.skill.helper.RoleSkillHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * 在以下两种情况的时候，这个类发挥作用
 * 1. 战斗开始前，触发双方的天赋技能
 * 2. 战斗进行时，当将领技能被出发的时候
 * <p/>
 * 主要功能为解析roleSkill，然后解析相关参数，给对方或者己方增加buff
 */
@Service
public class RoleSkillRunner {

    @Autowired
    RoleSkillConfigFetcher roleSkillConfigFetcher;


    /**
     * 发送天赋技能的时候触发
     *
     * @param tianfuRoleSkillId 天赋技能id
     * @param attack            发动技能方
     * @param defence           被动方
     */
    public void run(int round, int uid, Fighter originFighter, int tianfuRoleSkillId, FightUnit attack, FightUnit defence,
                    FightResult fightResult, String owner) {
        RoleSkillCO roleSkillCO = roleSkillConfigFetcher.get(tianfuRoleSkillId + "");

        final String runAttack = roleSkillCO.getRunParamsAttack();
        final String runDefence = roleSkillCO.getRunParamsDefence();
        final String runHealth = roleSkillCO.getRunParamsHealth();
        String runDamage = roleSkillCO.getRunParamsDamage();
        String runRound = roleSkillCO.getRunRound();
        String runType = roleSkillCO.getRunType();
        String runTarget = roleSkillCO.getRunTarget();
        String runStart = roleSkillCO.getRunStart();


        // 获取buff释放对象
        List<Fighter> fighterList = getBuffTargetList(originFighter, runTarget, runType, attack, defence);
        SlgLogger.debug(SlgLoggerEntity.p("battle", "fight", uid, "get buff target List")
                .addParam("list", logFightList(fighterList)));


        // 如果三个其中有一个不为0的话
        // 则开启 三围的buff
        if (!SlgStrings.isZeroOrEmptyOrNull(runAttack)
                || !SlgStrings.isZeroOrEmptyOrNull(runDefence)
                || !SlgStrings.isZeroOrEmptyOrNull(runHealth)) {

            addSanWeiBuff(uid, fighterList, originFighter, runAttack, runDefence, runHealth, runRound);

            // 记录日志
            FightSkillLog skillLog = new FightSkillLog();
            skillLog.setName(roleSkillCO.getName());
            skillLog.setOwner(owner);
            skillLog.setPos(originFighter.getPos());
            skillLog.setTarget(roleSkillCO.getRunTarget());
            skillLog.setSkillType("sanwei");
            skillLog.setType("startSkill");
            skillLog.setRoleName(originFighter.getName());
            skillLog.setStatus(new LinkedList<String>() {{
                add(runAttack);
                add(runDefence);
                add(runHealth);
            }});

            fightResult.addLog(round, skillLog);
        }

        //
        if (runStart.equals(FightConstants.BuffStartTime.NOW)) {
            if (runRound.equals(FightConstants.Round.GLOBAL)) {
                event(BuffEvent.BEFORE_FIGHT, fighterList, fightResult, 0);
            } else {
                event(BuffEvent.BEFORE_ROUND, fighterList, fightResult, 0);
            }

        }


    }

    /**
     * 触发事件
     *
     * @param buffEvent
     * @param fights
     */
    public void event(BuffEvent buffEvent, List<Fighter> fights, FightResult fightResult, int curRound) {

        for (Fighter f : fights) {
            if (f == null)
                continue;
            List<Buff> newBuffList = Lists.newLinkedList();
            for (Buff buff : f.getBuffList()) {
                try {
                    buff.trigger(buffEvent, f, fightResult, curRound);
                } catch (BuffStoppedException e) {
                    // 去掉某个buff
                }
                newBuffList.add(buff);
            }
            f.setBuffList(newBuffList);
        }


    }


    private void addSanWeiBuff(int uid, List<Fighter> fighterList,
                               Fighter originFighter, String runAttack, String runDefence, String runHealth,
                               String runRound) {

        for (Fighter f : fighterList) {
            if (f == null)
                continue;

            SanWeiBuff buff = new SanWeiBuff(uid,
                    originFighter.getId(),
                    new Integer(runAttack), new Integer(runDefence), new Integer(runHealth),
                    new Integer(runRound)
            );
            f.getBuffList().add(buff);
            SlgLogger.debug(SlgLoggerEntity.p("battle", "fight", uid, "add buff")
                    .addParam("buffName",    "sanwei")
                    .addParam("fighter.id", f.getId()));
        }

    }

    private List<Fighter> getBuffTargetList(Fighter originFighter,
                                            String runTarget,
                                            String runType,
                                            FightUnit attack,
                                            FightUnit defence) {
        List<Fighter> fighters = Lists.newLinkedList();
        FightUnit fightUnit = null;
        if (runTarget.equals(FightConstants.Target.ZIJI)) {
            fightUnit = attack;
        } else {
            fightUnit = defence;
        }
        if (runType.equals(FightConstants.BuffType.DANTI)) {
            fighters.add(originFighter);
        } else if (runType.equals(FightConstants.BuffType.GONG)
                || runType.equals(FightConstants.BuffType.QI)
                || runType.equals(FightConstants.BuffType.QIANG)) {
            fighters = findXi(runType, fightUnit);
        } else if (runType.equals(FightConstants.BuffType.ALL)) {
            fighters = Lists.newArrayList(fightUnit.getAllPos());
        } else {
            // 十字
        }

        return fighters;
    }

    private List<Fighter> findXi(String xi, FightUnit fightUnit) {
        List<Fighter> fighters = Lists.newLinkedList();
        for (Fighter f : fightUnit.getAllPos()) {
            if (f.getSoldierType().equals(xi)) {
                fighters.add(f);
            }
        }
        return fighters;
    }


    private String logFightList(List<Fighter> fighterList) {
        List<Integer> ids = Lists.newLinkedList();
        for (Fighter f : fighterList) {
            if (f == null) {
                ids.add(0);
            } else
                ids.add(f.getId());
        }
        return Joiner.on(",").join(ids.iterator());


    }

}
