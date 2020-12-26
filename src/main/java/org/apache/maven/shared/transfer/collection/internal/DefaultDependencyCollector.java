package org.apache.maven.shared.transfer.collection.internal;

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
import java.util.Objects;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.collection.CollectResult;
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;
import org.apache.maven.shared.transfer.collection.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * This DependencyCollector passes the request to the proper Maven 3.x implementation
 *
 * @author Robert Scholte
 */
@Component( role = DependencyCollector.class, hint = "default" )
class DefaultDependencyCollector implements DependencyCollector, Contextualizable
{
    private PlexusContainer container;

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, Dependency root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );

        try
        {
            return getMavenDependencyCollector( buildingRequest ).collectDependencies( root );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyCollectionException( e.getMessage(), e );
        }
    }

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, DependableCoordinate root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );

        try
        {
            return getMavenDependencyCollector( buildingRequest ).collectDependencies( root );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyCollectionException( e.getMessage(), e );
        }
    }

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, Model root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );

        try
        {
            return getMavenDependencyCollector( buildingRequest ).collectDependencies( root );
        }
        catch ( ComponentLookupException e )
        {
            throw new DependencyCollectionException( e.getMessage(), e );
        }
    }

  private void validateParameters( ProjectBuildingRequest buildingRequest, DependableCoordinate root )
  {
    validateBuildingRequestAndRoot( buildingRequest, root );
  }

  private void validateParameters( ProjectBuildingRequest buildingRequest, Dependency root )
  {
    validateBuildingRequestAndRoot( buildingRequest, root );
  }

  private void validateParameters( ProjectBuildingRequest buildingRequest, Model root )
  {
    validateBuildingRequestAndRoot( buildingRequest, root );
  }

  private void validateBuildingRequestAndRoot( ProjectBuildingRequest buildingRequest, Object root )
  {
    validateBuildingRequest( buildingRequest );
    validateRoot( root );
  }

  private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
  {
    Objects.requireNonNull( buildingRequest, "The parameter buildingRequest is not allowed to be null." );
  }

  private void validateRoot( Object root )
  {
    Objects.requireNonNull( root, "The parameter root is not allowed to be null." );
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

    private MavenDependencyCollector getMavenDependencyCollector( ProjectBuildingRequest buildingRequest )
            throws ComponentLookupException, DependencyCollectionException
    {
        ArtifactHandlerManager artifactHandlerManager = container.lookup( ArtifactHandlerManager.class );

        RepositorySystem m31RepositorySystem = container.lookup( RepositorySystem.class );

        RepositorySystemSession session = Invoker.invoke( buildingRequest, "getRepositorySession" );

        List<RemoteRepository> aetherRepositories = Invoker.invoke(
                RepositoryUtils.class, "toRepos", List.class, buildingRequest.getRemoteRepositories() );

        return new Maven31DependencyCollector( m31RepositorySystem, artifactHandlerManager, session,
                aetherRepositories );
    }

}
