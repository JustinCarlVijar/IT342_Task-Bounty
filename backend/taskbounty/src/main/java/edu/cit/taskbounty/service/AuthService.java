package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Registration logic
    public User registerUser(String username, String email, String rawPassword, String country) throws Exception {
        // Check if email or username already exists
        if(userRepository.findByEmail(email).isPresent()){
            throw new Exception("Email already exists");
        }
        if(userRepository.findByUsername(username).isPresent()){
            throw new Exception("Username already exists");
        }
        // Validate password length (minimum 8 characters)
        if(rawPassword.length() < 8){
            throw new Exception("Password must be at least 8 characters");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Create new user and save to database
        User user = new User(username, email, hashedPassword, country, new Date());
        return userRepository.save(user);
    }

    // Login logic
    public User authenticateUser(String email, String rawPassword) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty()){
            throw new Exception("Invalid email or password");
        }

        User user = userOpt.get();
        // Check the password using BCrypt
        if(!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new Exception("Invalid email or password");
        }
        return user;
    }

    // Generate a JWT token (this is a placeholder implementation)
    public String generateJwtToken(User user) {
        // For a real implementation, use a JWT library to generate tokens with claims
        // This is just a dummy token for demonstration purposes
        return "dummy-jwt-token-for-user-" + user.getId();
    }
}
