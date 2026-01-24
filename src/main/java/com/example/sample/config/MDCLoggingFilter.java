package com.example.sample.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MDCLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        UUID uniqueId = UUID.randomUUID();
        MDC.put("x-api-id", uniqueId.toString());
        log.trace("Request IP address is {}", servletRequest.getRemoteAddr());
        log.trace("Request content type is {}", servletRequest.getContentType());
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
        filterChain.doFilter(servletRequest, responseWrapper);
        responseWrapper.setHeader("x-api-id", uniqueId.toString());
        responseWrapper.copyBodyToResponse();
        log.trace("Response header is set with uuid {}", responseWrapper.getHeader("x-api-id"));
    }

}
