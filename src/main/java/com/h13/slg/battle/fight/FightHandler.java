package com.h13.slg.battle.fight;

import com.google.common.collect.Lists;
import com.h13.slg.battle.FightConstants;
import com.h13.slg.battle.buffs.Buff;
import com.h13.slg.battle.buffs.BuffEvent;
import com.h13.slg.battle.buffs.BuffStoppedException;
import com.h13.slg.event.EventType;
import com.h13.slg.web.WebApplicationContentHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: sunbo
 * Date: 14-3-26
 * Time: 下午4:20
 * To change this template use File | Settings | File Templates.
 */
@Service
public class FightHandler {

    private List<Integer> l1 = Arrays.asList(0, 3, 6);
    private List<Integer> l2 = Arrays.asList(1, 4, 7);
    private List<Integer> l3 = Arrays.asList(2, 5, 8);

    @Autowired
    RoleSkillRunner roleSkillRunner;

    /**
     * 战斗方法
     *
     * @param attackFightUnit
     * @param defenceFightUnit
     * @return
     */
    public FightResult fight(long uid, FightUnit attackFightUnit, FightUnit defenceFightUnit) {

        boolean finished = false;
        FightResult fightResult = new FightResult();

        if (attackFightUnit.getLeader() != null) {
            roleSkillRunner.run(attackFightUnit.getLeader(),
                    attackFightUnit.getLeader().getTianfu().getRsid(),
                    attackFightUnit, defenceFightUnit
            );
        }
        if (defenceFightUnit.getLeader() != null) {
            roleSkillRunner.run(defenceFightUnit.getLeader(),
                    defenceFightUnit.getLeader().getTianfu().getRsid(),
                    defenceFightUnit, attackFightUnit);
        }

        roleSkillRunner.event(BuffEvent.BEFORE_FIGHT, attackFightUnit.getAllPos());
        roleSkillRunner.event(BuffEvent.BEFORE_FIGHT, defenceFightUnit.getAllPos());

        int round = 1;
        while (!finished) {

            roleSkillRunner.event(BuffEvent.BEFORE_ROUND, attackFightUnit.getAllPos());
            roleSkillRunner.event(BuffEvent.BEFORE_ROUND, defenceFightUnit.getAllPos());

            finished = attack(uid, attackFightUnit, defenceFightUnit, fightResult, round, true);
            if (finished) {
                fightResult.setStatus(FightResult.WIN);
                break;
            }

            finished = attack(uid, defenceFightUnit, attackFightUnit, fightResult, round, false);
            if (finished) {
                fightResult.setStatus(FightResult.LOSE);
                break;
            }

            round++;
            roleSkillRunner.event(BuffEvent.AFTER_ROUND, attackFightUnit.getAllPos());
            roleSkillRunner.event(BuffEvent.AFTER_ROUND, defenceFightUnit.getAllPos());

        }

        roleSkillRunner.event(BuffEvent.AFTER_FIGHT, attackFightUnit.getAllPos());
        roleSkillRunner.event(BuffEvent.AFTER_FIGHT, defenceFightUnit.getAllPos());

        return fightResult;
    }


    public boolean attack(long uid, FightUnit attackFightUnit, FightUnit defenceFightUnit,
                          FightResult fightResult, int round, boolean attackAttack) {
        // 每个回合从0开始到9，依次攻击
        for (int attackPos = 0; attackPos < 9; attackPos++) {
            if (attackFightUnit.getAllPos().get(attackPos) == null)
                continue;

            // Todo:确定这次攻击是否需要群体攻击

            int defencePos = selectDefencePos(attackPos, defenceFightUnit);
            if (defencePos == -1) {
                return true;
            }

            // 开始战斗
            Fighter attackPosition = attackFightUnit.getAllPos().get(attackPos);
            Fighter defencePosition = defenceFightUnit.getAllPos().get(defencePos);

            // 开始计算伤害
            // Todo: 未来需要详细处理这块内容，现在就是简单的计算方法
            int attack = attackPosition.getAttack();
            int defence = defencePosition.getDefence();
            int damage = 0;
            if (attack > defence)
                damage = attack;
            else
                damage = 10;

            int remainHealth = defencePosition.getHealth() - damage;
            defencePosition.setHealth(remainHealth <= 0
                    ? 0 : remainHealth);

            FightLog fightLog = new FightLog();
            // 攻击方一定是人
            if (attackAttack)
                fightLog.setAttack(FightConstants.ATTACK_ATTACK);
            else
                fightLog.setAttack(FightConstants.DEFENCE_ATTACK);
            PosInfo attackPosInfo = new PosInfo();
            attackPosInfo.setId(attackPosition.getId());
            attackPosInfo.setName(attackPosition.getName());
            attackPosInfo.setPos(attackPos);
            fightLog.setAttackPos(attackPosInfo);


            PosInfo defencePosInfo = new PosInfo();
            defencePosInfo.setId(defencePosition.getId());
            defencePosInfo.setName(defencePosition.getName());
            defencePosInfo.setPos(defencePos);
            LinkedList<PosInfo> defencePosList = Lists.newLinkedList();
            defencePosList.add(defencePosInfo);
            fightLog.setDefencePos(defencePosList);
            FightStatus attackStatus = new FightStatus();
            attackStatus.setPos(attackPos);
            attackStatus.setStatus(new Integer[]{attackPosition.getHealth(), 0});
            FightStatus defenceStatus = new FightStatus();
            defenceStatus.setPos(defencePos);
            defenceStatus.setStatus(new Integer[]{defencePosition.getHealth(), damage * -1});
            fightLog.setAttackStatus(attackStatus);
            fightLog.setDefenceStatus(defenceStatus);

            fightResult.addLog(round, fightLog);
        }


        // 检查是否defence方是否都死了
        boolean allDead = true;
        for (int defencePos = 0; defencePos < 9; defencePos++) {
            if (defenceFightUnit.getAllPos().get(defencePos) != null
                    && defenceFightUnit.getAllPos().get(defencePos).getHealth() > 0) {
                allDead = false;
                break;
            }
        }
        if (allDead)
            return true;

        return false;
    }


    /**
     * 为对应的攻击者选择一个防守它这次攻击的人
     * 如果返回-1标示没有人存活，战斗结束
     *
     * @param attackPos
     * @param defenceFightUnit
     * @return
     */
    private int selectDefencePos(int attackPos, FightUnit defenceFightUnit) {
        // 查看竖排有攻击可能性么？
        // 查看 她到底在那一竖排上，看看她对应的竖排有没有可能被攻击的人
        if (l1.contains(attackPos)) {
            // 这一竖排是否有可以攻击的
            for (int i : l1) {
                if (defenceFightUnit.getAllPos().get(i) != null &&
                        defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                    return i;
            }
        }
        if (l2.contains(attackPos)) {
            // 这一竖排是否有可以攻击的
            for (int i : l2) {
                if (defenceFightUnit.getAllPos().get(i) != null &&
                        defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                    return i;
            }
        }
        if (l3.contains(attackPos)) {
            // 这一竖排是否有可以攻击的
            for (int i : l3) {
                if (defenceFightUnit.getAllPos().get(i) != null &&
                        defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                    return i;
            }
        }

        // l1 ,l2 , l3 一次查看
        for (int i : l1) {
            if (defenceFightUnit.getAllPos().get(i) != null &&
                    defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                return i;
        }
        for (int i : l2) {
            if (
                    defenceFightUnit.getAllPos().get(i) != null &&
                            defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                return i;
        }
        for (int i : l3) {
            if (defenceFightUnit.getAllPos().get(i) != null &&
                    defenceFightUnit.getAllPos().get(i).getHealth() > 0)
                return i;
        }

        return -1;
    }
}
