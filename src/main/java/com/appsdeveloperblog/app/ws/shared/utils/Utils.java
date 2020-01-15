package com.appsdeveloperblog.app.ws.shared.utils;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.appsdeveloperblog.app.ws.security.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class Utils {
	
	private final Random RADNOM = new SecureRandom();
	private final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public String generateUserId(int length) {
		return generateRandomString(length);
	}
	
	public String generateAddressId(int length) {
		return generateRandomString(length);
	}
	
	private String generateRandomString(int length) {
		StringBuilder returnValue = new StringBuilder(length);
		
		for (int i = 0; i < length; i++) {
			returnValue.append(ALPHABET.charAt(RADNOM.nextInt(ALPHABET.length())));
		}
		
		return new String(returnValue);
	}
	
	public String generatePasswordResetToken(String userId)
    {
        return Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
                .compact();
    }

	public boolean hasTokenExpired(String token) {
		boolean returnValue = false;
		
		try {
			Claims claims = Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token)
						.getBody();
			
			Date tokenExpirationDate = claims.getExpiration();
			Date todayDate = new Date();
			
			returnValue = tokenExpirationDate.before(todayDate);
		} catch (Exception e) {
			returnValue = true;
		}
		
		return returnValue;
	}
}
