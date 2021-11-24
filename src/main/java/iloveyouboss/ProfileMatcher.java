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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/* A ProfileMatcher collects all of the relevant profiles. Given a set of criteria from a client,
 * the ProfileMatcher instance iterates the profiles and returns those matching the criteria,
 * along with the MatchSet instance (which provides the ability to obtain the score of the match)
 */
public class ProfileMatcher {

   private Map<String, Profile> profiles = new HashMap<>(); 
   private static final int DEFAULT_POOL_SIZE = 4;
   // We need to access the ExecutorService instance from the test, so we extract its instantiation to the field level
   private ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
   // and provide a package-access-level getter method to return the ExecutorService reference.
   ExecutorService getExecutor() {
      return executor;
   }

   public void add(Profile profile) {
      profiles.put(profile.getId(), profile);
   }

   /**
    * To support stubbing the behavior of process, overload findMatchingProfiles
    * Change its existing implementation to take an additional argument, processFunction,
    * that represents the function to execute in each thread (BiConsumer - takes 2 arguments, no return)
    * @param criteria
    * @param listener
    * @param matchSets
    * @param processFunction
    */
   public void findMatchingProfiles(Criteria criteria,
                                    MatchListener listener,
                                    List<MatchSet> matchSets,
                                    BiConsumer<MatchListener, MatchSet> processFunction) {

      //For each match set,
      for (MatchSet set: matchSets) {
         // create and spawn a thread that
         Runnable runnable = () -> processFunction.accept(listener, set); //Use the processFunction function reference to call the appropriate logic to process each MatchSet
         executor.execute(runnable);
      }
      executor.shutdown();
   }

   /**
    * Add an implementation of findMatchingProfiles with the original signature that delegates to the overloaded version
    * (the one that takes a function argument, at line 47)
    * @param criteria
    * @param listener
    */
   public void findMatchingProfiles(Criteria criteria, MatchListener listener) {
      findMatchingProfiles(criteria, listener, collectMatchSets(criteria), this::process);
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

   /**
    * extract the application-specific logic that
    * sends matching profile information to a listener
    * @param listener
    * @param set
    */
   void process(MatchListener listener, MatchSet set) {
      // if a matches request to the MatchSet returns true
      // send the profile and corresponding MatchSet object to the MatchListener
      if (set.matches())
         listener.foundMatch(profiles.get(set.getProfileId()), set);
   }
}
