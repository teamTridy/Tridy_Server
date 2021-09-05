package teamtridy.tridy.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class GoogleService {

    @Value("${social.google.client_id}")
    private String googleClientId;

    public String getSocialId(String idToken) {
        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))  // Specify the CLIENT_ID of the app that accesses the backend:
                    .build();

            GoogleIdToken verifiedIdToken = null; // (Receive idTokenString by HTTPS POST)

            verifiedIdToken = verifier.verify(idToken);

            if (verifiedIdToken != null) {
                Payload payload = verifiedIdToken.getPayload();
                String socialId = payload.getSubject();
                return socialId;
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
