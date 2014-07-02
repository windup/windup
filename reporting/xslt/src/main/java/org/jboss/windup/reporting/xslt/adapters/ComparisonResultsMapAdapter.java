package org.jboss.windup.reporting.xslt.adapters;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.windup.reporting.xslt.adapters.ComparisonResultsMapAdapter.MapElement;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ComparisonResultsMapAdapter extends XmlAdapter<MapElement, Map<Path, FileHashComparer.MatchResult>> {

    @Override public MapElement marshal(Map<Path, FileHashComparer.MatchResult> v) throws Exception {
        if (v == null || v.isEmpty()) {return null;}

        MapElement map = new MapElement();
        for( Path key : v.keySet() ) {
            map.addEntry(key, v.get(key));
        }
        return map;
    }

    @Override
    public Map<Path, FileHashComparer.MatchResult> unmarshal(MapElement v) throws Exception {
        if (v == null) {return null;}

        Map<Path, FileHashComparer.MatchResult> map = new HashMap(v.entries.size());
        for(EntryElement entry: v.entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }
    

    
    @XmlRootElement(name = "map")
    public static class MapElement {

        @XmlElement(name = "entry")
        public List<EntryElement> entries = new LinkedList();

        public void addEntry(Path key, FileHashComparer.MatchResult value) {
            entries.add( new EntryElement(key, value) );
        }
    }

    @XmlRootElement
    public static class EntryElement {

        @XmlAttribute public Path key;
        @XmlAttribute public FileHashComparer.MatchResult value;

        public EntryElement() {}
        public EntryElement(Path key, FileHashComparer.MatchResult value) {
            this.key = key;
            this.value = value;
        }
    }    
    
}// class
