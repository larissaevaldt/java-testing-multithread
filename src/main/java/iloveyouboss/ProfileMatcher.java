/***
 * Excerpted from "Pragmatic Unit Testing in Java with JUnit",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/utj2 for more book information.
***/
package iloveyouboss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/* A ProfileMatcher collects all of the relevant profiles. Given a set of criteria from a client,
 * the ProfileMatcher instance iterates the profiles and returns those matching the criteria,
 * along with the MatchSet instance (which provides the ability to obtain the score of the match)
 */
public class ProfileMatcher {
   private Map<String, Profile> profiles = new HashMap<>(); 
   private static final int DEFAULT_POOL_SIZE = 4;

   public void add(Profile profile) {
      profiles.put(profile.getId(), profile);
   }

   /**
    * We need the application to be responsive, so we designed the findMatchingProfiles() method to
    * calculate matches in the context of separate threads. Further, rather than block the client
    * until all processing is complete, we instead designed findMatchingProfiles() to take a MatchListener argument.
    * Each matching profile gets returned via the MatchListener method foundMatch().
    * @param criteria
    * @param listener
    */
   public void findMatchingProfiles(Criteria criteria, MatchListener listener) {
      ExecutorService executor = 
            Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

      //For each match set,
      for (MatchSet set: collectMatchSets(criteria)) {
         // create and spawn a thread that
         Runnable runnable = () -> {
            // if a matches request to the MatchSet returns true
            if (set.matches())
               // send the profile and corresponding MatchSet object to the MatchListener
               listener.foundMatch(profiles.get(set.getProfileId()), set);
         };
         executor.execute(runnable);
      }
      executor.shutdown();
   }

   /**
    * extracting the logic that gathers MatchSet instances
    * @param criteria
    * @return
    */
   List<MatchSet> collectMatchSets(Criteria criteria) {
      //collect a list of MatchSet instances for each profile
      List<MatchSet> matchSets = profiles.values().stream()
              .map(profile -> profile.getMatchSet(criteria))
              .collect(Collectors.toList());
      return matchSets;
   }
}
