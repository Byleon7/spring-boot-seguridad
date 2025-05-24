package com.demo.jwt.demoJWT.security.filters;

import com.demo.jwt.demoJWT.security.SimpleGrantedAuthorityJsonCreator;
import com.demo.jwt.demoJWT.security.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//Con cada peticion http mediante este filtro se validara el token jwt que envia el cliente
public class JwtValidationFilter extends OncePerRequestFilter {
//Segun lei OncePerRequestFilter es ideal para validar tokens

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String headerValue = request.getHeader(TokenJwtConfig.HEADER_AUTHORIZATION);

        if(headerValue == null || !headerValue.startsWith(TokenJwtConfig.PREFIX_TOKEN)){
            //Es un recurso publico, se continua con la cadena de filtros
            chain.doFilter(request, response);
            return;
        }
        //Se obtiene el token sin el prefijo estandar
        String token = headerValue.replace(TokenJwtConfig.PREFIX_TOKEN, "");

        try {
            //Se obtiene el payload (Claims o datos adicionales) del token en caso que sea valido
            //Si el token esta expirado o la clave es incorrecta se lanza una excepcion
            Claims claims = Jwts.parser().verifyWith(TokenJwtConfig.SECRET_KEY).build().parseClaimsJws(token).getPayload();
            String username = claims.getSubject();
            String authoritiesText = (String) claims.get("authorities");
            byte[] authoritiesBytes = authoritiesText.getBytes();
            //Se obtienen los roles en el formato de objeto necesario
            Collection<? extends GrantedAuthority> authorities = Arrays.asList(
                    new ObjectMapper()
                            //SimpleGrantedAuthorityJsonCreator se creo para poblar el atributo role de SimpleGrantedAuthority en base al atributo authority de GrantedAuthority
                            //Si no se hubiera creado dicha clase, SimpleGrantedAuthority no encontraria el atributo role en GrantedAuthority durante la conversion de JSON a objeto
                            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
                            .readValue(authoritiesBytes, SimpleGrantedAuthority[].class)
            );

            //El password no aplica, solo cuando se crea el token como en el filtro JwtAuthenticationFilter
            //Nos autenticamos nuevamente pasando solo los datos que contiene el token
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response); //Se continua con la cadena de filtros
        }catch (JwtException e){
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "Token invalido");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        }
    }
}
