package com.xuxd.kafka.console.beans;

import java.util.List;
import lombok.Getter;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-30 20:10:07
 **/
public class CounterList<T> {

    @Getter
    private List<T> list;

    private int total;

    public CounterList(List<T> list) {
        this.list = list;
    }

    public int getTotal() {
        return list != null ? list.size() : 0;
    }

    @Override public String toString() {
        return "CounterList{" +
            "list=" + list +
            ", total=" + getTotal() +
            '}';
    }
}
