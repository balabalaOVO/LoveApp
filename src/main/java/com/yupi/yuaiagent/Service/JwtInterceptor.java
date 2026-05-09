package com.yupi.yuaiagent.Service;

import com.yupi.yuaiagent.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 尝试从 Header 获取 token（支持 token 头和 Authorization: Bearer 头）
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 2. 如果 Header 中没有，再从请求参数获取
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        //3. 验证token
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Missing or invalid Authorization header or token parameter\"}");
            return false;
        }

        //将用户邮箱（账号）存入request中，这样后续接口获取当前登录用户，直接request.getAttribute("currentUserEmail")即可
        String email = jwtUtil.getEmailFromToken(token);
        request.setAttribute("currentUserEmail", email);
        return true;
    }
}
