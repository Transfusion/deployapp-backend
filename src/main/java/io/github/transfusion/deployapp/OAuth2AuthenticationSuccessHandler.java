package io.github.transfusion.deployapp;

import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.services.MigrationService;
import io.github.transfusion.deployapp.session.SessionData;
import io.github.transfusion.deployapp.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

import static io.github.transfusion.deployapp.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

/**
 * <a href="https://stackoverflow.com/questions/30697202/setting-session-scoped-objects-in-authenticationsuccesshandler">See here</a>
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


//    private TokenProvider tokenProvider;

//    private AppProperties appProperties;

    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    @Autowired
    OAuth2AuthenticationSuccessHandler(/*TokenProvider tokenProvider, AppProperties appProperties,*/
            HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
//        this.tokenProvider = tokenProvider;
//        this.appProperties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private SessionData sessionData;

    @Autowired
    private HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println(authentication.getDetails());

        // clone the session temporarily
        Enumeration<String> keys = request.getSession().getAttributeNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            httpSession.setAttribute(key, request.getSession().getValue(key));
        }

        UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
        migrationService.migrateAnonymousData(userId);
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }


    private void addWelcomeCookie(String user, HttpServletResponse response) {
        Cookie welcomeCookie = getWelcomeCookie(user);
        response.addCookie(welcomeCookie);
    }

    private Cookie getWelcomeCookie(String user) {
        Cookie welcomeCookie = new Cookie("welcome", user);
        welcomeCookie.setMaxAge(60 * 60 * 24 * 30);
        return welcomeCookie;
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

//        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
//            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
//        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

//        String token = tokenProvider.createToken(authentication);
//        String token = "";
        return UriComponentsBuilder.fromUriString(targetUrl)
//                .queryParam("token", token)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

}
