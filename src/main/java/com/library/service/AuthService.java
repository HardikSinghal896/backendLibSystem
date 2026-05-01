package com.library.service;

import com.library.dto.Dto;
import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.session.SessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    private final UserRepository userRepo;
    private final SessionStore sessionStore;

    public Map<String, Object> login(String email, String password) {
        // Admin check
        if (adminEmail.equals(email) && adminPassword.equals(password)) {
            String token = sessionStore.createSession(-1L, true);
            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("role", "ADMIN");
            resp.put("name", "Admin");
            resp.put("userId", -1);
            return resp;
        }

        // Regular user
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

//        if (!"APPROVED".equals(user.getStatus())) {
//            if ("PENDING".equals(user.getStatus()))
//                throw new RuntimeException("Account pending admin approval");
//            throw new RuntimeException("Account has been rejected by admin");
//        }

        String token = sessionStore.createSession(user.getId(), false);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("role", "USER");
        resp.put("name", user.getName());
        resp.put("userId", user.getId());
        return resp;
    }

    public User register(Dto.RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setMobile(req.getMobile());
        user.setAadharNumber(req.getAadharNumber());
        user.setAadharPdf(req.getAadharPdf());
        user.setAdmissionDate(req.getAdmissionDate());
        user.setPassword(req.getPassword()); // NOTE: hash in production
        user.setStatus("PENDING");
        return userRepo.save(user);
    }
}