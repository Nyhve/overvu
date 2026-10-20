package com.overvu.backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.overvu.backend.security.JwtService;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserPreferencesRepository preferencesRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UserPreferencesRepository preferencesRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.preferencesRepository = preferencesRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token);
    }

    // public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    //     this.userRepository = userRepository;
    //     this.passwordEncoder = passwordEncoder;
    // }

    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), hashedPassword);
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public UserPreferencesResponse getPreferences() {
        Long userId = getCurrentUserId();
        UserPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return preferencesRepository.save(new UserPreferences(user));
                });
        return UserPreferencesResponse.fromEntity(prefs);
    }

    public UserPreferencesResponse updatePreferences(UpdateUserPreferencesRequest request) {
        Long userId = getCurrentUserId();
        UserPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return new UserPreferences(user);
                });
        prefs.setGoalsEnabled(request.goalsEnabled());
        prefs.setTasksEnabled(request.tasksEnabled());
        prefs.setHabitsEnabled(request.habitsEnabled());
        prefs.setFitnessEnabled(request.fitnessEnabled());
        prefs.setLearningEnabled(request.learningEnabled());
        prefs.setFinanceEnabled(request.financeEnabled());
        return UserPreferencesResponse.fromEntity(preferencesRepository.save(prefs));
    }

    private Long getCurrentUserId() {
        return (Long) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}