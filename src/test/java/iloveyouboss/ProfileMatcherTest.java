package iloveyouboss;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class ProfileMatcherTest {
    private BooleanQuestion question;
    private Criteria criteria;
    private ProfileMatcher matcher;
    private Profile matchingProfile;
    private Profile nonMatchingProfile;
    private MatchListener listener;

    @Before
    public void create() {
        question = new BooleanQuestion(1, "");
        criteria = new Criteria();
        criteria.add(new Criterion(matchingAnswer(), Weight.MustMatch));
        matchingProfile = createMatchingProfile("matching");
        nonMatchingProfile = createNonMatchingProfile("nonMatching");
    }
    @Before
    public void createMatcher() {
        matcher = new ProfileMatcher();
    }
    @Before
    public void createMatchListener() {
        // Use Mockito’s static mock() method to create a MatchListener mock instance.
        // Verify expectations using this instance.
        listener = mock(MatchListener.class);
    }

    @Test
    public void collectMatchSets() {
        matcher.add(matchingProfile);
        matcher.add(nonMatchingProfile);

        List<MatchSet> sets = matcher.collectMatchSets(criteria);

        assertThat(sets.stream()
                .map(set->set.getProfileId()).collect(Collectors.toSet()),
        equalTo(new HashSet<>
        (Arrays.asList(matchingProfile.getId(), nonMatchingProfile.getId()))));
    }

    @Test
    public void processNotifiesListenerOnMatch() {
        //Add a matching profile (a profile that is expected to match the given criteria) to the matcher.
        matcher.add(matchingProfile);
        //Ask for the MatchSet object for the matching profile given a set of criteria.
        MatchSet set = matchingProfile.getMatchSet(criteria);
        // Ask the matcher to run the match processing,
        // passing in the mock listener and the match set.
        matcher.process(listener, set);

        //Ask Mockito to verify that the foundMatch method was called on the mock listener
        // instance with the matching profile and match set as arguments.
        verify(listener).foundMatch(matchingProfile, set);
    }

    @Test
    public void processDoesNotNotifyListenerWhenNoMatch() {
        matcher.add(nonMatchingProfile);
        MatchSet set = nonMatchingProfile.getMatchSet(criteria);

        matcher.process(listener, set);

        verify(listener, never()).foundMatch(nonMatchingProfile,set);
    }

    @Test
    public void gathersMatchingProfiles() {
        // 1) Create a set of strings to store profile IDs from MatchSet objects that the listener receives.
        Set<String> processedSets = Collections.synchronizedSet(new HashSet<>());

        // 2) Define processFunction(), which will supplant the production version of process.
        BiConsumer<MatchListener, MatchSet> processFunction = (listener, set) -> {
            // 3) For each callback to the listener, add the MatchSet’s profile ID to processedSets.
            processedSets.add(set.getProfileId());
        };

        // 4) Using a helper method, create a pile of MatchSet objects for testing.
        List<MatchSet> matchSets = createMatchSets(100);

        // 5) Call the version of findMatchingProfiles that takes a function as an argument,
        // and pass it the processFunction() implementation.
        matcher.findMatchingProfiles(
                criteria, listener, matchSets, processFunction
        );

        // 6) Grab the ExecutorService from the matcher, and loop until
        // it indicates that all of its threads have completed execution.
        while(!matcher.getExecutor().isTerminated());

        // 7) Verify that the collection of processedSets (representing profile IDs captured in the listener)
        // matches the profile IDs from all of the MatchSet objects created in the test.
        assertThat(processedSets,
                equalTo(matchSets.stream().map(MatchSet::getProfileId).collect(Collectors.toSet())));

    }

    // Utility functions to create the profiles and answers
    private Profile createMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(matchingAnswer());
        return profile;
    }
    private Profile createNonMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(nonMatchingAnswer());
        return profile;
    }
    private Answer matchingAnswer() {
        return new Answer(question, Bool.TRUE);
    }
    private Answer nonMatchingAnswer() {
        return new Answer(question, Bool.FALSE);
    }
    private List<MatchSet> createMatchSets(int count) {
        List<MatchSet> sets = new ArrayList<>();
        for (int i = 0; i < count; i++)
            sets.add(new MatchSet(String.valueOf(i), null, null));
        return sets;
    }
}
