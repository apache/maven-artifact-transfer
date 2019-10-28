package org.apache.maven.shared.transfer.dependencies.collect.internal;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;

/**
 * This class is a copy of their homonymous in the Eclipse Aether library, adapted to work with Sonatype Aether.
 * 
 * @author Gabriel Belingueres
 */
public class Maven30ConfigUtils
{

    private Maven30ConfigUtils()
    {
        // hide constructor
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static Object getObject( Map<?, ?> properties, Object defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value != null )
            {
                return value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static Object getObject( RepositorySystemSession session, Object defaultValue, String... keys )
    {
        return getObject( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static String getString( Map<?, ?> properties, String defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof String )
            {
                return (String) value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static String getString( RepositorySystemSession session, String defaultValue, String... keys )
    {
        return getString( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static int getInteger( Map<?, ?> properties, int defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Number )
            {
                return ( (Number) value ).intValue();
            }

            try
            {
                return Integer.valueOf( (String) value );
            }
            catch ( Exception e )
            {
                // try next key
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static int getInteger( RepositorySystemSession session, int defaultValue, String... keys )
    {
        return getInteger( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static long getLong( Map<?, ?> properties, long defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Number )
            {
                return ( (Number) value ).longValue();
            }

            try
            {
                return Long.valueOf( (String) value );
            }
            catch ( Exception e )
            {
                // try next key
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static long getLong( RepositorySystemSession session, long defaultValue, String... keys )
    {
        return getLong( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static boolean getBoolean( Map<?, ?> properties, boolean defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Boolean )
            {
                return ( (Boolean) value ).booleanValue();
            }
            else if ( value instanceof String )
            {
                return Boolean.parseBoolean( (String) value );
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static boolean getBoolean( RepositorySystemSession session, boolean defaultValue, String... keys )
    {
        return getBoolean( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static List<?> getList( Map<?, ?> properties, List<?> defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof List )
            {
                return (List<?>) value;
            }
            else if ( value instanceof Collection )
            {
                return Collections.unmodifiableList( new ArrayList<Object>( (Collection<?>) value ) );
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static List<?> getList( RepositorySystemSession session, List<?> defaultValue, String... keys )
    {
        return getList( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static Map<?, ?> getMap( Map<?, ?> properties, Map<?, ?> defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Map )
            {
                return (Map<?, ?>) value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static Map<?, ?> getMap( RepositorySystemSession session, Map<?, ?> defaultValue, String... keys )
    {
        return getMap( session.getConfigProperties(), defaultValue, keys );
    }

}