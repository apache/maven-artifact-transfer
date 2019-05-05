package org.apache.maven.shared.transfer.project;

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
import java.lang.reflect.Method;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This util class will import the Aether library available from the installed Maven distribution. It will do nothing if
 * it is called from outside of a ClassRealm.
 *
 * @since 0.11.1
 * @author Gabriel Belingueres <a href="mailto:belingueres@gmail.com">belingueres@gmail.com</a>
 */
public class MavenAetherUtils
{

    private static final Logger LOGGER = LoggerFactory.getLogger( MavenAetherUtils.class );

    /**
     * Import the core Aether library from the maven distribution.
     *
     * @param pluginDescriptor the plugin descriptor where the operation will be executed.
     * @throws MojoExecutionException if there is an error when importing the library.
     */
    public static void importAetherLibrary()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if ( isClassRealm( classLoader ) )
        {
            importAether( classLoader );
        }
    }

    /**
     * Using reflection check if the Classloader is actually a ClassRealm.
     * <p>
     * PRECONDITION: the classLoader parameter is an instance of ClassRealm.
     * </p>
     *
     * @param classLoader the Classloader to test.
     */
    private static void importAether( ClassLoader classLoader )
    {
        try
        {
            try
            {
                Method importFromMethod = classLoader.getClass().getMethod( "importFrom", String.class, String.class );
                importFromMethod.invoke( classLoader, "plexus.core", "org.eclipse.aether.util" );
            }
            catch ( InvocationTargetException e )
            {
                if ( "NoSuchRealmException".equals( e.getCause().getClass().getSimpleName() ) )
                {
                    LOGGER.info( "'plexus.core' ClassRealm could not be found. "
                        + "Ignore this message if you are using the library outside of a Maven execution.", e );
                }
                else
                {
                    // another exception
                    throw e;
                }
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Unexpected exception when importing Aether library to the '{}' ClassRealm", classLoader, e );
        }
    }

    /**
     * Using reflection, check if the Classloader is actually an instance of a ClassRealm.
     *
     * @param classLoader the Classloader to test.
     * @return true if it an instance of ClassRealm; false otherwise.
     */
    private static boolean isClassRealm( ClassLoader classLoader )
    {
        for ( Class<?> clazz = classLoader.getClass(); clazz != null; clazz = clazz.getSuperclass() )
        {
            if ( "ClassRealm".equals( clazz.getSimpleName() ) )
            {
                return true;
            }
        }
        return false;
    }
}
