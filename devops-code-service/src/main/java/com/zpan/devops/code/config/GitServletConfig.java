package com.zpan.devops.code.config;

import com.zpan.devops.code.git.DisabledReceivePackFactory;
import com.zpan.devops.code.git.GitHttpRepositoryResolver;
import com.zpan.devops.code.git.GitReceivePackFactory;
import com.zpan.devops.code.git.GitUploadPackFactory;
import com.zpan.devops.code.git.auth.GitAuthenticationFilter;
import com.zpan.devops.code.git.auth.GitBasicAuthService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.http.server.GitServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
@RequiredArgsConstructor
public class GitServletConfig {

    private final GitHttpRepositoryResolver gitHttpRepositoryResolver;

    private final GitUploadPackFactory gitUploadPackFactory;

    private final GitReceivePackFactory gitReceivePackFactory;

    private final GitBasicAuthService gitBasicAuthService;

    @Bean
    public ServletRegistrationBean<GitServlet> gitServletRegistrationBean() {
        GitServlet gitServlet = new GitServlet();
        gitServlet.setRepositoryResolver(gitHttpRepositoryResolver);
        gitServlet.setUploadPackFactory(gitUploadPackFactory);
        gitServlet.setReceivePackFactory(gitReceivePackFactory);
        ServletRegistrationBean<GitServlet> registrationBean =
                new ServletRegistrationBean<>(gitServlet, "/git/*");

        registrationBean.setName("gitServlet");
        registrationBean.setLoadOnStartup(1);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<GitAuthenticationFilter> gitAuthenticationFilter() {
        GitAuthenticationFilter filter = new GitAuthenticationFilter(gitBasicAuthService);
        FilterRegistrationBean<GitAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setName("gitAuthenticationFilter");
        registrationBean.addUrlPatterns("/git/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
