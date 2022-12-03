# Spring REST Docs 

- SpringBoot 2.7.5
- Spring REST Docs 2.0.6
---

```text
Spring REST Docs 3.0.0 has the following minimum requirements:

- Java 17
- Spring Framework 6

Additionally, the spring-restdocs-restassured module requires REST Assured 5.2.
```
- https://docs.spring.io/spring-restdocs/docs/2.0.6.RELEASE/reference/html5/
- https://spring.io/projects/spring-restdocs#learn
- https://docs.asciidoctor.org/

---

### Troubleshooting
```text
Some problems were found with the configuration of task ':asciidoctor' (type 'AsciidoctorTask').
  - In plugin 'org.asciidoctor.convert' type 'org.asciidoctor.gradle.AsciidoctorTask' method 'asGemPath()' should not be annotated with: @Optional, @InputDirectory.
```
```text
> (LoadError) no such file to load -- asciidoctor/logging
```
- gradle version 7.5.1 -> 6.9.3 변경 했다가 현재 사용하고 있는 spring-restdocs 버전에 맞춰 `id "org.asciidoctor.jvm.convert" version "3.3.2"`로 plugin 변경
- gradle version 7.5.1 + org.asciidoctor.jvm.convert version 3.3.2
