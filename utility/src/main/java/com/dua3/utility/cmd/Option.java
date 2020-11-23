package com.dua3.utility.cmd;

import java.util.Collection;

/**
 * Command line option.
 */
public interface Option {
    
    String name();
    Collection<String> names();
    String description();
    int minOccurrences();
    int maxOccurrences();
    int minArity();
    int maxArity();

}
