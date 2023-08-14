package com.springboot.security.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

// https://velog.io/@404-nut-pound/Spring-Security-AntPathRequestMatcher-%EC%98%A4%EB%A5%98-%EC%B2%98%EB%A6%AC
// https://github.com/spring-projects/spring-security/issues/13609
// https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/util/matcher/AntPathRequestMatcher.html
// https://colabear754.tistory.com/171
// https://hoonzi-text.tistory.com/121
// https://github.com/patroklos83/springboot3_jwtauth

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] PERMIT_ALL_PATTERNS = new String[]{
            // method
            "/sign-api/**", "/",
            "/sign-api/sign-up/**",
            "/product/**",
            "/",
            "/sign-api/sign-in", "/sign-api/sign-up",

            // swagger
            "/v3/**", "/swagger/**", "/swagger-resources/**",
            "/swagger-ui.html", "/webjars/**",
            "/swagger-ui/**"
    };

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(c -> c.disable())// REST API는 csrf 보안이 필요 없으므로 비활성화
                // .csrf((csrf) -> csrf.disable()); 새 방식
                .cors(cr -> cr.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("localhost:8080"));
                    config.setAllowedMethods(Arrays.asList("GET","POST"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setMaxAge(3600L); //1시간
                    return config;
                }))

                // JWT Token 인증방식으로 세션은 필요 없으므로 비활성화
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
//                        .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
                                .xssProtection(xXssConfig -> xXssConfig.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                )

                // Request에 대한 사용권한 체크s
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(Stream
                                .of(PERMIT_ALL_PATTERNS) // 가입 및 로그인 주소는 허용
                                .map(AntPathRequestMatcher::antMatcher)
                                .toArray(AntPathRequestMatcher[]::new)).permitAll()
                        .anyRequest().hasRole("ADMIN")  // 그 외의 모든 요청은 인증 필요
                )

                // 나머지 요청은 인증된 ADMIN만 접근 가능
                .exceptionHandling(exceptHandling -> exceptHandling
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                )

                // JWT Token 필터를 id/password 인증 필터 이전에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedOrigins(List.of("localhost:8080"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET","POST"));
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
