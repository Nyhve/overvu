package com.overvu.backend.user;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.overvu.backend.security.JwtService;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_savesUserWithHashedPassword() {
        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "sicheresPasswort123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("sicheresPasswort123")).thenReturn("hashedValue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        verify(passwordEncoder).encode("sicheresPasswort123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "sicheresPasswort123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        User user = new User("test@example.com", "hashedValue");
        LoginRequest request = new LoginRequest("test@example.com", "sicheresPasswort123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("sicheresPasswort123", "hashedValue")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("test@example.com"))).thenReturn("fake-jwt-token");

        LoginResponse response = userService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
    }

    @Test
    void login_throwsException_whenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("unbekannt@example.com", "irgendeinPasswort");
        when(userRepository.findByEmail("unbekannt@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_throwsException_whenPasswordIsWrong() {
        User user = new User("test@example.com", "hashedValue");
        LoginRequest request = new LoginRequest("test@example.com", "falschesPasswort");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("falschesPasswort", "hashedValue")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any());
    }
}