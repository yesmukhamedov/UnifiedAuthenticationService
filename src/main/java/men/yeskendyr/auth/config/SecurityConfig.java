package men.yeskendyr.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import men.yeskendyr.auth.security.JwtAuthenticationEntryPoint;
import men.yeskendyr.auth.security.JwtAuthenticationFilter;
import men.yeskendyr.auth.service.TokenService;
import men.yeskendyr.auth.config.AuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenService tokenService,
                                                   ObjectMapper objectMapper) throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenService, entryPoint);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health")
                        .permitAll()
                        .requestMatchers("/.well-known/**", "/api/v1/auth/**", "/auth/login", "/auth/refresh")
                        .permitAll()
                        .requestMatchers("/oauth2/introspect").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AuthProperties authProperties, PasswordEncoder passwordEncoder) {
        String clientId = authProperties.getIntrospection().getClientId();
        String clientSecret = authProperties.getIntrospection().getClientSecret();
        UserDetails user = User.withUsername(clientId)
                .password(passwordEncoder.encode(clientSecret))
                .roles("INTROSPECTION")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
