package com.cantina.biometric.config;

import com.cantina.biometric.exception.PayloadTooLargeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestSizeFilter extends OncePerRequestFilter {

    private final long requestMaxBytes;

    public RequestSizeFilter(@Value("${biometric.request-max-bytes:1048576}") long requestMaxBytes) {
        this.requestMaxBytes = requestMaxBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength > requestMaxBytes) {
            throw new PayloadTooLargeException("Request body exceeds configured REQUEST_MAX_BYTES=" + requestMaxBytes);
        }
        filterChain.doFilter(request, response);
    }
}
