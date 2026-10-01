package io.a_caminho.backend.security.oauth2;

import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("Deve vincular google_id ao usuário existente que possuía o mesmo e-mail")
        void shouldLinkGoogleAccountToExistingUser() {
            String email = "existing@ufpb.br";
            String googleId = "google-sub-123456";

            User existingUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email(email)
                    .password("some-password-hash")
                    .role(UserRole.STUDENT)
                    .googleId(null)
                    .build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OAuth2User mockOAuth2User = mock(OAuth2User.class);
            when(mockOAuth2User.getAttribute("email")).thenReturn(email);
            when(mockOAuth2User.getAttribute("sub")).thenReturn(googleId);
            when(mockOAuth2User.getAttributes()).thenReturn(Map.of("email", email, "sub", googleId));

            OAuth2User result = customOAuth2UserService.processOAuth2User(mockOAuth2User);

            assertNotNull(result);
            assertTrue(result instanceof UserPrincipal);
            UserPrincipal principal = (UserPrincipal) result;
            assertEquals(email, principal.getUsername());
            verify(userRepository).save(argThat(u -> googleId.equals(u.getGoogleId())));
        }

        @Test
        @DisplayName("Deve criar novo usuário caso não exista registro com o e-mail retornado pelo Google")
        void shouldCreateNewUserWhenGoogleEmailDoesNotExist() {
            String email = "novousuario@gmail.com";
            String googleId = "google-sub-999999";

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed-random-password");

            User newUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email(email)
                    .password("hashed-random-password")
                    .googleId(googleId)
                    .role(UserRole.STUDENT)
                    .build();
            when(userRepository.save(any(User.class))).thenReturn(newUser);

            OAuth2User mockOAuth2User = mock(OAuth2User.class);
            when(mockOAuth2User.getAttribute("email")).thenReturn(email);
            when(mockOAuth2User.getAttribute("sub")).thenReturn(googleId);
            when(mockOAuth2User.getAttributes()).thenReturn(Map.of("email", email, "sub", googleId));

            OAuth2User result = customOAuth2UserService.processOAuth2User(mockOAuth2User);

            assertNotNull(result);
            verify(passwordEncoder).encode(any(CharSequence.class));
            verify(userRepository).save(argThat(u -> email.equals(u.getEmail())
                    && googleId.equals(u.getGoogleId())
                    && u.getRole() == UserRole.STUDENT
                    && "hashed-random-password".equals(u.getPassword())));
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("Deve lançar OAuth2AuthenticationException se o e-mail não for retornado pelo Google")
        void shouldThrowExceptionWhenEmailNotProvided() {
            OAuth2User mockOAuth2User = mock(OAuth2User.class);
            when(mockOAuth2User.getAttribute("email")).thenReturn(null);

            assertThrows(OAuth2AuthenticationException.class, () -> customOAuth2UserService.processOAuth2User(mockOAuth2User));
        }
    }
}
