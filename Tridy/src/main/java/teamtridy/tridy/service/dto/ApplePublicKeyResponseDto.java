package teamtridy.tridy.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

// https://hwannny.tistory.com/71
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplePublicKeyResponseDto {

    private List<Key> keys;

    public Optional<Key> getMatchedKeyBy(String kid, String alg) {
        return this.keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findFirst();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {

        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }
}