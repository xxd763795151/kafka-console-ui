package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {
    private String token;
    private Role role;
}
