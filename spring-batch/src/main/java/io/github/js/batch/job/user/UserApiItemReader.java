package io.github.js.batch.job.user;

import org.springframework.batch.item.ItemReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 외부 API 또는 페이징 데이터 소스를 청크 단위로 읽는 커스텀 ItemReader.
 *
 * 핵심 계약:
 * - read() 가 null 을 반환하면 Spring Batch는 해당 청크를 종료한다.
 * - 상태(currentPage, buffer)를 가지므로 반드시 @StepScope 로 등록해야 한다.
 *
 * 구현 방법:
 * - fetchPage() 를 HTTP 클라이언트, DB 페이징 쿼리 등으로 오버라이드한다.
 */
public class UserApiItemReader implements ItemReader<UserImportDto> {

    protected static final int PAGE_SIZE = 10;

    private int currentPage = 0;
    private final Queue<UserImportDto> buffer = new LinkedList<>();

    @Override
    public UserImportDto read() {
        if (buffer.isEmpty()) {
            List<UserImportDto> page = fetchPage(currentPage++);
            if (page.isEmpty()) {
                return null;
            }
            buffer.addAll(page);
        }
        return buffer.poll();
    }

    protected List<UserImportDto> fetchPage(int page) {
        throw new UnsupportedOperationException("fetchPage()를 실제 API 클라이언트로 구현 필요");
    }

}