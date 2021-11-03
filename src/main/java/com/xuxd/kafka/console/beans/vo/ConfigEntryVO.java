package com.xuxd.kafka.console.beans.vo;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.kafka.clients.admin.ConfigEntry;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-03 15:32:08
 **/
@Data
public class ConfigEntryVO implements Comparable {

    private static final Map<String, Integer> ORDER_DICTIONARY = new HashMap<>();

    static {
        ORDER_DICTIONARY.put("DYNAMIC_TOPIC_CONFIG", 0);
        ORDER_DICTIONARY.put("DYNAMIC_BROKER_LOGGER_CONFIG", 1);
        ORDER_DICTIONARY.put("DYNAMIC_BROKER_CONFIG", 2);
        ORDER_DICTIONARY.put("DYNAMIC_DEFAULT_BROKER_CONFIG", 3);
        ORDER_DICTIONARY.put("DEFAULT_CONFIG", 4);
        ORDER_DICTIONARY.put("STATIC_BROKER_CONFIG", 5);
        ORDER_DICTIONARY.put("UNKNOWN", 6);
    }

    private String name;

    private String value;

    private String source;

    private boolean sensitive;

    private boolean readOnly;

    public static ConfigEntryVO from(ConfigEntry entry) {
        ConfigEntryVO vo = new ConfigEntryVO();
        vo.name = entry.name();
        vo.value = entry.value();
        vo.source = entry.source().name();
        vo.sensitive = entry.isSensitive();
        vo.readOnly = entry.isReadOnly();
        return vo;
    }

    @Override public int compareTo(Object o) {

        ConfigEntryVO that = (ConfigEntryVO) o;

        if (!this.source.equals(that.source)) {
            return ORDER_DICTIONARY.get(this.source) - ORDER_DICTIONARY.get(that.source);
        }
        if (this.readOnly != that.readOnly) {
            return this.readOnly ? 1 : -1;
        }

        return this.name.compareTo(that.name);
    }
}
