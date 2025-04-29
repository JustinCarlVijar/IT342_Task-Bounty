package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Objects;
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

    private static final int INITIAL_COOLDOWN_RESEND = 32; // seconds
    private static final int INITIAL_COOLDOWN_CHANGE_EMAIL = 2; // seconds


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
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        int verificationCode = generateVerificationCode();
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        user.setVerified(false); // Set default to unverified
        user.setVerificationCode(verificationCode);

        User savedUser = userRepository.save(user);

        // Generate and send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return savedUser;
    }

    /**
     * Verify user's email using a code.
     *
     * @param username The user's username
     * @param code The verification token.
     * @return Success message.
     */
    public ResponseEntity<?> verifyUser(String username, Long code) {
        User user = userRepository.findByUsername(username);
        if (user.isVerified()) {
            return new ResponseEntity<>(HttpStatus.IM_USED);
        }
        if (Objects.equals(user.getVerificationCode(), code)) {
            user.setVerified(true);
            user.setResendAttempts(0);
            user.setLastCodeSentTimestamp(0);
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public void resendVerificationCode(String username) {
        User user = userRepository.findByUsername(username);

        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastSent = (currentTimestamp - user.getLastCodeSentTimestamp()) / 1000; // in seconds

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified");
        }

        // Calculate the cooldown time based on the number of resend attempts
        int cooldown = (int) Math.pow(2, user.getResendAttempts() - 1) * INITIAL_COOLDOWN_RESEND;

        if (timeSinceLastSent < cooldown) {
            throw new RuntimeException("Please wait for the " + cooldown + "s cooldown to expire before resending the verification code.");
        }

        // Resend the verification code
        int verificationCode = generateVerificationCode();  // Generate a new code (implement this method)
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        // Update user record with the new resend attempt info
        user.setVerificationCode(verificationCode);
        user.setLastCodeSentTimestamp(currentTimestamp);
        user.setResendAttempts(user.getResendAttempts() + 1);

        userRepository.save(user);

    }

    public boolean changeEmail(String username, String newEmail) {
        User user = userRepository.findByUsername(username);

        // Same cooldown logic as resend
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastSent = (currentTimestamp - user.getLastCodeSentTimestamp()) / 1000;

        int cooldown = (int) Math.pow(2, user.getResendAttempts()) * INITIAL_COOLDOWN_CHANGE_EMAIL;

        if (timeSinceLastSent < cooldown) {
            throw new RuntimeException("Please wait for the" + cooldown + "s cooldown.");
        }

        // Send a new verification code for email change
        int verificationCode = generateVerificationCode();  // Generate a new code (implement this method)
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        // Update user record
        user.setVerificationCode(verificationCode);
        user.setEmail(newEmail);
        user.setLastCodeSentTimestamp(currentTimestamp);
        user.setResendAttempts(user.getResendAttempts() + 1);
        user.setVerified(false);

        userRepository.save(user);

        return true;
    }

    private int generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 10000000 + random.nextInt(90000000);
        return code;
    }

}
