package com.strobel.assembler.metadata;

import com.strobel.core.VerifyArgument;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com>Marco Rizzi</a>
 */
public class WindupMetadataSystem extends MetadataSystem {
    public WindupMetadataSystem(final String classPath) {
        super(new WindupClasspathTypeLoader(VerifyArgument.notNull(classPath, "classPath")));
    }

    public WindupMetadataSystem(final ITypeLoader typeLoader) {
        super(typeLoader);
    }
}
