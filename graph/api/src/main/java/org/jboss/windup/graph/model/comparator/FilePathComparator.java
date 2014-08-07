package org.jboss.windup.graph.model.comparator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Sorts file paths according to their alphabetical order.
 * 
 * For example:
 * <ul>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * <li>/foo/hat.class</li>
 * </ul>
 * 
 * Would become:
 * <ul>
 * <li>/foo/hat.class</li>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * </ul>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class FilePathComparator implements Comparator<String>
{

    @Override
    public int compare(String o1, String o2)
    {
        // if they are exactly the same, just short circuit everything
        // and return 0
        if (o1.equals(o2))
        {
            return 0;
        }

        // split by the path separator (/ or \)
        Path path1 = Paths.get(o1);
        Path path2 = Paths.get(o2);

        if (path1.getNameCount() != path2.getNameCount())
        {
            // if there are differing number of path elements, compare based on number of segments
            return path1.getNameCount() - path2.getNameCount();
        }
        else
        {
            // otherwise, compare each segment
            for (int i = 0; i < path1.getNameCount(); i++)
            {
                String o1Segment = path1.getName(i).toString();
                String o2Segment = path2.getName(i).toString();

                // if the segments are different, return the results of this comparison
                if (!o1Segment.equals(o2Segment))
                {
                    return o1Segment.compareTo(o2Segment);
                }
            }

            // no segments differed, so just return 0 (same path)
            return 0;
        }
    }
}
