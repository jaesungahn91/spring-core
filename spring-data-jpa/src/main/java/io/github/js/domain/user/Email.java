package io.github.js.domain.user;

import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@EqualsAndHashCode(of = "address")
@Embeddable
public class Email {

    @Column(name = "email", length = 50, nullable = false)
    private String address;

    protected Email() {
    }

    public Email(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return address;
    }

}
