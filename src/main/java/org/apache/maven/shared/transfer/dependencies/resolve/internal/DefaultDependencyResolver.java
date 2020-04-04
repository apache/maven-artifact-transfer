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

import java.util.Collection;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
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
@Component( role = DependencyResolver.class, hint = "default" )
class DefaultDependencyResolver implements DependencyResolver, Contextualizable
{
    private PlexusContainer container;

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
            Collection<Dependency> coordinates, Collection<Dependency> managedDependencies, TransformableFilter filter )
            throws DependencyResolverException
    {
        validateBuildingRequest( buildingRequest );

        try
        {
            return getMavenDependencyResolver( buildingRequest ).resolveDependencies( coordinates, managedDependencies,
                    filter );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
        }
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
            DependableCoordinate coordinate, TransformableFilter filter ) throws DependencyResolverException
    {
        validateParameters( buildingRequest, coordinate );
        try
        {
            return getMavenDependencyResolver( buildingRequest ).resolveDependencies( coordinate, filter );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
        }
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest, Model model,
            TransformableFilter filter ) throws DependencyResolverException
    {
        validateParameters( buildingRequest, model );
        try
        {
            return getMavenDependencyResolver( buildingRequest ).resolveDependencies( model, filter );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyResolverException( e.getMessage(), e );
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

    private void validateParameters( ProjectBuildingRequest buildingRequest, DependableCoordinate coordinate )
    {
        validateBuildingRequest( buildingRequest );
        if ( coordinate == null )
        {
            throw new IllegalArgumentException( "The parameter coordinate is not allowed to be null." );
        }
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Model model )
    {
        validateBuildingRequest( buildingRequest );
        if ( model == null )
        {
            throw new IllegalArgumentException( "The parameter model is not allowed to be null." );
        }

    }

    private MavenDependencyResolver getMavenDependencyResolver( ProjectBuildingRequest buildingRequest )
            throws ComponentLookupException, DependencyResolverException
    {
        ArtifactHandlerManager artifactHandlerManager = container.lookup( ArtifactHandlerManager.class );

        if ( isMaven31() )
        {
            org.eclipse.aether.RepositorySystem m31RepositorySystem = container.lookup(
                    org.eclipse.aether.RepositorySystem.class );

            org.eclipse.aether.RepositorySystemSession session = Invoker.invoke( buildingRequest,
                    "getRepositorySession" );

            List<org.eclipse.aether.repository.RemoteRepository> aetherRepositories = Invoker.invoke(
                    RepositoryUtils.class, "toRepos", List.class, buildingRequest.getRemoteRepositories() );

            return new Maven31DependencyResolver( m31RepositorySystem, artifactHandlerManager, session,
                    aetherRepositories );
        }
        else
        {
            org.sonatype.aether.RepositorySystem m30RepositorySystem = container.lookup(
                    org.sonatype.aether.RepositorySystem.class );

            org.sonatype.aether.RepositorySystemSession session = Invoker.invoke( buildingRequest,
                    "getRepositorySession" );

            List<org.sonatype.aether.repository.RemoteRepository> aetherRepositories = Invoker.invoke(
                    RepositoryUtils.class, "toRepos", List.class, buildingRequest.getRemoteRepositories() );

            return new Maven30DependencyResolver( m30RepositorySystem, artifactHandlerManager, session,
                    aetherRepositories );

        }
    }

    private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
    }

}
