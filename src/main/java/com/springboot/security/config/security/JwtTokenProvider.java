package com.springboot.security.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.List;

// https://gksdudrb922.tistory.com/217
// https://colabear754.tistory.com/171

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);
	private final UserDetailsService userDetailsService; // Spring Security 에서 제공하는 서비스 레이어

	// 기본 key 설정
	private final String baseKey = "thisisdummykeythisisdummykeythisisdummykeythisisdummykey";
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	private Key createKey() {
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(baseKey);
        return new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	}

	@PostConstruct
	protected void init() {
		LOGGER.info("[init] JwtTokenProvider 내 secretKey 초기화 시작");
		System.out.println(createKey());
		LOGGER.info("[init] JwtTokenProvider 내 secretKey 초기화 완료");
	}

	// JWT 토큰 생성
	public String createToken(String userUid, List<String> roles) {
		LOGGER.info("[createToken] 토큰 생성 시작");
		Claims claims = Jwts.claims().setSubject(userUid);
		claims.put("roles", roles);
		Date now = new Date();
		// 1시간 토큰 유효
		long tokenValidMillisecond = 1000L * 60 * 60;
		String token = Jwts.builder().setClaims(claims).setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + tokenValidMillisecond))
				.signWith(createKey(), signatureAlgorithm) // 암호화 알고리즘, secret 값 세팅
				.compact();
		LOGGER.info("[createToken] 토큰 생성 완료");
		return token;
	}

	// JWT 토큰으로 인증정보 조회
	public Authentication getAuthentication(String token) {
		LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 시작");
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
		LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 완료, UserDetails UserName : {}", userDetails.getUsername());
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	public String getUsername(String token) {
		LOGGER.info("[getUsername] 토큰 기반 회원 구별 정보 추출");
		String info = Jwts.parserBuilder().setSigningKey(createKey()).build().parseClaimsJws(token).getBody().getSubject();
		LOGGER.info("[getUsername] 토큰 기반 회원 구별 정보 추출 완료, info : {}", info);
		return info;
	}

	// HTTP Request Header에 설정된 토큰값을 가져옴
	public String resolveToken(HttpServletRequest request) {
		LOGGER.info("[resolveToken] HTTP 헤더에서 Token 값 추출");
		return request.getHeader("X-AUTH-TOKEN");
	}

	// JWT 토큰의 유효성 + 만료일 체크
	public boolean validateToken(String token) {
		LOGGER.info("[validateToken] 토큰 유효 체크 시작");
		try {
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(createKey()).build().parseClaimsJws(token);

			return !claims.getBody().getExpiration().before(new Date());

		} catch (Exception e) {
			LOGGER.info("[validateToken] 토큰 유효 체크 예외 발생");
			return false;
		}
	}
}
