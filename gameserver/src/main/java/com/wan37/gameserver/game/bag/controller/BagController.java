package com.wan37.gameserver.game.bag.controller;

import com.wan37.common.entity.Message;
import com.wan37.common.entity.Cmd;
import com.wan37.gameserver.game.bag.model.Bag;
import com.wan37.gameserver.game.bag.model.Item;
import com.wan37.gameserver.game.bag.service.BagsService;
import com.wan37.gameserver.game.player.model.Player;
import com.wan37.gameserver.game.player.service.PlayerDataService;
import com.wan37.gameserver.game.roleProperty.model.RoleProperty;
import com.wan37.gameserver.manager.controller.ControllerManager;
import com.wan37.gameserver.manager.notification.NotificationManager;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author gonefuture  gonefuture@qq.com
 * time 2018/11/28 15:05
 * @version 1.00
 * Description: 背包相关
 */


@Controller
@Slf4j
public class BagController  {
    {
        ControllerManager.add(Cmd.PACK_BAG,this::packBag);
        ControllerManager.add(Cmd.SHOW_BAGS,this::showBag);
    }

    @Resource
    private BagsService bagsService;

    @Resource
    private PlayerDataService playerDataService;

    @Resource
    private NotificationManager notificationManager;


    public void packBag(ChannelHandlerContext ctx, Message message) {
        bagsService.packBag(ctx);
    }



    public void showBag(ChannelHandlerContext ctx, Message message) {
        Player player = playerDataService.getPlayerByCtx(ctx);
        if (Objects.isNull(player)) {
            NotificationManager.notifyByCtxWithMsgId(ctx,"背包栏： 角色尚未登陆".toString(),message.getMsgId());
            return;
        }


        Map<Integer, Item> itemMap = bagsService.show(player);
        log.debug("itemMap {}",itemMap);

        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format("背包：{0} 大小: {1}",
                player.getBag().getBagName(),player.getBag().getBagSize())).append("\n");

        if (0 == itemMap.size() ) {
            sb.append("背包空荡荡的");
        }
        for (Map.Entry<Integer,Item> entry : itemMap.entrySet()) {
            sb.append(MessageFormat.format("格子：{0}  {1} {2} 数量：{3} 描述：{4}  属性：",
                    entry.getKey(), entry.getValue().getThingInfo().getName(), entry.getValue().getThingInfo().getPart(),
                    entry.getValue().getCount() ,entry.getValue().getThingInfo().getDescribe()));
            // 遍历物品属性
            Map<Integer,RoleProperty> rolePropertyList = entry.getValue().getThingInfo().getThingRoleProperty();
            rolePropertyList.values().forEach(
                    roleProperty -> sb.append(MessageFormat.format("{0}:{1} "
                                ,roleProperty.getName(),roleProperty.getThingPropertyValue()))
            );
            sb.append(MessageFormat.format(" 等级：{0}，价格：{1}",
                    Optional.ofNullable(entry.getValue().getThingInfo().getLevel()).orElse(0),
                    Optional.ofNullable(entry.getValue().getThingInfo().getPrice()).orElse(0)));
            sb.append("\n");
        }

        NotificationManager.notifyByCtxWithMsgId(ctx,sb.toString(),message.getMsgId());
    }
}
