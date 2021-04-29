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

import org.codehaus.plexus.classworlds.realm.ClassRealm;
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

    private static final String NO_SUCH_REALM_EXCEPTION = "org.codehaus.plexus.classworlds.realm.NoSuchRealmException";

    /**
     * Import the core Aether library from the maven distribution.
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
     * Imports aether-util library from the user's Maven distribution.
     * <p>
     * PRECONDITION: the classLoader parameter is an instance of ClassRealm.
     * </p>
     *
     * @param classLoader the Classloader which needs to access aether-util.
     */
    private static void importAether( ClassLoader classLoader )
    {
        ClassRealm classRealm = (ClassRealm) classLoader;
        try
        {
            classRealm.importFrom( "plexus.core", "org.eclipse.aether.util" );
        }
        catch ( Exception e )
        {
            if ( NO_SUCH_REALM_EXCEPTION.equals( e.getClass().getCanonicalName() ) )
            {
                LOGGER.info( "'plexus.core' ClassRealm could not be found. "
                    + "Ignore this message if you are using the library outside of a Maven execution.", e );
            }
            else
            {
                // another exception
                LOGGER.error( "Unexpected exception when importing Aether library to the '{}' ClassRealm", classLoader,
                              e );
            }
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
            if ( "org.codehaus.plexus.classworlds.realm.ClassRealm".equals( clazz.getCanonicalName() ) )
            {
                return true;
            }
        }
        return false;
    }
}
