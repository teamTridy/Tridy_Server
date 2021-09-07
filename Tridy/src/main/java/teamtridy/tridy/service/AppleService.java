package teamtridy.tridy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import teamtridy.tridy.dto.ApplePublicKeyResponseDto;

// https://whitepaek.tistory.com/61
// https://jdifm.tistory.com/19
// https://hwannny.tistory.com/71
// 사용자 ID 토큰의 유효성과 무결성을 확인: https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/verifying_a_user
/* ID 토큰을 확인하려면 앱 서버가 다음을 수행해야 합니다.
    서버의 공개 키를 사용하여 JWS E256 서명 확인, jwt의 sinature를 증명하라는 것은  공개키를 통해 signature 값을 복호화 하여 header값과 payload 값과 같은지 비교하라는 것입니다.
    nonce인증 확인 -> rest api방식 또는 웹페이지 방식으로 개발하는 경우에만 가능 (redirect url있는 경우)
    iss필드에 다음이 포함되어 있는지 확인하십시오.https://appleid.apple.com
    aud필드가 개발자의 필드인지 확인하십시오.client_id
    시간이 exp토큰 값 보다 빠른지 확인합니다.
 */

@RequiredArgsConstructor
@Service
public class AppleService {

    private final RestTemplate restTemplate;

    @Value("${social.apple.client_id}")
    private String appleClientId; //The identifier (App ID or Services ID) for your app.

    @Value("${social.apple.issuer}")
    private String appleIssuer;

    @Value("${social.apple.nonce}")
    private String appleNonce;

    @Value("${social.apple.url.public_keys}")
    private String appleUrlPublicKeys; //The identifier (App ID or Services ID) for your app.

    public String getSocialId(String idToken) {
        try {
            PublicKey publickey = getPublicKey(idToken); //서버의 공개 키를 사용하여 JWS E256 서명 확인
            Claims claims = Jwts.parserBuilder().setSigningKey(publickey).build()
                .parseClaimsJws(idToken).getBody();

            if (claims.get("iss").toString().equals(appleIssuer)
                //iss필드에 다음이 포함되어 있는지 확인하십시오.https://appleid.apple.com
                && claims.get("aud").toString().equals(
                appleClientId)) { //aud필드가 개발자의 필드인지 확인하십시오. Apple Developer 페이지에 App Bundle ID를 말한다. ex) com.xxx.xxx 형식이다.

                return claims.get("sub")
                    .toString(); //Since this token is meant for your application, the value is the unique identifier for the user.
            }
        } catch (ExpiredJwtException e) { //시간이 exp토큰 값 보다 빠른지 확인합니다.
            throw e;
        }
        return null;
    }


    private PublicKey getPublicKey(String idToken) {
        try {
            HttpHeaders headers = new HttpHeaders();

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
            ResponseEntity<ApplePublicKeyResponseDto> response = restTemplate
                .exchange(appleUrlPublicKeys, HttpMethod.GET, request,
                    ApplePublicKeyResponseDto.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                //get jwt token header
                String headerOfIdentityToken = idToken.substring(0, idToken.indexOf(
                    ".")); //jwt은 우선  header와 payload, signature로 구성되어있습니다. header에는 kid(key ID), alg(알고리즘 유형)으로 구성되어있고 payload는 전달하고자 하는 정보들이 담겨있습니다. signature는 header와 payload를 비밀키와 header에 있는 알고리즘 유형으로 암호화한 정보입니다.
                Map<String, String> header = new ObjectMapper().readValue(
                    new String(Base64.getDecoder().decode(headerOfIdentityToken),
                        StandardCharsets.UTF_8),
                    Map.class);

                //identify token의 kid와 alg가 일치하는 public key 얻기
                ApplePublicKeyResponseDto.Key key = response.getBody()
                    .getMatchedKeyBy(header.get("kid"), header.get("alg"))
                    .orElseThrow(() -> new NullPointerException(
                        "Failed get public key from apple's id server."));

                byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
                byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());
                BigInteger n = new BigInteger(1, nBytes);
                BigInteger e = new BigInteger(1, eBytes);
                RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
                KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

                return publicKey;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
