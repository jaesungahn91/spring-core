package io.github.js.domain.follow;

import io.github.js.domain.user.User;
import io.github.js.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return;
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + followerId));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + followeeId));

        followRepository.save(Follow.of(follower, followee));
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowings(Long userId) {
        return followRepository.findByFollowerId(userId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFolloweeId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }
}
