package teamtridy.tridy.util;


import static org.springframework.restdocs.snippet.Attributes.key;

import org.springframework.restdocs.snippet.Attributes;

public interface DocumentFormatGenerator {

    static Attributes.Attribute getDateFormat() { // (2)
        return key("format").value("yyyy-MM-dd");
    }

    static Attributes.Attribute getDateTimeFormat() { // (2)
        return key("format").value("dd-MMM-yy hh.mm.ss.SSSSSS");
    }
}