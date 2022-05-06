package com.xuxd.kafka.console.interceptor;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.annotation.RequiredAuthorize;
import com.xuxd.kafka.console.beans.enums.Role;
import com.xuxd.kafka.console.beans.vo.DevOpsUserVO;
import com.xuxd.kafka.console.service.DevOpsUserService;
import com.xuxd.kafka.console.utils.ContextUtil;
import com.xuxd.kafka.console.utils.ConvertUtil;
import com.xuxd.kafka.console.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static com.xuxd.kafka.console.beans.ResponseData.TOKEN_ILLEGAL;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements AsyncHandlerInterceptor {

    private final static String TOKEN = "token";
    private final DevOpsUserService devOpsUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            String token = request.getHeader(TOKEN);
            if (StringUtils.isBlank(token)){
                log.info("token not exist");
                write(response);
                return false;
            }

            String username = JwtUtils.parse(token);
            if (StringUtils.isBlank(username)){
                log.info("{} is wrongful", token);
                write(response);
                return false;
            }

            ResponseData<DevOpsUserVO> userVORsp = devOpsUserService.detail(username);
            if (userVORsp == null || userVORsp.getData() == null){
                log.info("{} not exist", username);
                write(response);
                return false;
            }

            ContextUtil.set(ContextUtil.USERNAME, username);
            HandlerMethod method = (HandlerMethod)handler;
            RequiredAuthorize annotation = method.getMethodAnnotation(RequiredAuthorize.class);
            if (annotation != null){
                DevOpsUserVO userVO = userVORsp.getData();
                if (!userVO.getRole().equals(Role.manager)){
                    log.info("{},{} no permission", username, request.getRequestURI());
                    write(response);
                    return false;
                }
            }
        }
        return true;
    }

    private void write(HttpServletResponse response){
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(ConvertUtil.toJsonString(ResponseData.create().failed(TOKEN_ILLEGAL)));
        } catch (Exception ignored){
        } finally {
            if (writer != null){
                writer.flush();
                writer.close();
            }
        }
    }
}
