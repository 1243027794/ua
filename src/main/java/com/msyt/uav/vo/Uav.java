package com.msyt.uav.vo;

import com.msyt.uav.domain.UavData;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 简述：
 *
 * @author WangLipeng 1243027794@qq.com
 * @version 1.0
 * @since 2019/12/31 16:00
 */
public class Uav {

    private static final Double STEP = 0.0005; /*0.00005;*/
    private static final Double ALTITUDE = 100.0;
    private static final Integer ALTITUDE_STEP = 2;
    private static final Integer INTERVAL_TIME = 3000;

    private Double fireLongitude;
    private Double fireLatitude;
    private Double uavLongitude;
    private Double uavLatitude;
//    private UavData uavData = new UavData();


    private int stepNumber;
    private int takeDownNumber;
    private double uavAltitude;
    private double latitudeStep;
    private double longitudeStep;

    public Uav(Double fireLongitude,
               Double fireLatitude,
               Double uavLongitude,
               Double uavLatitude) {
        this.fireLongitude = fireLongitude;
        this.fireLatitude = fireLatitude;
        this.uavLongitude = uavLongitude;
        this.uavLatitude = uavLatitude;
        stepNumber = (int) (Math.sqrt(Math.pow(fireLatitude - uavLatitude, 2) +
                Math.pow(fireLongitude - uavLongitude, 2)) / STEP);
        takeDownNumber = stepNumber;
        latitudeStep = (fireLatitude - uavLatitude) / stepNumber;
        longitudeStep = (fireLongitude - uavLongitude) / stepNumber;
    }

    private UavData flight(Boolean isToFire) {
        int toFire = -1;
        if (isToFire) {
            toFire = 1;
            uavAltitude += ALTITUDE_STEP;
        }
        if (!isToFire && takeDownNumber-- <= 50) {
            uavAltitude -= ALTITUDE_STEP;
        }
        if (uavAltitude >= ALTITUDE) {
            uavAltitude = ALTITUDE;
        }
        uavLongitude += toFire * longitudeStep;
        uavLatitude += toFire * latitudeStep;

        UavData uavData = new UavData();
        //200毫秒飞行0.00005度
        uavData.setLatitude(uavLatitude);
        uavData.setLongitude(uavLongitude);
        uavData.setAltitude(uavAltitude);
        return uavData;
    }

    public void takeOff(CopyOnWriteArrayList<SseEmitter> emitters, SseEmitter emitter) {
        new Thread(() -> {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int flightTimes = this.stepNumber;
            int uavTakeoffTimes = 2 * flightTimes;
            boolean toFire = true;

//            try {
//                Thread.sleep(ThreadLocalRandom.current().nextInt(180000) + 120000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            while (uavTakeoffTimes-- > 0) {
                UavData uavData = null;
                try {
                    Thread.sleep(INTERVAL_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (toFire) {
                    uavData = this.flight(true);
                    if (flightTimes-- == 0) {
                        toFire = false;
                    }
                }
                if (!toFire) {
                    uavData = this.flight(false);
                }
                try {
                    emitter.send(uavData);
                } catch (IOException e) {
                    emitters.remove(emitter);
                    break;
                }
            }
        }).start();
    }
}