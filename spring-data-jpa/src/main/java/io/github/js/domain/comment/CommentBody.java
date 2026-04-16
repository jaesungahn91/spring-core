package io.github.js.domain.comment;

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
public class CommentBody {

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String value;

    private CommentBody(String value) {
        this.value = value;
    }

    public static CommentBody of(String value) {
        return new CommentBody(value);
    }
}
