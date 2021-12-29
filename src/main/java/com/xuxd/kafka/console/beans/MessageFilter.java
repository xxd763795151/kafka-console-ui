package com.xuxd.kafka.console.beans;

import com.xuxd.kafka.console.beans.enums.FilterType;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-29 15:30:08
 **/
public class MessageFilter {

    private FilterType filterType = FilterType.NONE;

    private Object searchContent = null;

    private String headerKey = null;

    private String headerValue = null;

    private Deserializer deserializer = null;

    private boolean isContainsValue = false;

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public Object getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(Object searchContent) {
        this.searchContent = searchContent;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    public Deserializer getDeserializer() {
        return deserializer;
    }

    public void setDeserializer(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public boolean isContainsValue() {
        return isContainsValue;
    }

    public void setContainsValue(boolean containsValue) {
        isContainsValue = containsValue;
    }
}
