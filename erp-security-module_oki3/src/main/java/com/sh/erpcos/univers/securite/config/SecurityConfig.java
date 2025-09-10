package com.sh.erpcos.univers.securite.config;

import com.sh.erpcos.univers.securite.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Mettre à jour la dernière connexion
            userDetailsService.updateLastLogin(authentication.getName());
            log.info("Connexion réussie pour l'utilisateur: {}", authentication.getName());
            
            // Obtenir le contexte de l'application depuis la requête
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/dashboard");
        };
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Pages publiques
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**","/select2/**","/images/**").permitAll()
                
                // Pages d'erreur
                .requestMatchers("/error/**").permitAll()
                
                // API publiques (si nécessaire)
                .requestMatchers("/api/public/**").permitAll()
                
               // .requestMatchers("/pontbascule/**").permitAll()
                
                // Pont bascule - nécessite des permissions spécifiques
                .requestMatchers("/pontbascule/**").hasAnyAuthority(
                    "PONTBASCULE_LIRE", "PONTBASCULE_CREER", "PONTBASCULE_MODIFIER", "PONTBASCULE_SUPPRIMER"
                )
                
              
                // Sécurité - nécessite des permissions spécifiques
                .requestMatchers("/securite/**").hasAnyAuthority(
                    "SECURITE_LIRE", "SECURITE_CREER", "SECURITE_MODIFIER", "SECURITE_SUPPRIMER"
                )
                
                // Contact - nécessite des permissions spécifiques
                .requestMatchers("/contact/**").hasAnyAuthority(
                    "CONTACT_LIRE", "CONTACT_CREER", "CONTACT_MODIFIER", "CONTACT_SUPPRIMER"
                )
                
                // Comptabilité - nécessite des permissions spécifiques
                .requestMatchers("/comptabilite/**").hasAnyAuthority(
                    "COMPTABILITE_LIRE", "COMPTABILITE_CREER", "COMPTABILITE_MODIFIER", "COMPTABILITE_SUPPRIMER"
                )
                
                // RH - nécessite des permissions spécifiques
                .requestMatchers("/rh/**").hasAnyAuthority(
                    "RH_LIRE", "RH_CREER", "RH_MODIFIER", "RH_SUPPRIMER"
                )
                
                // Stock - nécessite des permissions spécifiques
                .requestMatchers("/stock/**").hasAnyAuthority(
                    "STOCK_LIRE", "STOCK_CREER", "STOCK_MODIFIER", "STOCK_SUPPRIMER"
                )
                
                // Vente - nécessite des permissions spécifiques
                .requestMatchers("/vente/**").hasAnyAuthority(
                    "VENTE_LIRE", "VENTE_CREER", "VENTE_MODIFIER", "VENTE_SUPPRIMER"
                )
                
                // Achat - nécessite des permissions spécifiques
                .requestMatchers("/achat/**").hasAnyAuthority(
                    "ACHAT_LIRE", "ACHAT_CREER", "ACHAT_MODIFIER", "ACHAT_SUPPRIMER"
                )
                
                // Dashboard - accessible à tous les utilisateurs authentifiés
                .requestMatchers("/dashboard").authenticated()
                
                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                
                .defaultSuccessUrl("/dashboard", true)
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(request -> request.getRequestURI().equals("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );
        return http.build();
    }
}
