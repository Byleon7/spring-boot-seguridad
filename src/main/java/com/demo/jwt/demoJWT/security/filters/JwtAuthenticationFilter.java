package com.demo.jwt.demoJWT.security.filters;

import com.demo.jwt.demoJWT.entities.User;
import com.demo.jwt.demoJWT.security.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;

//Filtro utilizado para autenticacion y generacion del token
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    //El request es la peticion de login con los datos del usuario (Se ejecuta cuando se realiza una solicitud HTTP POST a /login)
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username, password;

        try {
            //El json con los datos del usuario que quiere loguearse llega como un input stream
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        //Por debajo el authenticate, llama al metodo loadUserByUsername de JpaUserDetailsService gracias a implementar UserDetailsService
        //Para obtener el usuario de la base de datos y comparar su contrasenha con la recibida
        return authenticationManager.authenticate(authenticationToken);
    }

    //Este metodo se ejecuta posterior a si la autenticacion es correcta
    //Se genera el jwt token
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        //Se obtiene el usuario autenticado
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String username = user.getUsername();
        //Una coleccion de cualquier tipo que herede de GrantedAuthority
        Collection<? extends GrantedAuthority> authorities =  user.getAuthorities();
        String authoritiesJson = new ObjectMapper().writeValueAsString(authorities);

        //Claims extiende Map
        Claims claims = Jwts.claims()
                .add("authorities", authoritiesJson)
                .build();

        //Generacion del token JWT
        String token = Jwts.builder()
                .subject(username) //Nombre de usuario logueado
                .claims(claims) //Datos adicionales, es opcional, en este caso los roles
                .expiration(new Date(System.currentTimeMillis() + 3600000)) //Fecha de Expiracion del token, es opcional. (En este caso en una hora, hora actual + 1 hora en milisegundos)
                .issuedAt(new Date()) //Fecha de creacion del token
                .signWith(TokenJwtConfig.SECRET_KEY)  //Se firma con la clave secreta
                .compact();

        //En el response en un atributo de cabecera se retornara el token al front
        //'Bearer ' es un estandar de JWT solo en el header
        response.addHeader(TokenJwtConfig.HEADER_AUTHORIZATION, TokenJwtConfig.PREFIX_TOKEN + token);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("token", token);
        body.put("message", "Ha iniciado sesion satisfactoriamente");

        response.getWriter().write(new ObjectMapper().writeValueAsString(body)); //Convierte el map a un json string
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    //Este metodo se ejecuta posterior a si la autenticacion es incorrecta
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Error en la autenticacion, username o password incorrectos");
        body.put("error", failed.getMessage()); //El error producido en la autenticacion

        response.getWriter().write(new ObjectMapper().writeValueAsString(body)); //Convierte el map a un json string
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
