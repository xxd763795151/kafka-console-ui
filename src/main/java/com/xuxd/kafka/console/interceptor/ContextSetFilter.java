package com.xuxd.kafka.console.interceptor;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.config.ContextConfigHolder;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.utils.ConvertUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-05 19:56:25
 **/
@WebFilter(filterName = "context-set-filter", urlPatterns = {"/*"})
@Slf4j
public class ContextSetFilter implements Filter {

    private Set<String> excludes = new HashSet<>();

    {
        excludes.add("/cluster/info/peek");
        excludes.add("/cluster/info");
    }

    @Autowired
    private ClusterInfoMapper clusterInfoMapper;

    @Override public void doFilter(ServletRequest req, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            String uri = request.getRequestURI();
            if (!excludes.contains(uri)) {
                String headerId = request.getHeader(Header.ID);
                if (StringUtils.isBlank(headerId)) {
                    ResponseData failed = ResponseData.create().failed("Cluster id is null.");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getOutputStream().println(ConvertUtil.toJsonString(failed));
                    return;
                } else {
                    ClusterInfoDO infoDO = clusterInfoMapper.selectById(Long.valueOf(headerId));
                    ContextConfig config = new ContextConfig();

                    config.setBootstrapServer(infoDO.getAddress());
                    config.setProperties(ConvertUtil.toProperties(infoDO.getProperties()));
                    ContextConfigHolder.CONTEXT_CONFIG.set(config);
                }
            }
            chain.doFilter(req, response);
        } finally {
            ContextConfigHolder.CONTEXT_CONFIG.remove();
        }
    }

    interface Header {
        String ID = "X-Cluster-Info-Id";
        String NAME = "X-Cluster-Info-Name";
    }
}
