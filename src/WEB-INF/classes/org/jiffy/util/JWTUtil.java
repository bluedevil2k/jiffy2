package org.jiffy.util;

import java.security.Key;
import java.util.Date;

import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JWTUtil
{
	public static Key key = null;
	
	public static void init()
	{
		byte[] encoded = Jiffy.getValue("jwtSecretKey").getBytes();
		key = Keys.hmacShaKeyFor(encoded);
	}
	
	public static String getToken(Context ctx)
	{
		String jwtToken = ctx.header("Authorization");
		return jwtToken.substring(7); // remove "Bearer " from the token
	}
	
	public static String createJWT(String id, String issuer, String subject, long ttlMillis)
	{
		if (key == null)
		{
			JWTUtil.init();
		}
		
		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().
								setId(id).
								setIssuedAt(now).
								setSubject(subject).
								setIssuer(issuer).
								signWith(signatureAlgorithm, key);

		// if it has been specified, let's add the expiration
		if (ttlMillis > 0)
		{
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		// Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	public static Claims decodeJWT(String jwt) throws Exception
	{
		if (key == null)
		{
			JWTUtil.init();
		}
		
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
		return claims;
	}
	
	public static String getSubject(String jwt)
	{
		if (key == null)
		{
			JWTUtil.init();
		}
		
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
		return claims.getSubject();
	}
	
	public static boolean isValidToken(String jwt)
	{
		try
		{
			if (key == null)
			{
				JWTUtil.init();
			}
			
			Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	public static void main(String...a) throws Exception
	{
		Jiffy.configure();
		
		String s = JWTUtil.createJWT("JWT-Token", "AuctionGo", "a@a.com", 10000);
		System.out.println(s);
		String f = JWTUtil.getSubject(s);
		System.out.println(f);
		boolean b = JWTUtil.isValidToken(s);
		System.out.println(b);
		b = JWTUtil.isValidToken(s+"A");
		System.out.println(b);
	}
}
