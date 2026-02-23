package com.ticketing.common.security;

import com.ticketing.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository userRepository;
    private static final String[] PERMIT_URL_ARRAY = {
            "/",
            "/signup",
            "/error"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // REST API 경로는 401 Unauthorized 반환 (리다이렉트 없음)
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                            } else {
                                // 일반 페이지는 로그인 페이지로 리다이렉트
                                response.sendRedirect("/login");
                            }
                        })
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/login")
                );

        return http.build();
    }

    /**
     * DB에서 이메일로 사용자를 조회하여 Spring Security 인증 컨텍스트에 제공.
     * soft delete 된 사용자(deleted_dt IS NOT NULL)는 조회 대상에서 제외.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmailAndDeletedDtIsNull(email)
                .map(CustomUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 비밀번호 인코더 빈 등록.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
