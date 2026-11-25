package br.mainpass.acesso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import br.mainpass.acesso.auth.JwtFilter;
import br.mainpass.acesso.auth.JwtService;
import br.mainpass.acesso.usuario.UsuarioRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt, UsuarioRepository usuarios) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/login", "/v3/api-docs/**",
                            "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                    // Area do convidado e chat do convite (a autorizacao fina por convite fica no service)
                    .requestMatchers("/api/convidado/**").hasRole("CONVIDADO")
                    .requestMatchers("/api/convites/*/mensagens/**").hasAnyRole("USUARIO", "CONVIDADO")
                    .requestMatchers("/api/convites/**").hasRole("USUARIO")
                    // Todo o resto continua exigindo login, mas CONVIDADO fica de fora por padrao:
                    // um novo endpoint nunca nasce acessivel ao convidado por esquecimento.
                    .anyRequest().hasAnyRole("ADMIN", "OPERADOR", "USUARIO"))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"status\":401,\"codigo\":\"NAO_AUTENTICADO\",\"mensagem\":\"Token ausente ou invalido\"}");
                })
                .accessDeniedHandler((request, response, accessException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"status\":403,\"codigo\":\"ACESSO_NEGADO\",\"mensagem\":\"Voce nao tem permissao para esta operacao\"}");
                }))
            .addFilterBefore(new JwtFilter(jwt, usuarios), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
