package io.github.js.domain.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagName {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String value;

    private TagName(String value) {
        this.value = value.toLowerCase().trim();
    }

    public static TagName of(String value) {
        return new TagName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
