package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String country = request.get("country");

            User newUser = authService.registerUser(username, email, password, country);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", newUser.getId());
            data.put("username", newUser.getUsername());
            data.put("email", newUser.getEmail());
            data.put("message", "User registered successfully");

            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String password = request.get("password");

            User user = authService.authenticateUser(email, password);
            String token = authService.generateJwtToken(user);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("token", token);
            data.put("message", "Login successful");

            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            // Returning 400 for invalid format or 401 for unauthorized can be handled separately if needed.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
