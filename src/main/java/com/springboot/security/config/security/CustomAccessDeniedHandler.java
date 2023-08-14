package com.springboot.security.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // 권한이 없는 예외가 발생했을 경우 핸들링하는 클래스
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

	@Override
	public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
					   AccessDeniedException e) throws IOException, ServletException {
		LOGGER.info("[handle] 접근이 막혔을 경우 경로 리다이렉트");
		httpServletResponse.sendRedirect("/sign-api/exception");
	}
}
