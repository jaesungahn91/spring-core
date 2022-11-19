package io.github.js.domain.jwt;

public interface JWTDeserializer {

    JWTPayload jwtPayloadFromJWT(String jwtToken);

}
