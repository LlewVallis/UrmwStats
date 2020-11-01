package org.astropeci.urmwstats.data;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PollRepository {

    private final MongoDatabase db;

    public static class AlreadyExistsException extends RuntimeException { }
    public static class PollNotFoundException extends RuntimeException { }
    public static class InsufficientOptionsException extends RuntimeException { }
    public static class MalformedVoteException extends RuntimeException { }

    @Synchronized
    public List<Poll> getPollsAlphabetically() {
        MongoCollection<Poll> collection = db.getCollection("polls", Poll.class);
        return collection.find().sort(Sorts.ascending("name")).into(new ArrayList<>());
    }

    @Synchronized
    public Poll create(String name, List<String> options) {
        if (options.size() < 2) {
            throw new InsufficientOptionsException();
        }

        MongoCollection<Poll> collection = db.getCollection("polls", Poll.class);

        Poll poll = new Poll(name, options);
        try {
            collection.insertOne(poll);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new AlreadyExistsException();
            }

            throw e;
        }

        return poll;
    }

    @Synchronized
    public Poll close(String name) {
        MongoCollection<Poll> collection = db.getCollection("polls", Poll.class);

        Poll poll = collection.findOneAndDelete(Filters.eq("name", name));
        if (poll == null) {
            throw new PollNotFoundException();
        }

        return poll;
    }

    @Synchronized
    public Poll vote(String pollName, String voterId, String voterName, List<Integer> preferences) {
        MongoCollection<Poll> collection = db.getCollection("polls", Poll.class);

        Poll poll = collection.find(Filters.eq("name", pollName)).first();
        if (poll == null) {
            throw new PollNotFoundException();
        }

        validatePreferences(poll.getOptions(), preferences);

        Voter voter = new Voter(voterId, voterName, preferences);

        Set<Voter> voters = poll.getVoters();
        voters.remove(voter);
        voters.add(voter);

        boolean updated = collection.replaceOne(Filters.eq("_id", poll.getId()), poll).getMatchedCount() > 0;
        if (!updated) {
            throw new PollNotFoundException();
        }

        return poll;
    }

    private void validatePreferences(List<String> options, List<Integer> preferences) {
        if (preferences.size() != options.size()) {
            throw new MalformedVoteException();
        }

        Set<Integer> preferenceSet = new HashSet<>();
        for (Integer preference : preferences) {
            if (preference == null) {
                throw new MalformedVoteException();
            }

            if (!preferenceSet.add(preference)) {
                throw new MalformedVoteException();
            }

            if (preference >= options.size()) {
                throw new MalformedVoteException();
            }
        }
    }

    @Synchronized
    public Poll withdraw(String pollName, String voterId) {
        MongoCollection<Poll> collection = db.getCollection("polls", Poll.class);

        Poll poll = collection.find(Filters.eq("name", pollName)).first();
        if (poll == null) {
            throw new PollNotFoundException();
        }

        Set<Voter> voters = poll.getVoters();
        voters.removeIf(voter -> voter.getId().equals(voterId));

        boolean updated = collection.replaceOne(Filters.eq("_id", poll.getId()), poll).getMatchedCount() > 0;
        if (!updated) {
            throw new PollNotFoundException();
        }

        return poll;
    }
}
