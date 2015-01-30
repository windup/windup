package org.jboss.windup.qs.skiparchives.bundler;

import java.util.logging.Logger;
import org.jboss.windup.qs.skiparchives.nexusreader.IndexToGavMappingConverterTest;
import org.jboss.windup.util.Logging;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecuteIndexToGavMappingConverterTest extends IndexToGavMappingConverterTest
{
    private static final Logger log = Logging.get(ExecuteIndexToGavMappingConverterTest.class);


    public ExecuteIndexToGavMappingConverterTest()
    {
        super();
    }
}
