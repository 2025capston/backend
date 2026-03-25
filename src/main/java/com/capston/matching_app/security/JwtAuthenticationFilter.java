package com.capston.matching_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    //private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {

               //Jwt claims에서 직접 꺼내면 DB조회 없음
                Claims claims = jwtTokenProvider.parse(token).getBody();
                String email = claims.getSubject();
                Integer userId = claims.get("userId",Integer.class);
                String role = claims.get("role", String.class);

                logger.info("JWT 인증 성공, 사용자: {}", email);

                // loadUserByUsername()내부 -> userRepository.findByEmail(email) ->DB조회 발생
                CustomUserDetails customUser = new CustomUserDetails(userId, email, role);

                //CustomUserDetails customUser = (CustomUserDetails) userDetailsService.loadUserByUsername(email);


                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        customUser,
                        null,
                        customUser.getAuthorities()
                );


                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ExpiredJwtException e) {
                logger.warn("만료된 JWT 토큰: {}", e.getMessage());
            } catch (SignatureException e) {
                logger.warn("JWT 서명 불일치: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.warn("잘못된 JWT 토큰: {}", e.getMessage());
            } catch (Exception e) {
                logger.warn("JWT 처리 중 오류 발생: {}", e.getMessage());
            }
        } else {
            logger.debug("Authorization 헤더 없음 또는 Bearer 형식 아님");
        }

        filterChain.doFilter(request, response);
    }
}
