package com.igu.webrtc.conference.utils;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class TokenUtils {
	 
    /**
     * 签名秘钥
     */
    public static final String SECRET = "igu2018";
 
    /**
     * 生成token
     *
     * @param id 一般传入userName
     * @return
     */
    public static String createJwtToken(String id) {
        String issuer = "www.xxxx.com";
        String subject = "xxxx@163.com";
        //超时时间，30天
        long ttlMillis = 2678400;
        return createJwtToken(id, issuer, subject, ttlMillis);
    }
 
    /**
     * 生成Token
     *
     * @param id        编号
     * @param issuer    该JWT的签发者，是否使用是可选的
     * @param subject   该JWT所面向的用户，是否使用是可选的；
     * @param ttlMillis 签发时间 （有效时间，过期会报错）
     * @return token String
     */
    public static String createJwtToken(String id, String issuer, String subject, long ttlMillis) {
 
        // 签名算法 ，将对token进行签名
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
 
        // 生成签发时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
 
        // 通过秘钥签名JWT
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
 
        // Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);
 
        // if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
 
        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
 
    }
 
    // Sample method to validate and read the JWT
    public static Claims parseJWT(String jwt) {
        // This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
                .parseClaimsJws(jwt).getBody();
        return claims;
    }
 
    public static void main(String[] args) {
    	
    	String accessToken=TokenUtils.createJwtToken("11111");
        System.out.println(accessToken);
        
        accessToken="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiLkuK3mlocxIiwiaWF0IjoxNTQ1NDcyNzM5LCJzdWIiOiJ4eHh4QDEyNi5jb20iLCJpc3MiOiJ3d3cueHh4eC5jb20iLCJleHAiOjE1NDU0NzYzMzl9.EfRTjscsRc8JP9NdsOVUz6O2Fi3Rj-AEKBlYZTs-4tg";
        
        Claims claims = null;
        try{
             claims = TokenUtils.parseJWT(accessToken);
        }catch (ExpiredJwtException e){
        	System.out.println("token失效，请重新登录");
        }catch (SignatureException se){
        	System.out.println("token令牌错误");
        }

        if(claims!=null) {
        	String userName = claims.getId();
        	System.out.println(userName);
        }
 
        
        
    }
}

