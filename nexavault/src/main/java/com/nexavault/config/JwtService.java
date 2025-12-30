package com.nexavault.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.nexavault.requestdto.JwtLoginRequestDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Service class for JWT token generation, validation, and extraction.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

	private final SpringUserDetailsService userService;

	// Secret key for signing the JWT token
	@Value("${SECRET_KEY}")
	public String SECRET;

	/**
	 * Extracts the username from a JWT token.
	 *
	 * @param token The JWT token
	 * @return The username extracted from the token
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extracts the expiration date from a JWT token.
	 *
	 * @param token The JWT token
	 * @return The expiration date of the token
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extracts a specific claim from a JWT token.
	 *
	 * @param token          The JWT token
	 * @param claimsResolver The function to resolve the claim
	 * @param <T>            The type of the claim
	 * @return The extracted claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Extracts all claims from a JWT token.
	 *
	 * @param token The JWT token
	 * @return All claims in the token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignKey()).setAllowedClockSkewSeconds(60).build()
				.parseClaimsJws(token).getBody();
	}

	/**
	 * Checks if a JWT token is expired.
	 *
	 * @param token The JWT token
	 * @return true if the token is expired, false otherwise
	 */
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Validates a JWT token.
	 *
	 * @param token       The JWT token
	 * @param userDetails The UserDetails object representing the user
	 * @return true if the token is valid for the user, false otherwise
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	/**
	 * Generates a JWT token for a given user.
	 *
	 * @param jwtRequest The JwtLoginRequestDto object containing the user's login
	 *                   details
	 * @return The generated JWT token
	 */
	public String generateToken(JwtLoginRequestDto jwtRequest) {
		UserDetails userDetails = userService.loadUserByUsername(jwtRequest.email());
		return createToken(userDetails);
	}

	/**
	 * Creates a JWT token for a user.
	 *
	 * @param userDetails The UserDetails object representing the user
	 * @return The generated JWT token
	 */
	private String createToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		SpringUserDetails springUserDetails = (SpringUserDetails) userDetails;
		// Adding custom claims, such as user roles, to the token
		claims.put("role", userDetails.getAuthorities().toString().substring(1,
				userDetails.getAuthorities().toString().length() - 1));
		claims.put("userId", springUserDetails.getUserId());
		return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Gets the signing key for the JWT token.
	 *
	 * @return The signing key
	 */
	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
