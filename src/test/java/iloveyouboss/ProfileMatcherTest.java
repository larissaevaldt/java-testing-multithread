package iloveyouboss;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProfileMatcherTest {
    private BooleanQuestion question;
    private Criteria criteria;
    private ProfileMatcher matcher;
    private Profile matchingProfile;
    private Profile nonMatchingProfile;

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
