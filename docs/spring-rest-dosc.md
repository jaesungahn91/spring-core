# Spring REST Docs 
> With REST Assured

- SpringBoot 2.7.5
- Spring REST Docs 2.0.6
- rest-assured 4.5.1
---

- https://docs.spring.io/spring-restdocs/docs/2.0.6.RELEASE/reference/html5/
- https://spring.io/projects/spring-restdocs#learn
- https://docs.asciidoctor.org/
---

### Troubleshooting
#### 버전 관련 문제
```text
Some problems were found with the configuration of task ':asciidoctor' (type 'AsciidoctorTask').
  - In plugin 'org.asciidoctor.convert' type 'org.asciidoctor.gradle.AsciidoctorTask' method 'asGemPath()' should not be annotated with: @Optional, @InputDirectory.
```
```text
> (LoadError) no such file to load -- asciidoctor/logging
```
- gradle version 7.5.1 -> 6.9.3 변경 했다가 현재 사용하고 있는 spring-restdocs 버전에 맞춰 `id "org.asciidoctor.jvm.convert" version "3.3.2"`로 plugin 변경
- gradle version 7.5.1 + org.asciidoctor.jvm.convert version 3.3.2

#### Asciidoc 문서 경로 설정 문제
- https://docs.asciidoctor.org/asciidoc/latest/attributes/document-attributes-ref/#intrinsic-attributes
---

