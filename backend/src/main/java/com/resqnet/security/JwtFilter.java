package com.resqnet.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtUtil.extractClaims(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                // --- Debugging logs ---
                System.out.println("🔑 Incoming JWT: " + token);
                System.out.println("👤 Email from token: " + email);
                System.out.println("🛡️ Raw role from token: " + role);

                // --- Normalize role ---
                if (role != null && role.startsWith("ROLE_")) {
                    role = role.substring(5); // strip leading ROLE_
                }
                System.out.println("✅ Normalized role used: " + role);

                // Grant authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("✔️ Authenticated user: " + email + " with ROLE_" + role);

            } catch (Exception e) {
                System.err.println("❌ JWT validation failed: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
