package org.jboss.windup.engine.decompilers.procyon;


import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Keeps a set of types which failed to load.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
final class NoRetryMetadataSystem extends MetadataSystem {
    private static final Logger log = LoggerFactory.getLogger( NoRetryMetadataSystem.class );

    private final Set<String> _failedTypes = new HashSet<>();

    NoRetryMetadataSystem() {
    }

    NoRetryMetadataSystem(final String classPath) {
        super(classPath);
    }

    NoRetryMetadataSystem(final ITypeLoader typeLoader) {
        super(typeLoader);
    }

    @Override
    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive) {
        if (_failedTypes.contains(descriptor))
            return null;

        TypeDefinition result = super.resolveType(descriptor, mightBePrimitive);

        if (result == null)
            _failedTypes.add(descriptor);

        return result;
    }
}// class