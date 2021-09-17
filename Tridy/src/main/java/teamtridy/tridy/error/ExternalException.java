package teamtridy.tridy.error;

import lombok.Getter;

@Getter
public class ExternalException extends RuntimeException {

    private final Exception e;

    public ExternalException(Exception e) {
        super(e.getMessage());
        this.e = e;
    }
}