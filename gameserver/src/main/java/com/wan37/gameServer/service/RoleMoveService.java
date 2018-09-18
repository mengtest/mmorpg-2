package com.wan37.gameServer.service;

import com.wan37.gameServer.common.MapMarker;
import com.wan37.gameServer.entity.map.GameMap;
import com.wan37.gameServer.entity.map.Position;
import com.wan37.gameServer.entity.role.Adventurer;
import com.wan37.gameServer.entity.role.Role;
import com.wan37.gameServer.manager.GameCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

/**
 * @author gonefuture  gonefuture@qq.com
 * time 2018/9/12 11:44
 * @version 1.00
 * Description: JavaLearn
 */

@Service
@Slf4j
public class RoleMoveService {

    private GameMap gameMap = new GameMap("桃花源",10,10);
    private MapMarker[][] map = new MapMarker[10][10];
    private  static GameCacheManager gameCacheManager = GameCacheManager.getInstance();




    RoleMoveService() {
        // 初始化
        initMap();
        log.info("初始化成功");
    }

    // 初始化地图
    private void initMap() {
        for (int i=0; i < 10; i++) {
            for (int j=0; j < 10; j++) {
                if (i == j) {
                    map[i][j] = MapMarker.HILL;
                } else {
                    map[i][j] = MapMarker.ROAD;
                }
            }
        }
        // 设置地图
        gameMap.setMap(map);
    }

    public String move() {

        return currentLocation();
    }

    public String currentLocation() {
        Role role = (Adventurer) gameCacheManager.get("hero");
        Position p = role.getPosition();
        map[p.getX()][p.getY()] = MapMarker.ROLE;
        return gameMap.StringMap();
    }



}