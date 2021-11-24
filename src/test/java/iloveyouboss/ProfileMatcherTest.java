package iloveyouboss;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
}
