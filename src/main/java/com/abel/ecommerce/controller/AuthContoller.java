package com.abel.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abel.ecommerce.entity.Pengguna;
import com.abel.ecommerce.model.JwtRespone;
import com.abel.ecommerce.model.LoginRequest;
import com.abel.ecommerce.model.RefreshTokenRequest;
import com.abel.ecommerce.model.SignupRequest;
import com.abel.ecommerce.security.jjwt.JwtUtils;
import com.abel.ecommerce.security.service.UserDetailsImpl;
import com.abel.ecommerce.security.service.UserDetailsServiceImpl;
import com.abel.ecommerce.service.PenggunaService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthContoller {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    @PostMapping("/signin")
    public ResponseEntity<JwtRespone> authenticateUser(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshJwtToken(authentication);
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok()
                .body(new JwtRespone(token, refreshToken, principal.getUsername(), principal.getEmail()));
    }

    @PostMapping("/signup")
    public Pengguna signup(@RequestBody SignupRequest request) {
        Pengguna pengguna = new Pengguna();
        pengguna.setId(request.getUsername());
        pengguna.setEmail(request.getEmail());
        pengguna.setPassword(passwordEncoder.encode(request.getPassword()));
        pengguna.setNama(request.getNama());
        pengguna.setRoles("admin");

        Pengguna created = penggunaService.create(pengguna);
        return created;
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtRespone> refreshToken(@RequestBody RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        boolean valid = jwtUtils.validateJwtToken(token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsImpl, null,
                userDetailsImpl.getAuthorities());
        String newToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshJwtToken(authentication);
        return ResponseEntity.ok(new JwtRespone(newToken, refreshToken, username, userDetailsImpl.getEmail()));
    }

}
