package com.projectshopbando.shopbandoapi.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.projectshopbando.shopbandoapi.dtos.request.AuthenticateReq;
import com.projectshopbando.shopbandoapi.dtos.request.LogoutReq;
import com.projectshopbando.shopbandoapi.dtos.request.RefreshTokenReq;
import com.projectshopbando.shopbandoapi.dtos.response.AuthenticateRes;
import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist_token:";

    @Value("${jwt.signerKey}")
    private String SignerKey;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    public AuthenticateRes authenticate(AuthenticateReq request){
        Account account = accountService.getAccountByEmail(request.getEmail());
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

    public void logout(LogoutReq request) throws ParseException, JOSEException {
        // Verify token first
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expirationMillis = signedJWT.getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();
        // Blacklist the token
        setBlackList(jit, expirationMillis);
    }

    public AuthenticateRes refreshToken(RefreshTokenReq request) throws ParseException, JOSEException {
        // Verify token first
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        // Check if token is blacklisted
        if(redisTemplate.hasKey(BLACKLIST_PREFIX + jit)) {
            throw new UnauthorizedException("Token is blacklisted");
        }
        var expirationMillis = signedJWT.getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();
        // Blacklist the old token
        setBlackList(jit, expirationMillis);

        String subjectId = signedJWT.getJWTClaimsSet().getSubject();
        Account account;
        if(signedJWT.getJWTClaimsSet().getClaim("userType").equals("CUSTOMER")) {
            account = accountService.getAccountByCustomerId(subjectId);
        } else if(signedJWT.getJWTClaimsSet().getClaim("userType").equals("STAFF")) {
            account = accountService.getAccountByStaffId(subjectId);
        } else {
            throw new UnauthorizedException("Invalid user type");
        }

        //Generate new token
        var token = generateToken(account);

        return AuthenticateRes.builder().isAuthenticated(true).token(token).build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SignerKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh) ? new Date(signedJWT
                .getJWTClaimsSet().getIssueTime().toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.MINUTES)
                .toEpochMilli())
            : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if(!verified || new Date().after(expiryTime)) {
            throw new UnauthorizedException("Token unvalid or expired");
        }
        if(redisTemplate.hasKey(BLACKLIST_PREFIX + signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new UnauthorizedException("Token is blacklisted");
        }
        return  signedJWT;
    }

    public boolean introspect (String token){
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

    private void setBlackList(String jit, long expirationMillis) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jit, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }
    // Helper method for generating JWT token
    private String generateToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String subjectId;
        String userType;
        if(account.getCustomer() != null) {
            subjectId = account.getCustomer().getId();
            userType = "CUSTOMER";
        } else if(account.getStaff() != null) {
            subjectId = account.getStaff().getId();
            userType = "STAFF";
        } else {
            throw new UnauthorizedException("Account has no associated user");
        }

        // Build JWT Claims Set
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subjectId)
                .issuer("ShopBanDoAaKk.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account.getRole()))
                .claim("userType", userType)
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
