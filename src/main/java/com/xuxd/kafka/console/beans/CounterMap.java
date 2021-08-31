package com.xuxd.kafka.console.beans;

import java.util.Map;
import lombok.Getter;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-31 21:05:14
 **/
public class CounterMap<K, E> {

    @Getter
    private Map<K, E> map;

    @Getter
    private int total;

    public CounterMap(Map<K, E> map) {
        this.map = map;
        this.total = this.map != null ? this.map.size() : 0;
    }

}
