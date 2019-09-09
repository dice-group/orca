package org.dice_research.ldcbench.nodes.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.Lang;

import java.util.Random;
/**
 * 
 * Useful methods for Jena Lang issues
 * 
 * @author Geraldo de Souza Jr
 *
 */

public class LangUtils {
    
  /**
   * 
   * Method that returns a list of Lang Types, that could be used by the dumpFile Node
   * 
   *   
   * @return List of Lang Types
   */
    public static List<Lang> getAllowedLangs(){
        List<Lang> langlist = new ArrayList<Lang>();
        
        langlist.add(Lang.TTL);
        langlist.add(Lang.N3);
        langlist.add(Lang.RDFXML);
        langlist.add(Lang.NT);
        
        return langlist;
    }
    
    /**
     * 
     * Method that randomize a position in a list returned by 
     * the getAllowedLangs() method and return a random
     * Lang Type
     * 
     * @return a random Lang Type
     */
    public static Lang getRandomLang(long seed) {
        List<Lang> langlist = getAllowedLangs();
        Random rand = new Random(seed);
        int langPos = rand.nextInt(langlist.size());
        return langlist.get(langPos);
    }

}
