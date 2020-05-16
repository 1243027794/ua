package com.msyt.uav.service;

import cn.hutool.json.JSONUtil;
import com.msyt.uav.vo.Uav;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简述：
 * 一度  111 138米
 *
 * @author WangLipeng 1243027794@qq.com
 * @version 1.0
 * @since 2019/12/31 13:54
 */
@Service
@RequiredArgsConstructor
public class UavService {

    private static final HashMap<String, Boolean> status = new HashMap<>();
    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private static final String MESSAGE = "coords_received";
    private static final Double UAV_INITLONGITUDE = 116.200746;
    private static final Double UAV_INITLATITUDE = 39.704176;

    public SseEmitter generateEmitter(HttpServletResponse response, Double fireLongitude, Double fireLatitude) {
        response.setHeader("Cache-Control", "no-store");
        //指定每次请求处理的时间上限
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        if (fireLongitude == null || fireLatitude == null) {
            status.put(MESSAGE, false);
            try {
                emitter.send(JSONUtil.toJsonStr(status));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return emitter;
        }

        status.put(MESSAGE, true);
        try {
            emitter.send(JSONUtil.toJsonStr(status));
        } catch (IOException e) {
            e.printStackTrace();
        }
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        Uav uav = new Uav(fireLongitude, fireLatitude, UAV_INITLONGITUDE, UAV_INITLATITUDE);
        calculateTrackTask(emitter,uav);
        return emitter;
    }

    private void calculateTrackTask(SseEmitter emitter, Uav uav) {
        uav.takeOff(emitters, emitter);
    }
}
