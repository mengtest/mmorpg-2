package com.wan37.gameServer.game.bag.service;
/*
 *  @author : 钱伟健 gonefuture@qq.com
 *  @version : 2018/11/5 10:40.
 *  说明： 背包管理
 */

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.wan37.gameServer.game.bag.model.Bag;
import com.wan37.gameServer.game.bag.model.Item;
import com.wan37.gameServer.game.gameRole.model.Player;
import com.wan37.gameServer.game.gameRole.service.PlayerDataService;
import com.wan37.gameServer.game.roleProperty.model.RoleProperty;
import com.wan37.gameServer.game.things.manager.ThingsCacheMgr;
import com.wan37.gameServer.game.things.model.Things;
import com.wan37.gameServer.manager.notification.NotificationManager;
import com.wan37.mysql.pojo.entity.TBag;
import com.wan37.mysql.pojo.entity.TBagExample;
import com.wan37.mysql.pojo.mapper.TBagMapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;

/**
 * <pre> </pre>
 */

@Slf4j
@Service
public class BagsService {

    @Resource
    private PlayerDataService playerDataService;

    @Resource
    private ThingsCacheMgr thingsCacheMgr;

    @Resource
    private TBagMapper tBagMapper;


    @Resource
    private NotificationManager notificationManager;


    /**
     *
     *  展示背包格子
     */
    public Map<Integer,Item> show(Player player) {

        return player.getBag().getItemMap();
    }


    /**
     *  获取物品的物品信息
     */
    public Things getThings(Player player, Integer locationIndex) {
        Bag bag = player.getBag();
        Item item = bag.getItemMap().get(locationIndex);
        if (item == null)
            return null;
        return thingsCacheMgr.get(item.getThings().getId());
    }


    /**
     *   从数据库加载背包
     */

    public void loadBag(Player player) {
        TBagExample tBagExample = new TBagExample();
        tBagExample.or().andPlayerIdEqualTo(player.getId());
        List<TBag> tBagList = tBagMapper.selectByExample(tBagExample);

        tBagList.forEach( tBag ->  {

            // 普通背包的加载
            if (tBag.getType() == 1) {
                Bag bag = new Bag(tBag.getPlayerId(),tBag.getBagSize());
                if (!Strings.isNullOrEmpty(tBag.getItems())) {
                    Map<Integer,Item> itemMap =  JSON.parseObject(tBag.getItems(),
                            new TypeReference<Map<Integer,Item>>(){});
                    bag.setItemMap(itemMap);
                } else {
                    bag.setItemMap(new LinkedHashMap<>());
                }

                bag.setType(tBag.getType());
                bag.setBagName(tBag.getBagName());
                player.setBag(bag);

                log.debug("bag {} ", bag );
            }
        });
    }


    /**
     *  持久化背包数据
     */

    public void saveBag(Player player){
        TBag tBag = new TBag();
        Bag bag = player.getBag();
        tBag.setPlayerId(player.getId());
        tBag.setType(bag.getType());
        tBag.setBagName(bag.getBagName());
        tBag.setBagSize(bag.getBagSize());
        tBag.setItems(JSON.toJSONString(bag.getItemMap()));

        if (tBagMapper.updateByPrimaryKeySelective(tBag) == 1) {
            log.debug("更新背包成功 {}",tBag);
        } else {
            tBagMapper.insertSelective(tBag);
            log.debug("保存背包成功 {}",tBag);
        }
    }


    /**
     *  移除背包的物品
     * @param player 玩家
     * @param index 物品在背包的位置索引
     * @return 如果移除返回一个Item
     */
        public Optional<Item> removeItem(Player player, Integer index) {
            Item item = player.getBag().getItemMap().remove(index);
            return Optional.ofNullable(item);
        }

    /**
     *  从背包中寻找空位置放进去
     * @param player 玩家
     * @param item 物品条目
     * @return 物品是否放入背包成功
     */
    public boolean addItem(Player player, Item item) {
        Bag bag = player.getBag();
        if (item == null)
            return false;

        Map<Integer,Item> itemMap = bag.getItemMap();

        // 种类为3的物品为可堆叠的
        if (item.getThings().getKind() == 3) {
            for (int locationIndex=1; locationIndex <= bag.getBagSize(); locationIndex++) {
                Item i = itemMap.get(locationIndex);
                // 如果是用一种物品且堆叠未满
                if (i != null && i.getThings().getId().equals(item.getThings().getId())) {
                    i.setCount(i.getCount() + item.getCount());
                    notificationManager.notifyPlayer(player,
                            MessageFormat.format("你获得了物品{0} x {1}  \n",
                                    item.getThings().getName(),item.getCount()));
                    return true;
                }
            }
        }


        // 遍历背包所有格子，如果是空格，将物品放入格子
        for (int locationIndex=1; locationIndex <= bag.getBagSize(); locationIndex++) {
            item.setLocationIndex(locationIndex);
            if (null == bag.getItemMap().putIfAbsent(locationIndex,item)) {
                notificationManager.notifyPlayer(player,
                        MessageFormat.format("你获得了物品{0} x {1}  \n",
                                item.getThings().getName(),item.getCount()));
                return true;
            }
        }
        // 如果背包没有空位，物品放入失败,恢复物品的无栏位状态
        item.setLocationIndex(0);
        return false;
    }

    /**
     *  整理背包
     * @param ctx 通道上下文
     */
    public void packBag(ChannelHandlerContext ctx) {
        Player player = playerDataService.getPlayerByCtx(ctx);
        Bag bag = player.getBag();
        Map<Integer,Item> olderItemMap  = bag.getItemMap();
        Map<Integer,Item> newItemMap = new LinkedHashMap<>();
        // 将玩家的背包物品Map为新的空Map
        player.getBag().setItemMap(newItemMap);

        // 将物品按循序放回背包
        olderItemMap.values().forEach(
                item -> addItem(player,item)
        );
        olderItemMap = null;

        notificationManager.notifyPlayer(player,"整理背包完毕");

        bag.getItemMap().forEach((key, value) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(MessageFormat.format("格子：{0}  {1} {2} 数量：{3} 描述：{4}  属性：",
            key, value.getThings().getName(), value.getThings().getPart(),
                    value.getCount(), value.getThings().getDescribe()));
            // 遍历物品属性
            Set<RoleProperty> rolePropertyList = value.getThings().getThingRoleProperty();
            rolePropertyList.forEach(
                    roleProperty ->
                        sb.append(MessageFormat.format("{0}:{1} "
                                , roleProperty.getName(), roleProperty.getThingPropertyValue()))
            );
            notificationManager.notifyPlayer(player,sb.append("\n"));
        });
    }




}
