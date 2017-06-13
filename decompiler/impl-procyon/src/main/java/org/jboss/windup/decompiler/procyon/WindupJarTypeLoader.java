/*
 * JarTypeLoader.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package org.jboss.windup.decompiler.procyon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.strobel.assembler.ir.ConstantPool;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.VerifyArgument;

public class WindupJarTypeLoader implements ITypeLoader
{
    private final static Logger LOG = Logger.getLogger(JarTypeLoader.class.getName());

    private final JarFile _jarFile;
    private final Map<String, String> _knownMappings = new ConcurrentHashMap<>(1000);

    public WindupJarTypeLoader(final JarFile jarFile)
    {
        _jarFile = VerifyArgument.notNull(jarFile, "jarFile");
    }

    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer)
    {
        try
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Attempting to load type: " + internalName + "...");
            }

            final JarEntry entry = _jarFile.getJarEntry(internalName + ".class");

            if (entry == null)
            {
                final String mappedName = _knownMappings.get(internalName);

                return mappedName != null &&
                            !mappedName.equals(internalName) && tryLoadType(mappedName, buffer);
            }

            final InputStream inputStream = _jarFile.getInputStream(entry);

            int remainingBytes = inputStream.available();

            buffer.reset(remainingBytes);

            while (remainingBytes > 0)
            {
                final int bytesRead = inputStream.read(buffer.array(), buffer.position(), remainingBytes);

                if (bytesRead < 0)
                {
                    break;
                }

                buffer.position(buffer.position() + bytesRead);
                remainingBytes -= bytesRead;
            }

            buffer.position(0);

            final String actualName = getInternalNameFromClassFile(buffer);

            if (actualName != null && !actualName.equals(internalName))
            {
                _knownMappings.put(actualName, internalName);
            }

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Type loaded from " + _jarFile.getName() + "!" + entry.getName() + ".");
            }

            return true;
        }
        catch (IOException e)
        {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }

    private static String getInternalNameFromClassFile(final Buffer b)
    {
        final long magic = b.readInt() & 0xFFFFFFFFL;

        if (magic != 0xCAFEBABEL)
        {
            return null;
        }

        b.readUnsignedShort(); // minor version
        b.readUnsignedShort(); // major version

        final ConstantPool constantPool = ConstantPool.read(b);

        b.readUnsignedShort(); // access flags

        final ConstantPool.TypeInfoEntry thisClass = constantPool.getEntry(b.readUnsignedShort());

        b.position(0);

        return thisClass.getName();
    }
}
