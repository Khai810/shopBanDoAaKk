package com.projectshopbando.shopbandoapi.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.projectshopbando.shopbandoapi.dtos.request.AuthenticateReq;
import com.projectshopbando.shopbandoapi.dtos.response.AuthenticateRes;
import com.projectshopbando.shopbandoapi.entities.User;
import com.projectshopbando.shopbandoapi.enums.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.signerKey}")
    private String SignerKey;

    public AuthenticateRes authenticate(AuthenticateReq request){
        User user = userService.getUserByUsername(request.getUsername());
        boolean result = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!result){
            return AuthenticateRes.builder()
                    .isAuthenticated(false)
                    .build();
        }
        return AuthenticateRes.builder()
                .isAuthenticated(true)
                .token(generateToken(user))
                .build();

    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("ShopBanDo.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("scope", buildScope(user.getRole()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try{
            jwsObject.sign(new MACSigner(SignerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(Set<Roles> roles) {
        StringBuilder sb = new StringBuilder();
        for(Roles role : roles) {
            sb.append("ROLE_").append(role).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
