package ch.plaintext.schuetu.service.juriwagen;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JuriwagenFilterConfig {

    @Bean
    public FilterRegistrationBean<JuriwagenTokenFilter> juriwagenTokenFilterRegistration(JuriwagenTokenFilter filter) {
        FilterRegistrationBean<JuriwagenTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/juriwagen/*");
        registration.setOrder(1);
        return registration;
    }
}
