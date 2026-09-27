package br.com.sams.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ApplicationScoped
public class PasswordEncoderProducer {

    @Produces
    @ApplicationScoped
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
