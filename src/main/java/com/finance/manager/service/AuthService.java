package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.entity.User;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // register a new user
    public MessageResponse register(RegisterRequest request) {
        // check if username is taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        // create the user object and encode password
        User newUser = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .phoneNumber(request.getPhoneNumber())
            .build();

        User savedUser = userRepository.save(newUser);

        return new MessageResponse("User registered successfully", savedUser.getId());
    }

    // login - check credentials and create session
    public MessageResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // find the user
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // create a session and store user id
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", user.getId());

        return new MessageResponse("Login successful");
    }

    // logout - just invalidate the session
    public MessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new MessageResponse("Logout successful");
    }

    // helper to get currently logged in user from session
    // used by other services a lot
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthorizedException("Not logged in");
        }

        Long userId = (Long) session.getAttribute("userId");

        // find user in database
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        return currentUser;
    }
}
