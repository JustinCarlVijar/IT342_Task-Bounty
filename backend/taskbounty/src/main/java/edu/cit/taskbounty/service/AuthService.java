package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user.
     *
     * @param user The user object containing registration details.
     * @return The saved user.
     */
    public User register(User user) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (user.isDisabled()) {
            throw new RuntimeException("You account is disabled");
        }

        // Check email syntax
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(user.getEmail());

        if (!matcher.matches()) {
            throw new RuntimeException("Invalid Email Syntax");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        user.setVerified(false); // Set default to unverified

        User savedUser = userRepository.save(user);

        // Generate and send verification email
        String token = generateVerificationToken(user);
        String verificationLink = "https://example.com/auth/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), verificationLink, token);

        return savedUser;
    }

    /**
     * Generate an email verification token.
     *
     * @param user The user.
     * @return The encoded token.
     */
    private String generateVerificationToken(User user) {
        return jwtUtil.generateJwtToken(user.getUsername());
    }

    /**
     * Verify user's email using a token.
     *
     * @param token The verification token.
     * @return Success message.
     */
    public String verifyUser(String token) {
        String username = jwtUtil.getUserNameFromJwtToken(token);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isVerified()) {
                return "Email Already Verified!";
            }
            if (jwtUtil.validateJwtToken(token)) {
                user.setVerified(true);
                userRepository.save(user);
                return "Email verified successfully!";
            }
        }
        return "Invalid or expired token";
    }

    /**
     * Authenticate a user using either email or username.
     *
     * @param identifier Username or email.
     * @param password   The user's password.
     * @return An optional user.
     */
    public Optional<User> login(String identifier, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isVerified()) {
                throw new RuntimeException("User is not verified. Please check your email.");
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
}
