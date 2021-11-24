package com.xuxd.kafka.console.beans.enums;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-24 19:38:00
 **/
public enum ThrottleUnit {
    KB, MB;

    public long toKb(long size) {
        if (this == MB) {
            return 1024 * size;
        }
        return size;
    }
}
