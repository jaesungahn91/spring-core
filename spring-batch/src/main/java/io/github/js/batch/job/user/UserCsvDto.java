package io.github.js.batch.job.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCsvDto {

    private String email;
    private String name;
    private int age;

}
