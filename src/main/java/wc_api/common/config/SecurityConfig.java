//package wc_api.common.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//@RequiredArgsConstructor
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    // CORS 설정
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.addAllowedOrigin("*");
//        configuration.addAllowedMethod("*");
//        configuration.addAllowedHeader("*");
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    // 패스워드 인코더
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    // 보안 필터 체인
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(Customizer.withDefaults())
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(request -> request
//                        .anyRequest().permitAll()
//                )
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable);
//        return http.build();
//    }
//}