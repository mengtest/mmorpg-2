package com.wan37.gameserver.model;

import com.wan37.gameserver.game.player.model.Buffer;
import com.wan37.gameserver.game.skills.model.Pet;
import com.wan37.gameserver.game.skills.model.Skill;


import java.util.List;
import java.util.Map;

/**
 * @author gonefuture  gonefuture@qq.com
 * time 2018/11/8 15:38
 * @version 1.00
 * Description: 场景内的活物
 */


public interface Creature   {

    /** 活物的id */
    Long getId();


    /** 活物的名字 **/
    String getName();


    /** 活物的血量 */
    Long getHp();
    void setHp(Long hp);

    /** 活物的mp */
    Long getMp();
    void setMp(Long hp);


    /** 活物的状态，生存 or 死亡 */
    Integer getState();
    void setState(Integer state);


    /**  活物当前使用的技能 */
    Map<Integer, Skill> getHasUseSkillMap();
    void setHasUseSkillMap(Map<Integer, Skill> skillMap);

    /** 活物的当前buffer */
    List<Buffer> getBufferList();
    void setBufferList(List<Buffer> bufferList);


    /** 活物的当前目标 */
    Creature getTarget();
    void setTarget(Creature target);


    /**
     *  获得宠物
     * @return 宠物
     */
    Pet getPet();

    /**
     *  设置宠物
     * @param pet 宠物
     */
    void setPet(Pet pet);






}
