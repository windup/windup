/*
 * ParameterizedType.java
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

package com.strobel.assembler.metadata;

import java.util.List;

/**
 * (WINDUP NOTE: Copied from the procyon source code and used here to override the version in the procyon jar. This will allow us to more reliably
 * abord the decompilation process within WINDUP). Whenever the thread is interrupted, this will throw an exception indicating that.
 * 
 * @author Mike Strobel
 */
final class ParameterizedType extends TypeReference implements IGenericInstance
{
    private final TypeReference _genericDefinition;
    private final List<TypeReference> _typeParameters;

    ParameterizedType(final TypeReference genericDefinition, final List<TypeReference> typeParameters)
    {
        _genericDefinition = genericDefinition;
        _typeParameters = typeParameters;
    }

    private void exitIfNeeded()
    {
        if (Thread.currentThread().isInterrupted())
        {
            throw new RuntimeException("Thread interrupted... exiting");
        }
    }

    @Override
    public String getName()
    {
        exitIfNeeded();
        return _genericDefinition.getName();
    }

    @Override
    public String getPackageName()
    {
        exitIfNeeded();
        return _genericDefinition.getPackageName();
    }

    @Override
    public String getFullName()
    {
        exitIfNeeded();
        return _genericDefinition.getFullName();
    }

    @Override
    public String getInternalName()
    {
        exitIfNeeded();
        return _genericDefinition.getInternalName();
    }

    @Override
    public TypeReference getDeclaringType()
    {
        exitIfNeeded();
        return _genericDefinition.getDeclaringType();
    }

    @Override
    public String getSimpleName()
    {
        exitIfNeeded();
        return _genericDefinition.getSimpleName();
    }

    @Override
    public boolean isGenericDefinition()
    {
        exitIfNeeded();
        return false;
    }

    @Override
    public List<GenericParameter> getGenericParameters()
    {
        exitIfNeeded();
        if (!_genericDefinition.isGenericDefinition())
        {
            final TypeDefinition resolvedDefinition = _genericDefinition.resolve();

            if (resolvedDefinition != null)
            {
                return resolvedDefinition.getGenericParameters();
            }
        }

        return _genericDefinition.getGenericParameters();
    }

    @Override
    public boolean hasTypeArguments()
    {
        exitIfNeeded();
        return true;
    }

    @Override
    public List<TypeReference> getTypeArguments()
    {
        exitIfNeeded();
        return _typeParameters;
    }

    @Override
    public IGenericParameterProvider getGenericDefinition()
    {
        exitIfNeeded();
        return _genericDefinition;
    }

    @Override
    public TypeReference getUnderlyingType()
    {
        exitIfNeeded();
        return _genericDefinition;
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter)
    {
        exitIfNeeded();
        return visitor.visitParameterizedType(this, parameter);
    }

    @Override
    public TypeDefinition resolve()
    {
        exitIfNeeded();
        return _genericDefinition.resolve();
    }

    @Override
    public FieldDefinition resolve(final FieldReference field)
    {
        exitIfNeeded();
        return _genericDefinition.resolve(field);
    }

    @Override
    public MethodDefinition resolve(final MethodReference method)
    {
        exitIfNeeded();
        return _genericDefinition.resolve(method);
    }

    @Override
    public TypeDefinition resolve(final TypeReference type)
    {
        exitIfNeeded();
        return _genericDefinition.resolve(type);
    }
}
