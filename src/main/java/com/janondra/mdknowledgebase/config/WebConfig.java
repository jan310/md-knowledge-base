package com.janondra.mdknowledgebase.config;

import com.janondra.mdknowledgebase.user.resolver.AuthIdArgumentResolver;
import com.janondra.mdknowledgebase.document.resolver.UserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthIdArgumentResolver authIdArgumentResolver;
    private final UserIdArgumentResolver userIdArgumentResolver;

    public WebConfig(
        AuthIdArgumentResolver authIdArgumentResolver,
        UserIdArgumentResolver userIdArgumentResolver
    ) {
        this.authIdArgumentResolver = authIdArgumentResolver;
        this.userIdArgumentResolver = userIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authIdArgumentResolver);
        resolvers.add(userIdArgumentResolver);
    }

}
