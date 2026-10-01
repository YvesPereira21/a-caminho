package io.a_caminho.backend.security.oauth2;

import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final CookieUtils cookieUtils;

    @Value("${app.oauth2.frontend-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado após login OAuth2"));

        String accessToken = tokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Adiciona cookies HttpOnly
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(refreshToken.getToken()).toString());

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, frontendRedirectUri);
    }
}
