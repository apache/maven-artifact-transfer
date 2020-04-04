package org.apache.maven.shared.transfer.dependencies.resolve.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.InvocationTargetException;

import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;

/**
 * Invokes method on objects using reflection.
 */
final class Invoker
{
    private Invoker()
    {
        // do not instantiate
    }

    public static <T> T invoke( Object object, String method )
        throws DependencyResolverException
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            T invoke = (T) object.getClass().getMethod( method ).invoke( object );
            return invoke;
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
        }
    }

    public static <T> T invoke( Class<?> objectClazz, String staticMethod, Class<?> argClazz, Object arg )
        throws DependencyResolverException
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            T invoke = (T) objectClazz.getMethod( staticMethod, argClazz ).invoke( null, arg );
            return invoke;
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
        }
    }

    /**
     * <strong>Note:</strong> Ensure that argClasses and args have the same number of elements
     * 
     * @param objectClazz the class of the static method
     * @param staticMethod the static method to call
     * @param argClasses the classes of the argument, used to select the right static method
     * @param args the actual arguments to be passed
     * @return the result of the method invocation
     * @throws DependencyResolverException if any checked exception occurs
     */
    public static <T> T invoke( Class<?> objectClazz, String staticMethod, Class<?>[] argClasses, Object[] args )
        throws DependencyResolverException
    {
        if ( args.length != argClasses.length )
        {
            throw new IllegalArgumentException( "The number of elements in argClasses and args are not the same." );
        }

        try
        {
            @SuppressWarnings( "unchecked" )
            T invoke = (T) objectClazz.getMethod( staticMethod, argClasses ).invoke( null, args );
            return invoke;
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
        }
    }
}
