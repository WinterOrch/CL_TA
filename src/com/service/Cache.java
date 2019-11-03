package com.service;

import com.SocketConstant;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A Sort of Read and Write Locked Map Generified Class
 * created in 18:21 20
 * 19/10/13
 */
public class Cache extends HashMap<String,Integer>{

    private final Map<String, Integer> map;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock r = lock.readLock();
    private final Lock w = lock.writeLock();

    public Cache() {
        this.map = new HashMap<>();

        this.map.put("A", SocketConstant.CACHE_INITIALIZED_SIZE);
        this.map.put("B", SocketConstant.CACHE_INITIALIZED_SIZE);
        this.map.put("C", SocketConstant.CACHE_INITIALIZED_SIZE);
    }

    public Integer put(String key, Integer value) {
        w.lock();
        try {
            return map.put(key, value);
        } finally {
            w.unlock();
        }
    }

    public Integer get(Object key) {
        r.lock();
        try {
            return map.get(key);
        } finally {
            r.unlock();
        }
    }

    /**
     * 随机生成小于SocketConstant.TRANSFER_MAX_SIZE的转账金额，可自行调整
     */
    public Map<String,Integer> transfer() {
        Map<String,Integer> tempMap = new HashMap<>();

        BigDecimal t1;
        BigDecimal t2;

        t1 = new BigDecimal(Double.toString(Math.random()));
        t2 = new BigDecimal(SocketConstant.TRANSFER_MAX_SIZE);
        int toBeSent_A = t1.multiply(t2).intValue();

        t1 = new BigDecimal(Double.toString(Math.random()));
        int toBeSent_B = t1.multiply(t2).intValue();

        t1 = new BigDecimal(Double.toString(Math.random()));
        int toBeSent_C = t1.multiply(t2).intValue();

        tempMap.put("A", toBeSent_A);
        tempMap.put("B", toBeSent_B);
        tempMap.put("C", toBeSent_C);
        return tempMap;
    }


    public void transfer( Map<String,Integer> m ) {
        r.lock();
        try {
            Integer present_A = map.get("A");
            Integer present_B = map.get("B");
            Integer present_C = map.get("C");
            this.map.replace("A", present_A - m.get("A"));
            this.map.replace("B", present_B - m.get("B"));
            this.map.replace("C", present_C - m.get("C"));
        } finally {
            r.unlock();
        }
    }


    public void receive( Map<String,Integer> m ) {
        r.lock();
        try {
            Integer present_A = map.get("A");
            Integer present_B = map.get("B");
            Integer present_C = map.get("C");

            this.map.replace("A", present_A + m.get("A"));
            this.map.replace("B", present_B + m.get("B"));
            this.map.replace("C", present_C + m.get("C"));
        } finally {
            r.unlock();
        }
    }

    public void print() {
        r.lock();
        try {
            System.out.println("A "+map.get("A")+" B "+map.get("B")+" C "+map.get("C"));
        } finally {
            r.unlock();
        }
    }

}
