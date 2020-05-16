package com.msyt.uav.controller;


import com.msyt.uav.service.UavService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;


/**
 * 简述：
 *
 * @author WangLipeng 1243027794@qq.com
 * @version 1.0
 * @since 2019/12/31 12:23
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class UavSseController {
    private final UavService uavService;

    @GetMapping("takeoff")
    public SseEmitter handle(HttpServletResponse response, Double longitude, Double latitude) {
        return uavService.generateEmitter(response, longitude, latitude);
    }
}
