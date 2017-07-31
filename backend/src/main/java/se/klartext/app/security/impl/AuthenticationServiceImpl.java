package se.klartext.app.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import se.klartext.app.data.api.AuthTokenRepository;
import se.klartext.app.model.AuthToken;
import se.klartext.app.model.User;
import se.klartext.app.security.api.AuthenticationService;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService{

    private final AuthenticationManager authenticationManager;
    private final AuthTokenRepository authTokenRepo;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,AuthTokenRepository authTokenRepo) {
        this.authenticationManager = authenticationManager;
        this.authTokenRepo = authTokenRepo;
    }

    @Override
    public Optional<AuthToken> authenticate(String username, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username,password);

        try{
            authentication = authenticationManager.authenticate(authentication);
        }catch (AuthenticationException e){
            return Optional.empty();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if(authentication.getPrincipal() != null){

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<String> token  = TokenGenerator.getInstance().generateToken(userDetails);

            if(token.isPresent()){
                AuthToken authToken = authTokenRepo.findByUserId(userDetails.getUser().getId()).orElse(
                        AuthToken.builder()
                                .user(userDetails.getUser())
                                .expiresAt(LocalDateTime.now().plusMonths(1))
                                .build()
                );
                authToken.setToken(token.get());
                return Optional.of(authTokenRepo.save(authToken));
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isValidToken(String token) {
        return false;
    }
}
