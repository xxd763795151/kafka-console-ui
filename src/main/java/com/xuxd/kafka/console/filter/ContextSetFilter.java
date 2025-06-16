package com.xuxd.kafka.console.filter;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.config.ContextConfigHolder;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-05 19:56:25
 **/
@Order(100)
@WebFilter(filterName = "context-set-filter", urlPatterns = {"/acl/*", "/user/*", "/cluster/*", "/config/*", "/consumer/*", "/message/*", "/topic/*", "/op/*", "/client/*"})
@Slf4j
public class ContextSetFilter implements Filter {

    private Set<String> excludes = new HashSet<>();

    {
        excludes.add("/cluster/info/peek");
        excludes.add("/cluster/info");
        excludes.add("/config/console");
    }

    @Autowired
    private ClusterInfoMapper clusterInfoMapper;

    @Override
    public void doFilter(ServletRequest req, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            String uri = request.getRequestURI();
            if (!excludes.contains(uri)) {
                String headerId = request.getHeader(Header.ID);
                String specificId = request.getHeader(Header.SPECIFIC_ID);
                if (StringUtils.isNotBlank(specificId)) {
                    headerId = specificId;
                }
                if (StringUtils.isBlank(headerId)) {
//                    ResponseData failed = ResponseData.create().failed("Cluster info is null.");
                    ResponseData failed = ResponseData.create().failed("没有集群信息，请先切换集群");
                    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                    response.getWriter().println(ConvertUtil.toJsonString(failed));
                    return;
                } else {
                    ClusterInfoDO infoDO = clusterInfoMapper.selectById(Long.valueOf(headerId));
                    if (infoDO == null) {
                        ResponseData failed = ResponseData.create().failed("该集群找不到信息，请切换一个有效集群");
                        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                        response.getWriter().println(ConvertUtil.toJsonString(failed));
                        return;
                    }
                    ContextConfig config = new ContextConfig();
                    config.setClusterInfoId(infoDO.getId());
                    config.setClusterName(infoDO.getClusterName());

                    config.setBootstrapServer(infoDO.getAddress());
                    if (StringUtils.isNotBlank(infoDO.getProperties())) {
                        config.setProperties(ConvertUtil.toProperties(infoDO.getProperties()));
                    }
                    ContextConfigHolder.CONTEXT_CONFIG.set(config);
//                    log.info("current kafka config: {}", config);
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
        String SPECIFIC_ID = "X-Specific-Cluster-Info-Id";
    }
}
