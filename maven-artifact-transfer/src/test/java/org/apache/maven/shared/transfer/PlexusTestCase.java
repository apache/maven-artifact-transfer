package org.apache.maven.shared.transfer;

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

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.fail;

/**
 * This is a verbatim copy of PlexusTestCase from org.eclipse.sisu.plexus BUT is
 * improved and changed to use Junit4.
 *
 * Note: in general, WE NEED Plexus still as some Maven core components are still plexus, but
 * for NEW CODE WE MUST NOT use plexus, but use "layer below": SISU.
 */
public abstract class PlexusTestCase
{
    private PlexusContainer container;
    private static String basedir;

    public PlexusTestCase()
    {
    }

    @Before
    public final void basedirSetup() throws Exception
    {
        basedir = getBasedir();
    }

    protected void setupContainer()
    {
        DefaultContext context = new DefaultContext();
        context.put( "basedir", getBasedir() );
        this.customizeContext( context );
        boolean hasPlexusHome = context.contains( "plexus.home" );
        if ( !hasPlexusHome )
        {
            File f = getTestFile( "target/plexus-home" );
            if ( !f.isDirectory() )
            {
                f.mkdir();
            }

            context.put( "plexus.home", f.getAbsolutePath() );
        }

        String config = this.getCustomConfigurationName();
        ContainerConfiguration containerConfiguration = ( new DefaultContainerConfiguration() )
                .setName( "test" )
                .setContext( context.getContextData() )
                .setClassPathScanning( PlexusConstants.SCANNING_INDEX );
        if ( config != null )
        {
            containerConfiguration.setContainerConfiguration( config );
        }
        else
        {
            String resource = this.getConfigurationName( (String) null );
            containerConfiguration.setContainerConfiguration( resource );
        }

        this.customizeContainerConfiguration( containerConfiguration );

        try
        {
            this.container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException var6 )
        {
            var6.printStackTrace();
            fail( "Failed to create plexus container." );
        }

    }

    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
    }

    protected void customizeContext( Context context )
    {
    }

    protected PlexusConfiguration customizeComponentConfiguration()
    {
        return null;
    }

    @After
    public final void containerDispose() throws Exception
    {
        if ( this.container != null )
        {
            this.container.dispose();
            this.container = null;
        }

    }

    protected PlexusContainer getContainer()
    {
        if ( this.container == null )
        {
            this.setupContainer();
        }

        return this.container;
    }

    protected InputStream getConfiguration() throws Exception
    {
        return this.getConfiguration( (String) null );
    }

    protected InputStream getConfiguration( String subname ) throws Exception
    {
        return this.getResourceAsStream( this.getConfigurationName( subname ) );
    }

    protected String getCustomConfigurationName()
    {
        return null;
    }

    protected String getConfigurationName( String subname )
    {
        return this.getClass().getName().replace( '.', '/' ) + ".xml";
    }

    protected InputStream getResourceAsStream( String resource )
    {
        return this.getClass().getResourceAsStream( resource );
    }

    protected ClassLoader getClassLoader()
    {
        return this.getClass().getClassLoader();
    }

    protected Object lookup( String componentKey ) throws Exception
    {
        return this.getContainer().lookup( componentKey );
    }

    protected Object lookup( String role, String roleHint ) throws Exception
    {
        return this.getContainer().lookup( role, roleHint );
    }

    protected <T> T lookup( Class<T> componentClass ) throws Exception
    {
        return this.getContainer().lookup( componentClass );
    }

    protected <T> T lookup( Class<T> componentClass, String roleHint ) throws Exception
    {
        return this.getContainer().lookup( componentClass, roleHint );
    }

    protected void release( Object component ) throws Exception
    {
        this.getContainer().release( component );
    }

    public static File getTestFile( String path )
    {
        return new File( getBasedir(), path );
    }

    public static File getTestFile( String basedir, String path )
    {
        File basedirFile = new File( basedir );
        if ( !basedirFile.isAbsolute() )
        {
            basedirFile = getTestFile( basedir );
        }

        return new File( basedirFile, path );
    }

    public static String getTestPath( String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    public static String getTestPath( String basedir, String path )
    {
        return getTestFile( basedir, path ).getAbsolutePath();
    }

    public static String getBasedir()
    {
        if ( basedir != null )
        {
            return basedir;
        }
        else
        {
            basedir = System.getProperty( "basedir" );
            if ( basedir == null )
            {
                basedir = ( new File( "" ) ).getAbsolutePath();
            }

            return basedir;
        }
    }

    public String getTestConfiguration()
    {
        return getTestConfiguration( this.getClass() );
    }

    public static String getTestConfiguration( Class<?> clazz )
    {
        String s = clazz.getName().replace( '.', '/' );
        return s.substring( 0, s.indexOf( "$" ) ) + ".xml";
    }
}
