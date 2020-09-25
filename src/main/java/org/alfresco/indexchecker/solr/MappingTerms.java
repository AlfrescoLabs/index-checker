package org.alfresco.indexchecker.solr;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

/**
 * Stream mapper to get a Map from a (String, Integer) list.
 * 
 * ("a", 1, "b", 2) -> ("a", 1) ("b", 2)
 *
 */
public class MappingTerms
{

    private Map<String, Integer> map = new HashMap<>();

    private String key;

    public void accept(Object object)
    {
        if (object instanceof String)
        {
            key = (String) object;
        }
        else
        {
            map.put(key, (Integer) object);
        }
    }
    
    public MappingTerms combine(MappingTerms other)
    {
        throw new UnsupportedOperationException("Parallel Stream not supported");
    }

    public Map<String, Integer> finish()
    {
        return map;
    }

    public static Collector<Object, ?, Map<String, Integer>> collector()
    {
        return Collector.of(MappingTerms::new, MappingTerms::accept, MappingTerms::combine, MappingTerms::finish);
    }

}