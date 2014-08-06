package com.h13.slg.battle.fight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 战斗单元
 */
public class FightUnit {

    private List<Fighter> allPos = new ArrayList<Fighter>(9){{
        add(null);
        add(null);
        add(null);
        add(null);
        add(null);
        add(null);
        add(null);
        add(null);
        add(null);
    }};

    private Fighter leader = null;

    public Fighter getLeader() {
        return leader;
    }

    public void setLeader(Fighter leader) {
        this.leader = leader;
    }

    public List<Fighter> getAllPos() {
        return allPos;
    }

    public void setAllPos(List<Fighter> allPos) {
        this.allPos = allPos;
    }
}
