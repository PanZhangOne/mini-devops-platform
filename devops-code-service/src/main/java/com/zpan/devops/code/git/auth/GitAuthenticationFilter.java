package com.zpan.devops.code.git.auth;

import com.zpan.devops.code.git.GitRequestAttributes;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class GitAuthenticationFilter implements Filter {

    private static final String REALM = "Mini DevOps Git";

    private final GitBasicAuthService gitBasicAuthService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response   = (HttpServletResponse) servletResponse;

        GitAuthResult authResult = gitBasicAuthService.authenticate(request);

        if (!authResult.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().write("Git authentication failed");
            return;
        }

        GitAuthUser user =  authResult.getUser();

        request.setAttribute(GitRequestAttributes.GIT_USER_ID, user.getUserId());
        request.setAttribute(GitRequestAttributes.GIT_USERNAME, user.getUsername());

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
