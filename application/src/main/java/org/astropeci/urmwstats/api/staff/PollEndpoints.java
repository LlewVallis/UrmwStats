package org.astropeci.urmwstats.api.staff;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.astropeci.urmwstats.Announcer;
import org.astropeci.urmwstats.auth.RoleManager;
import org.astropeci.urmwstats.data.Poll;
import org.astropeci.urmwstats.data.PollRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff")
@Secured("ROLE_USER")
public class PollEndpoints {

    private final RoleManager roleManager;
    private final PollRepository pollRepository;

    private final Announcer announcer;

    @GetMapping("/polls")
    public List<Poll> polls(@AuthenticationPrincipal OAuth2User principal) {
        roleManager.authenticate(principal);
        return pollRepository.getPollsAlphabetically();
    }

    @Data
    private static class NewPollData {
        private List<String> options;
    }

    @PostMapping("/poll/{name}")
    public ResponseEntity<Poll> createPoll(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String name,
            @RequestBody NewPollData data
    ) {
        roleManager.authenticate(principal);
        log.info("{} is creating poll {}", principal, name);

        try {
            Poll poll = pollRepository.create(name, data.options);

            String username = Objects.requireNonNull(principal.getAttribute("username"));
            announcer.announce(username + " created the poll `" + name + "`");

            return new ResponseEntity<>(poll, HttpStatus.CREATED);
        } catch (PollRepository.AlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } catch (PollRepository.InsufficientOptionsException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @DeleteMapping("/poll/{name}")
    public Poll closePoll(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String name
    ) {
        roleManager.authenticate(principal);
        log.info("{} is closing poll {}", principal, name);

        try {
            Poll poll = pollRepository.close(name);

            String username = Objects.requireNonNull(principal.getAttribute("username"));
            String winningOptions = String.join(", ", poll.getWinningOptions());
            announcer.announce(username + " closed `" + name + "` the winning option(s) were `" + winningOptions + "`");

            return poll;
        } catch (PollRepository.PollNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Data
    private static class VoteData {
        private List<Integer> preferences;
    }

    @PostMapping("/poll/{pollName}/vote")
    public Poll vote(
        @AuthenticationPrincipal OAuth2User principal,
        @PathVariable String pollName,
        @RequestBody VoteData voteData
    ) {
        roleManager.authenticate(principal);
        log.info(
                "{} is casting vote [{}] on poll {}",
                principal,
                voteData.preferences.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(", ")),
                pollName
        );

        String voterId = Objects.requireNonNull(principal.getAttribute("id"));
        String voterName = Objects.requireNonNull(principal.getAttribute("username"));

        try {
            Poll poll = pollRepository.vote(pollName, voterId, voterName, voteData.preferences);

            announcer.announce(voterName + " voted on `" + pollName + "`");

            return poll;
        } catch (PollRepository.PollNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (PollRepository.MalformedVoteException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @DeleteMapping("/poll/{pollName}/vote")
    public Poll withdraw(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String pollName
    ) {
        roleManager.authenticate(principal);
        log.info("{} is withdrawing their vote on poll {}", principal, pollName);

        String voterId = Objects.requireNonNull(principal.getAttribute("id"));
        String voterName = Objects.requireNonNull(principal.getAttribute("username"));

        try {
            Poll poll = pollRepository.withdraw(pollName, voterId);

            announcer.announce(voterName + " withdrew their vote on `" + pollName + "`");

            return poll;
        } catch (PollRepository.PollNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
