package com.sovon9.activity_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.graphql.path:/graphql}")
    private String graphqlPath;

    @Value("${spring.graphql.graphiql.path:/graphiql}")
    private String graphiqlPath;

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(req->req.anyRequest().permitAll())
//                .csrf(csrf->csrf.disable());
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(req->
                        req.requestMatchers(HttpMethod.GET, graphiqlPath).permitAll()
                                .requestMatchers(HttpMethod.GET, graphqlPath).permitAll()
                                .anyRequest().authenticated())
                .csrf(csrf->csrf.disable())
                .oauth2ResourceServer(oauth2->oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
