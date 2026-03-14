package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.UserRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User ID 범위 기반 Partitioner.
 *
 * DB에 저장된 User의 min/max ID를 기준으로 gridSize개 파티션으로 분할.
 * 각 파티션은 stepExecutionContext에 minId, maxId를 담아 Worker Step에 전달.
 */
@Component
public class UserRangePartitioner implements Partitioner {

    private final UserRepository userRepository;

    public UserRangePartitioner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long minId = userRepository.findMinId();
        long maxId = userRepository.findMaxId();

        Map<String, ExecutionContext> result = new HashMap<>();

        if (maxId == 0) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", 0L);
            context.putLong("maxId", 0L);
            result.put("partition0", context);
            return result;
        }

        long rangeSize = Math.max(1, (maxId - minId + 1) / gridSize);
        long start = minId;

        for (int i = 0; i < gridSize; i++) {
            long end = (i == gridSize - 1) ? maxId : start + rangeSize - 1;
            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start);
            context.putLong("maxId", end);
            result.put("partition" + i, context);
            start = end + 1;
            if (start > maxId) break;
        }

        return result;
    }

}
