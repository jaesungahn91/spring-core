package io.github.js.application.follow;

import io.github.js.domain.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FollowRestController {

    private final FollowService followService;

    @PostMapping("/{followeeId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable Long followeeId,
            @RequestParam Long followerId) {
        followService.follow(followerId, followeeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{followeeId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long followeeId,
            @RequestParam Long followerId) {
        followService.unfollow(followerId, followeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(
                followService.getFollowers(userId).stream().map(FollowResponse::from).toList());
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<List<FollowResponse>> getFollowings(@PathVariable Long userId) {
        return ResponseEntity.ok(
                followService.getFollowings(userId).stream().map(FollowResponse::from).toList());
    }
}
