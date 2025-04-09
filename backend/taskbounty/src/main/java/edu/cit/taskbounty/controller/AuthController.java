package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.service.AuthService;
import edu.cit.taskbounty.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "userId", registeredUser.getId(),
                            "username", registeredUser.getUsername(),
                            "email", registeredUser.getEmail(),
                            "birthDate", registeredUser.getBirthDate(),
                            "countryCode", registeredUser.getCountryCode(),
                            "message", "User registered successfully. Please verify your email."
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", e.getMessage()));
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("code") Long code, @CookieValue(name = "jwt") String token) {
        String username = jwtUtil.getUserNameFromJwtToken(token);
        return authService.verifyUser(username, code);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String identifier = credentials.get("identifier");
        String password = credentials.get("password");

        try {

            Optional<User> userOpt = authService.login(identifier, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtUtil.generateJwtToken(user.getUsername());

                ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(24 * 60 * 60)
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                        .body(Map.of(
                                "status", "success",
                                "data", Map.of(
                                        "userId", user.getId(),
                                        "username", user.getUsername(),
                                        "message", "Login successful"
                                )
                        ));
            }
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Invalid username/email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/resend_code")
    public ResponseEntity<String> resendVerificationCode(@CookieValue(name ="jwt") String authToken) {
        try {
            String username = jwtUtil.getUserNameFromJwtToken(authToken);
            authService.resendVerificationCode(username);
            return ResponseEntity.ok("Verification code resent successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/change_email")
    public ResponseEntity<?> changeEmail(@RequestParam String newEmail,
                                              @CookieValue(name = "jwt") String authToken) {
        try {
            String username = jwtUtil.getUserNameFromJwtToken(authToken);
            authService.changeEmail(username, newEmail);
            return ResponseEntity.ok("Email change requested. Please verify your new email.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


}
