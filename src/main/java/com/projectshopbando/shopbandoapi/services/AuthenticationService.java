package com.projectshopbando.shopbandoapi.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.projectshopbando.shopbandoapi.dtos.request.AuthenticateReq;
import com.projectshopbando.shopbandoapi.dtos.response.AuthenticateRes;
import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
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
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    @Value("${jwt.signerKey}")
    private String SignerKey;

    public AuthenticateRes authenticate(AuthenticateReq request){
        Account account = accountService.getAccountByEmail(request.getEmail());
        if(account == null){
            throw  new NotFoundException("User not found");
        }
        boolean result = passwordEncoder.matches(request.getPassword(), account.getPassword());
        if(!result){
            return AuthenticateRes.builder()
                    .isAuthenticated(false)
                    .build();
        }
        return AuthenticateRes.builder()
                .isAuthenticated(true)
                .token(generateToken(account))
                .build();

    }

    // Helper method for generating JWT token
    private String generateToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //Check if account is customer or staff
        String subjectId;
        if (account.getCustomer() != null) {
            subjectId = account.getCustomer().getId();
        } else if (account.getStaff() != null) {
            subjectId = account.getStaff().getId();
        } else {
            throw new RuntimeException("Account must belong to either Customer or Staff");
        }

        // Build JWT Claims Set
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subjectId)
                .issuer("ShopBanDoAaKk.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("scope", buildScope(account.getRole()))
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

    // Helper method to build scope string from roles
    private String buildScope(Set<Roles> roles) {
        StringBuilder sb = new StringBuilder();
        for(Roles role : roles) {
            sb.append("ROLE_").append(role).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
