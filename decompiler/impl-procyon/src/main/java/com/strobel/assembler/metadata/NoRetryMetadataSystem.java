package com.strobel.assembler.metadata;

import org.apache.commons.collections4.map.LRUMap;

import com.strobel.core.VerifyArgument;

/**
 * Keeps a set of types which failed to load and also uses an LRUCache to reduce the memory footprint for cached lookups.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public final class NoRetryMetadataSystem extends MetadataSystem
{
    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_NAME = new TypeDefinition['Z' - 'B' + 1];
    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_DESCRIPTOR = new TypeDefinition[16];
    private final LRUMap<String, Boolean> failedTypes = new LRUMap<>(1000);
    private final LRUMap<String, TypeDefinition> resolvedTypes = new LRUMap<>(1000);
    private final ITypeLoader typeLoader;

    public NoRetryMetadataSystem(final ITypeLoader typeLoader)
    {
        super(typeLoader);
        this.typeLoader = typeLoader;
    }

    public void addTypeDefinition(final TypeDefinition type)
    {
        VerifyArgument.notNull(type, "type");
        resolvedTypes.put(type.getInternalName(), type);
    }

    @Override
    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive)
    {
        if (failedTypes.containsKey(descriptor))
            return null;

        TypeDefinition result = resolveTypeInternal(descriptor, mightBePrimitive);

        if (result == null)
            failedTypes.put(descriptor, true);

        return result;
    }

    protected TypeDefinition resolveTypeInternal(final String descriptor, final boolean mightBePrimitive)
    {
        VerifyArgument.notNull(descriptor, "descriptor");

        if (mightBePrimitive)
        {
            if (descriptor.length() == 1)
            {
                final int primitiveHash = descriptor.charAt(0) - 'B';

                if (primitiveHash >= 0 && primitiveHash < PRIMITIVE_TYPES_BY_DESCRIPTOR.length)
                {
                    final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_DESCRIPTOR[primitiveHash];

                    if (primitiveType != null)
                    {
                        return primitiveType;
                    }
                }
            }
            else
            {
                final int primitiveHash = hashPrimitiveName(descriptor);

                if (primitiveHash >= 0 && primitiveHash < PRIMITIVE_TYPES_BY_NAME.length)
                {
                    final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_NAME[primitiveHash];

                    if (primitiveType != null && descriptor.equals(primitiveType.getName()))
                    {
                        return primitiveType;
                    }
                }
            }
        }

        TypeDefinition cachedDefinition = resolvedTypes.get(descriptor);

        if (cachedDefinition != null)
        {
            return cachedDefinition;
        }

        final Buffer buffer = new Buffer(0);

        if (!this.typeLoader.tryLoadType(descriptor, buffer))
        {
            return null;
        }

        final TypeDefinition typeDefinition = ClassFileReader.readClass(ClassFileReader.OPTIONS_DEFAULT, this, buffer);

        cachedDefinition = resolvedTypes.put(descriptor, typeDefinition);
        typeDefinition.setTypeLoader(this.typeLoader);

        if (cachedDefinition != null)
        {
            return cachedDefinition;
        }

        return typeDefinition;
    }

    private static int hashPrimitiveName(final String name)
    {
        if (name.length() < 3)
        {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % 16;
    }
}