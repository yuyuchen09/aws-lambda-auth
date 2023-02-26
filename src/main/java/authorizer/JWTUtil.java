package authorizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

public class JWTUtil {

    static ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();

    static {

        try {

            JWKSource keySource = null;
            keySource = new RemoteJWKSet(
                    new URL("https://cognito-idp." + System.getenv("AWS_REGION") + ".amazonaws.com/" + System.getenv("USER_POOL_ID") + "/.well-known/jwks.json"));
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public static String getSub(String token) {

        SecurityContext securityContext = null;
        JWTClaimsSet claimsSet;
        try {
            claimsSet = jwtProcessor.process(token, securityContext);
            return claimsSet.getStringClaim("sub");
        } catch (JOSEException | ParseException joseException) {
            joseException.printStackTrace();
        } catch (BadJOSEException badJOSEException) {
            badJOSEException.printStackTrace();
        }

        return null;
    }

}