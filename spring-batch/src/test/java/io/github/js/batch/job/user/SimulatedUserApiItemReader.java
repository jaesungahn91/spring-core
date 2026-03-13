package io.github.js.batch.job.user;

import java.util.ArrayList;
import java.util.List;

class SimulatedUserApiItemReader extends UserApiItemReader {

    private static final int SIMULATED_TOTAL = 25;

    @Override
    protected List<UserImportDto> fetchPage(int page) {
        int from = page * PAGE_SIZE;
        if (from >= SIMULATED_TOTAL) {
            return List.of();
        }
        int to = Math.min(from + PAGE_SIZE, SIMULATED_TOTAL);
        List<UserImportDto> items = new ArrayList<>();
        for (int i = from; i < to; i++) {
            UserImportDto dto = new UserImportDto();
            dto.setEmail("user" + i + "@example.com");
            dto.setName("User " + i);
            dto.setAge(20 + (i % 30));
            items.add(dto);
        }
        return items;
    }

}