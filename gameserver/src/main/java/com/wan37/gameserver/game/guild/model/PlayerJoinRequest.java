package com.wan37.gameserver.game.guild.model;

import com.wan37.gameserver.game.player.model.Player;
import lombok.Data;

import java.util.Date;

/**
 * @author gonefuture  gonefuture@qq.com
 * time 2018/12/20 18:20
 * @version 1.00
 * Description: 玩家的入会申请
 */
@Data
public class PlayerJoinRequest {

    public PlayerJoinRequest(boolean isAgree, Date date, Player player) {
        this.isAgree = isAgree;
        this.date = date;
        this.player = player;
    }



    private boolean isAgree;

    private Date date;

    Player player;
}
