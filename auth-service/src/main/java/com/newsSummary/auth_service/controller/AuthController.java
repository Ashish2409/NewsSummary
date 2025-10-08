package com.newsSummary.auth_service.controller;


import com.newsSummary.auth_service.dto.LoginRequest;
import com.newsSummary.auth_service.dto.RegisterRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.newsSummary.auth_service.repository.UserRepository;
import com.newsSummary.auth_service.security.JwtUtil;
import com.newsSummary.auth_service.model.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder= new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, JwtUtil jwtUtil){
        this.repo= repo;
        this.jwtUtil= jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        if (repo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email Already Exists");
        }
        User user = User.builder().userName(request.getUsername())
                .email(request.getEmail()).role("USER")
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        repo.save(user);
        return "User Registered Successfully!";
    }
    @PostMapping("/login")
    public String login (@RequestBody LoginRequest request){
        User user= repo.findByEmail(request.getEmail()).orElseThrow(()->
                new RuntimeException("User Not Found"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid Password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }





}
