package com.xuxd.kafka.console.interceptor;

import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: xuxd
 * @date: 2023/5/9 21:20
 **/
@Order(1)
@WebFilter(filterName = "auth-filter", urlPatterns = {"/*"})
@Slf4j
public class AuthFilter implements Filter {

    private final AuthConfig authConfig;

    private final String TOKEN_HEADER = "X-Auth-Token";

    private final String AUTH_URI_PREFIX = "/auth";

    public AuthFilter(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!authConfig.isEnable()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = request.getHeader(TOKEN_HEADER);

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(AUTH_URI_PREFIX)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (StringUtils.isEmpty(accessToken)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Credentials credentials = AuthUtil.parseToken(authConfig.getSecret(), accessToken);
        if (credentials.isInvalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
