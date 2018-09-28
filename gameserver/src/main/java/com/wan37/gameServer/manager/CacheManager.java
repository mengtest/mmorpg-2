package com.wan37.gameServer.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.wan37.mysql.pojo.entity.GameRole;
import com.wan37.mysql.pojo.entity.Player;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

/**
 * @author gonefuture  gonefuture@qq.com
 * time 2018/9/26 14:34
 * @version 1.00
 * Description: mmorpg
 */
@Slf4j
public class CacheManager {

    public static Cache<Object, Object> gameCache = CacheBuilder.newBuilder()
            // 设置并发级别，最多8个线程同时写
            .concurrencyLevel(8)
            // 设置写缓存后，三小时过期
            .expireAfterWrite(3, TimeUnit.HOURS)
            // 设置缓存容器的初始容量为100
            .initialCapacity(100)
            .maximumSize(200)
            .recordStats()
            .removalListener(
                    notification -> System.out.println(notification.getKey() + "was removed, cause is" + notification.getCause())
            ).build();


    public static void cachePlayer(long id , Player player) {
        String key = "player:"+id;
        gameCache.put(key,player);
    }

    public static Player getPlayer(long id) {
        return (Player) gameCache.getIfPresent("player:"+id);
    }


    public static void cacheGameRole(long id , GameRole gameRole) {
        String key = "gameRole:"+id;
        gameCache.put(key,gameRole);
    }

    public static GameRole getGameRole(long id) {
        return (GameRole) gameCache.getIfPresent("gameRole:"+id);
    }


    public static void put(Object key, Object value) {
        gameCache.put(key, value);
    }

    public static Object get(Object key) {
        return gameCache.getIfPresent(key);
    }

}
