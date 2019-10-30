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

    // Could only be used in this sample
    public Map<String,Integer> transfer() { // TODO 1.1
        r.lock();
        try {
            Map<String,Integer> tempMap = new HashMap<>();

            Integer present_A = map.get("A");
            Integer present_B = map.get("B");
            Integer present_C = map.get("C");

            BigDecimal t1;
            BigDecimal t2;

            t1 = new BigDecimal(Double.toString(Math.random()));
            t2 = new BigDecimal(Double.toString(present_A));
            int toBeSent_A = t1.multiply(t2).intValue();

            t1 = new BigDecimal(Double.toString(Math.random()));
            t2 = new BigDecimal(Double.toString(present_B));
            int toBeSent_B = t1.multiply(t2).intValue();

            t1 = new BigDecimal(Double.toString(Math.random()));
            t2 = new BigDecimal(Double.toString(present_C));
            int toBeSent_C = t1.multiply(t2).intValue();

            tempMap.put("A", toBeSent_A);
            this.map.replace("A", present_A - toBeSent_A);

            tempMap.put("B", toBeSent_B);
            this.map.replace("B", present_B - toBeSent_B);

            tempMap.put("C", toBeSent_C);
            this.map.replace("C", present_C - toBeSent_C);

            return tempMap;

        } finally {
            r.unlock();
        }
    }

    public void transfer( Map<String,Integer> m ) {
        //  TODO 1.2
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

    public static void main(String[] args) {
        Cache free = new Cache();

        System.out.println("A"+free.get("A"));
        System.out.println("B"+free.get("B"));
        System.out.println("C"+free.get("C"));

        Map<String,Integer> fuck;

        fuck = free.transfer();

        System.out.println("sentA"+fuck.get("A"));
        System.out.println("sentB"+fuck.get("B"));
        System.out.println("sentC"+fuck.get("C"));

        System.out.println("A"+free.get("A"));
        System.out.println("B"+free.get("B"));
        System.out.println("C"+free.get("C"));

        free.receive(fuck);

        System.out.println("A"+free.get("A"));
        System.out.println("B"+free.get("B"));
        System.out.println("C"+free.get("C"));
    }
}
