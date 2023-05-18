package com.xuxd.kafka.console.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @author: xuxd
 * @date: 2023/5/18 15:49
 **/
@ToString
public class RolePermUpdateEvent extends ApplicationEvent {

    @Getter
    @Setter
    private boolean reload = false;

    public RolePermUpdateEvent(Object source) {
        super(source);
    }
}
