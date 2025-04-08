package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Registration logic remains the same
    public User registerUser(String username, String email, String rawPassword, String country) throws Exception {
        if(userRepository.findByEmail(email).isPresent()){
            throw new Exception("Email already exists");
        }
        if(userRepository.findByUsername(username).isPresent()){
            throw new Exception("Username already exists");
        }
        if(rawPassword.length() < 8){
            throw new Exception("Password must be at least 8 characters");
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(username, email, hashedPassword, country, new Date());
        return userRepository.save(user);
    }

    // Login logic with real JWT generation
    public User authenticateUser(String email, String rawPassword) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty()){
            throw new Exception("Invalid email or password");
        }

        User user = userOpt.get();
        if(!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new Exception("Invalid email or password");
        }
        return user;
    }

    // Generate a JWT token using JwtService
    public String generateJwtToken(User user) {
        return jwtService.generateToken(user);
    }
}
