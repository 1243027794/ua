package com.msyt.uav.controller;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简述：
 *
 * @author WangLipeng 1243027794@qq.com
 * @version 1.0
 * @since 2020/1/14 11:55
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class TestController {
    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("test")
    public SseEmitter generateEmitter(HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store");
        //指定每次请求处理的时间上限
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        returnBack(emitter);
        return emitter;
    }


    private void returnBack(SseEmitter emitter) {
        new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    emitter.send("hello world!!!");
                } catch (Exception e) {
                    emitters.remove(emitter);
                    break;
                }
            }
        }).start();
    }
}
