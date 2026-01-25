package com.example.sample.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("within(com.example.sample.controller..*) && !@annotation(com.example.sample.annotation.NoLogAspect)")
    public Object logging(ProceedingJoinPoint pjp) throws Throwable {
        String params = getRequestParams();

        long startAt = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        log.info("[{}]-REQUEST {} [{}] [{}]",  method.getName(), params, request.getRequestURI(), request.getRemoteHost());


        Object result = pjp.proceed();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        long endAt = System.currentTimeMillis();

        log.info("[{}]-RESPONSE [status : {}] ({}ms)",  method.getName(), response != null ? response.getStatus() : null, endAt - startAt);

        return result;
    }

    private String getRequestParams() {
        String params = "";

        RequestAttributes requestAttribute = RequestContextHolder.getRequestAttributes();

        if (requestAttribute != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();

            Map<String, String[]> paramMap = request.getParameterMap();

            if (!paramMap.isEmpty()) {
                params = "[" + paramMapToString(paramMap) + "]";
            }
        }
        return params;
    }

    private String paramMapToString(Map<String, String[]> paramMap) {
        return paramMap.entrySet().stream()
                .map(entry -> String.format("%s -> (%s)", entry.getKey(), String.join(",", entry.getValue())))
                .collect(Collectors.joining(", "));
    }

}
