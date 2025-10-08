package com.news_summary.news_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtFilter extends OncePerRequestFilter {


    private String secret="mysupersecretkey_for_testing12345";

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws
            ServletException, IOException
    {

        String header= request.getHeader("Authorization");

        if(header==null || !header.startsWith("Bearer ")){
            logger.info("inside the header validation");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Missing Or Invalid Token");
            return;

        }
        String token = header.substring(7);
        logger.info(token);
        try{
            Key key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims= Jwts.parserBuilder().setSigningKey(key)
                    .build().parseClaimsJws(token).getBody();
            request.setAttribute("userEmail",claims.getSubject());

        }
        catch (Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid Token");
            return;
        }
        filter.doFilter(request,response);
    }


}
