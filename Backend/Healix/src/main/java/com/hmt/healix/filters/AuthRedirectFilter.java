package com.hmt.healix.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if((uri.equals("/login") || uri.equals("/register") || uri.equals("/verify"))
        && request.getUserPrincipal() != null) {
            if(request.isUserInRole("ADMIN")) {
                response.sendRedirect("/dashboard/adminDashboard");
                return;
            }
            else if(request.isUserInRole("DOCTOR")) {
                response.sendRedirect("/dashboard/doctorDashboard");
                return;
            }
            else if(request.isUserInRole("PATIENT")) {
                response.sendRedirect("/dashboard/patientDashboard");
                return;
            }


        }
        filterChain.doFilter(request, response);
    }
}
