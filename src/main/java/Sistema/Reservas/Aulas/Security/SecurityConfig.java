package Sistema.Reservas.Aulas.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String API = "/api/**";
    // Rutas de la API de datos (Aulas, Cursos, Usuarios y Reservas)
    private static final String[] API_DATOS = {
            "/api/aulas", "/api/aulas/**",
            "/api/cursos", "/api/cursos/**",
            "/api/usuarios", "/api/usuarios/**",
            "/api/reservas", "/api/reservas/**"
    };
    private static final String LOGIN = "/login";
    private static final String REGISTRO = "/registro";
    private static final String LOGOUT = "/logout";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * API REST: las rutas de datos (/api/aulas, cursos, usuarios y reservas) son públicas si api.publica=true; si no, usan HTTP Basic.
     * Al no usar cookies de sesión no necesita token CSRF.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http,
                                           @Value("${api.publica:true}") boolean apiPublica) throws Exception {
        http
                .securityMatcher(API)
                .authorizeHttpRequests(auth -> {
                    // api.publica=true (por defecto): se prueba en Postman sin cuenta ni contraseña.
                    // api.publica=false: toda la API exige HTTP Basic (correo y contraseña).
                    if (apiPublica) {
                        auth.requestMatchers(API_DATOS).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** Páginas web: todo requiere sesión salvo el login, el registro y los recursos estáticos. */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http,
                                           LoginSuccessHandler loginSuccessHandler,
                                           CerrarSesionHandler cerrarSesionHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(LOGIN, REGISTRO, "/error", "/css/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage(LOGIN)
                        .loginProcessingUrl(LOGIN)
                        .usernameParameter("correo")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)   // guarda el nombre en la HttpSession
                        .failureUrl(LOGIN + "?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl(LOGOUT)              // POST + token CSRF (lo agrega Thymeleaf)
                        .addLogoutHandler(cerrarSesionHandler)   // invalida la HttpSession
                        .deleteCookies("JSESSIONID")             // borra la cookie de sesión del navegador
                        .logoutSuccessUrl(LOGIN + "?logout")
                        .permitAll());
        return http.build();
    }
}
