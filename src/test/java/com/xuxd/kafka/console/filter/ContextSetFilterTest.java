package com.xuxd.kafka.console.filter;

import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ContextSetFilterTest {

    @Test
    void shouldWriteChineseFailureMessageAsUtf8() throws Exception {
        ContextSetFilter filter = new ContextSetFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/topic/list");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(MediaType.parseMediaType(response.getContentType()).getCharset())
                .isEqualTo(StandardCharsets.UTF_8);
        assertThat(response.getContentAsString()).contains("没有集群信息，请先切换集群");
        verifyNoInteractions(chain);
    }

    @Test
    void shouldWriteMissingClusterMessageAsUtf8() throws Exception {
        ContextSetFilter filter = new ContextSetFilter();
        ReflectionTestUtils.setField(filter, "clusterInfoMapper", mock(ClusterInfoMapper.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/topic/list");
        request.addHeader(ContextSetFilter.Header.ID, "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentAsString()).contains("该集群找不到信息，请切换一个有效集群");
        verifyNoInteractions(chain);
    }
}
