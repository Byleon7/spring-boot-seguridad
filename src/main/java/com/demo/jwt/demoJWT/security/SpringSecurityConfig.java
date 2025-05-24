package com.demo.jwt.demoJWT.security;

import com.demo.jwt.demoJWT.security.filters.JwtAuthenticationFilter;
import com.demo.jwt.demoJWT.security.filters.JwtValidationFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//Se creo una clase Configuration para centralizar lo necesario con respecto a la seguridadd
@Configuration
public class SpringSecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    //Se define el Bean AuthenticationManager que es utilizado en filtro JwtAuthenticationFilter para manejar la autenticacion
    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //Interfaz PasswordEncoder que utilizara BCryptPasswordEncoder como implementacion para encriptar las contrasenhas de usuarios
    @Bean
    PasswordEncoder passwordEncoder(){
        //Hay varias implementaciones de PasswordEncoder, en este caso usamos una de las mas recomendadas BCrypt
        //Si no declaramos esto, Sprin no va a saber que implementacion de PasswordEncoder debe usar
        return new BCryptPasswordEncoder();
    }


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/error").permitAll() //Ante cualquier error Spring redirecciona a esta URL, dando mas informacion, por ello la hacemos publica
                .requestMatchers( "/v3/api-docs/**", "/swagger-ui/**","/swagger-ui.html" ).permitAll() //Urls publicas para acceder a documentacion, /v3/api-docs/** para ver en formato JSON segun especificacion Open Api, lo demas  mediante la interfaz Swagger UI
                .requestMatchers(HttpMethod.GET,"/api/users/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").authenticated()
                .anyRequest().authenticated())
                .addFilterBefore(new JwtValidationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtAuthenticationFilter(authenticationManager())) //Se agrega filtro personalizado a la cadena de filtros (Al final posterior a los filtros estandar de Spring)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    //Este es un Bean que se usa para personalizar metadatos globales para documentacion con Springdoc con implementacion Open API
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API JWT")
                        .version("1.0")
                        .description("Documentación de API de ejemplo para Iiplementacion de JWT"));
    }


    //Security Filter Chain (Cadenas de filtros de seguridad) serie de filtros que se aplican a cada request http
    //Los filtros trabajan en conjunto para realizar tareas de seguridad (Autenticacion, Autorizacion, gestion de sesiones, etc)
    /*@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/users/**").permitAll() //Regla 1: Esta url es de acceso publico
                .anyRequest().authenticated()) //Regla 2: Cualquier otro request requiere de autenticacion
                .csrf(AbstractHttpConfigurer::disable) //CSFG es un token token oculto generado por el servidor para dar seguridad a formularios (Como no se trabaja con vistas o formulario del lado del servidor se deshabilita, ya que esto es un api REST)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //stateless: significa que el servidor no almacena información sobre el estado del usuario entre solicitudes.
                .build();
    }*/
}

//**CONTEXTO DE ESTADO
//Estado puede ser cualquier cosa que el servidor recuerde sobre un usuario entre una petición y la siguiente (Si esta logueado o no, preferencias, etc.)

//**STATELESS (Sin Estado)
//Spring no crea ni usa objetos HttpSession para recordar al usuario entre peticiones.
//Cada petición del cliente debe contener toda la información necesaria para que el servidor la procese, sin depender de una sesión guardada en memoria o en base de datos.


