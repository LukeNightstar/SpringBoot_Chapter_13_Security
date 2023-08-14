package com.springboot.security.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.security.data.dto.EntryPointErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // 인증 실패 시 결과를 처리해주는 로직을 가지고 있는 클래스
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
						 AuthenticationException e) throws IOException, ServletException {
		ObjectMapper objectMapper = new ObjectMapper();
		LOGGER.info("[commence] 인증 실패로 response.sendError 발생");
		EntryPointErrorResponse entryPointErrorResponse = new EntryPointErrorResponse();
		entryPointErrorResponse.setMsg("인증이 실패하였습니다.");
		httpServletResponse.setStatus(401);
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("utf-8");
		httpServletResponse.getWriter().write(objectMapper.writeValueAsString(entryPointErrorResponse));
	}
}
