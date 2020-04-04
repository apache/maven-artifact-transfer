package org.apache.maven.shared.transfer.artifact.resolve.internal;

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

import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;


/**
 *
 */
@Component( role = ArtifactResolver.class, hint = "default" )
class DefaultArtifactResolver implements ArtifactResolver, Contextualizable
{
    private PlexusContainer container;

    @Override
    public ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
            throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, mavenArtifact );
        try
        {
            return getMavenArtifactResolver( buildingRequest ).resolveArtifact( mavenArtifact );
        }
        catch ( ComponentLookupException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

    @Override
    public ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
            throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, coordinate );
        try
        {
            return getMavenArtifactResolver( buildingRequest ).resolveArtifact( coordinate );
        }
        catch ( ComponentLookupException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( mavenArtifact == null )
        {
            throw new IllegalArgumentException( "The parameter mavenArtifact is not allowed to be null." );
        }
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( coordinate == null )
        {
            throw new IllegalArgumentException( "The parameter coordinate is not allowed to be null." );
        }
    }

    /**
     * @return true if the current Maven version is Maven 3.1.
     */
    private boolean isMaven31()
    {
        return canFindCoreClass( "org.eclipse.aether.artifact.Artifact" ); // Maven 3.1 specific
    }

    private boolean canFindCoreClass( String className )
    {
        try
        {
            Thread.currentThread().getContextClassLoader().loadClass( className );

            return true;
        }
        catch ( ClassNotFoundException e )
        {
            return false;
        }
    }

    /**
     * Injects the Plexus content.
     *
     * @param context Plexus context to inject.
     * @throws ContextException if the PlexusContainer could not be located.
     */
    public void contextualize( Context context ) throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private MavenArtifactResolver getMavenArtifactResolver( ProjectBuildingRequest buildingRequest )
            throws ComponentLookupException, ArtifactResolverException
    {
        if ( isMaven31() )
        {
            org.eclipse.aether.RepositorySystem repositorySystem = container.lookup(
                    org.eclipse.aether.RepositorySystem.class );

            List<org.eclipse.aether.repository.RemoteRepository> aetherRepositories = Invoker.invoke(
                    RepositoryUtils.class, "toRepos", List.class, buildingRequest.getRemoteRepositories() );

            org.eclipse.aether.RepositorySystemSession session = Invoker.invoke( buildingRequest,
                    "getRepositorySession" );

            return new Maven31ArtifactResolver( repositorySystem, aetherRepositories, session );

        }
        else
        {
            org.sonatype.aether.RepositorySystem repositorySystem = container.lookup(
                    org.sonatype.aether.RepositorySystem.class );

            List<org.sonatype.aether.repository.RemoteRepository> aetherRepositories = Invoker.invoke(
                    RepositoryUtils.class, "toRepos", List.class, buildingRequest.getRemoteRepositories() );

            org.sonatype.aether.RepositorySystemSession session = Invoker.invoke( buildingRequest,
                    "getRepositorySession" );

            return new Maven30ArtifactResolver( repositorySystem, aetherRepositories, session );
        }


    }
}
