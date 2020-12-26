package org.apache.maven.shared.transfer.repository.internal;

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

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.apache.maven.shared.transfer.repository.RepositoryManagerException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

/**
 * 
 */
@Component( role = RepositoryManager.class )
class DefaultRepositoryManager
    implements RepositoryManager, Contextualizable 
{
    private PlexusContainer container;
    
    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest, Artifact artifact )
    {
        try
        {
            return getMavenRepositoryManager( buildingRequest ).getPathForLocalArtifact( artifact );
        }
        catch ( ComponentLookupException | RepositoryManagerException e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coor )
    {
        try
        {
            return getMavenRepositoryManager( buildingRequest ).getPathForLocalArtifact( coor );
        }
        catch ( ComponentLookupException | RepositoryManagerException e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

    @Override
    public String getPathForLocalMetadata( ProjectBuildingRequest buildingRequest, ArtifactMetadata metadata )
    {
        if ( metadata instanceof ProjectArtifactMetadata )
        {
            DefaultArtifactCoordinate pomCoordinate = new DefaultArtifactCoordinate();
            pomCoordinate.setGroupId( metadata.getGroupId() );
            pomCoordinate.setArtifactId( metadata.getArtifactId() );
            pomCoordinate.setVersion( metadata.getBaseVersion() );
            pomCoordinate.setExtension( "pom" );
            return getPathForLocalArtifact( buildingRequest, pomCoordinate );
        }

        try
        {
            return getMavenRepositoryManager( buildingRequest ).getPathForLocalMetadata( metadata );
        }
        catch ( ComponentLookupException | RepositoryManagerException e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

    @Override
    public ProjectBuildingRequest setLocalRepositoryBasedir( ProjectBuildingRequest buildingRequest, File basedir )
    {
        try
        {
            return getMavenRepositoryManager( buildingRequest ).setLocalRepositoryBasedir( buildingRequest, basedir );
        }
        catch ( ComponentLookupException | RepositoryManagerException e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

    @Override
    public File getLocalRepositoryBasedir( ProjectBuildingRequest buildingRequest )
    {
        try
        {
            return getMavenRepositoryManager( buildingRequest ).getLocalRepositoryBasedir();
        }
        catch ( ComponentLookupException | RepositoryManagerException e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }
    
    private MavenRepositoryManager getMavenRepositoryManager( ProjectBuildingRequest buildingRequest )
        throws ComponentLookupException, RepositoryManagerException
    {
        RepositorySystem m31RepositorySystem = container.lookup( RepositorySystem.class );

        RepositorySystemSession session = Invoker.invoke( buildingRequest, "getRepositorySession" );

        return new Maven31RepositoryManager( m31RepositorySystem, session );
    }
    
    public void contextualize( Context context ) throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
