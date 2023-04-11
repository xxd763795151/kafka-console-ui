package com.xuxd.kafka.console.aspect;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 晓东哥哥
 */
@Slf4j
@Order(-1)
@Aspect
@Component
public class ControllerLogAspect {

    private Map<String, String> descMap = new HashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    @Pointcut("@annotation(com.xuxd.kafka.console.aspect.annotation.ControllerLog)")
    private void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuilder params = new StringBuilder("[");
        try {
            String methodName = getMethodFullName(joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());

            if (!descMap.containsKey(methodName)) {
                cacheDescInfo(joinPoint);
            }

            Object[] args = joinPoint.getArgs();
            long startTime = System.currentTimeMillis();
            Object res = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            for (int i = 0; i < args.length; i++) {
                params.append(args[i]);
            }
            params.append("]");

            String resStr = "[" + (res != null ? res.toString() : "") + "]";

            StringBuilder sb = new StringBuilder();
            String shortMethodName = descMap.getOrDefault(methodName, ".-");
            shortMethodName = shortMethodName.substring(shortMethodName.lastIndexOf(".") + 1);
            sb.append("[").append(shortMethodName)
                    .append("调用完成: ")
                    .append("请求参数=").append(params).append(", ")
                    .append("响应值=").append(resStr).append(", ")
                    .append("耗时=").append(endTime - startTime)
                    .append(" ms");
            log.info(sb.toString());
            return res;
        } catch (Throwable e) {
            log.error("调用方法异常， 请求参数：" + params, e);
            throw e;
        }
    }

    private void cacheDescInfo(ProceedingJoinPoint joinPoint) {
        lock.lock();
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> aClass = joinPoint.getTarget().getClass();

            Method method = null;
            try {
                Object[] args = joinPoint.getArgs();

                Class<?>[] clzArr = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    clzArr[i] = args[i].getClass();
                }
                method = aClass.getDeclaredMethod(methodName, clzArr);

            } catch (Exception e) {
                log.warn("cacheDescInfo error: {}", e.getMessage());
            }

            String fullMethodName = getMethodFullName(aClass.getName(), methodName);
            String desc = "[" + fullMethodName + "]";
            if (method == null) {
                descMap.put(fullMethodName, desc);
                return;
            }

            ControllerLog controllerLog = method.getAnnotation(ControllerLog.class);
            String value = controllerLog.value();
            if (StringUtils.isBlank(value)) {
                descMap.put(fullMethodName, desc);
            } else {
                descMap.put(fullMethodName, value);
            }
        } finally {
            lock.unlock();
        }
    }

    private String getMethodFullName(String className, String methodName) {
        return className + "#" + methodName;
    }
}
