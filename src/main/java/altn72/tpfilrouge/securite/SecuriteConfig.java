package altn72.tpfilrouge.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecuriteConfig implements WebMvcConfigurer {

    // Les mots de passe sont stockés hachés en BCrypt (cf. data.sql)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Toutes les pages et l'API sont protégées, sauf la connexion, la page d'erreur et les fichiers statiques
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ConnexionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico");
    }
}
