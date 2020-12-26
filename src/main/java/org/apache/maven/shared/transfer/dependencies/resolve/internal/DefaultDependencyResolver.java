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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.RepositorySystem;

import java.util.Collection;

/**
 *
 */
@Component( role = DependencyResolver.class, hint = "default" )
class DefaultDependencyResolver implements DependencyResolver
{
    @Requirement
    ArtifactHandlerManager artifactHandlerManager;

    @Requirement
    RepositorySystem repositorySystem;

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
            Collection<Dependency> coordinates, Collection<Dependency> managedDependencies, TransformableFilter filter )
            throws DependencyResolverException
    {
        validateBuildingRequest( buildingRequest );

        return getMavenDependencyResolver( buildingRequest )
                .resolveDependencies( coordinates, managedDependencies, filter );
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
            DependableCoordinate coordinate, TransformableFilter filter ) throws DependencyResolverException
    {
        validateParameters( buildingRequest, coordinate );
        return getMavenDependencyResolver( buildingRequest ).resolveDependencies( coordinate, filter );
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest, Model model,
            TransformableFilter filter ) throws DependencyResolverException
    {
        validateParameters( buildingRequest, model );
        return getMavenDependencyResolver( buildingRequest ).resolveDependencies( model, filter );
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
    {
        return new Maven31DependencyResolver( repositorySystem, artifactHandlerManager,
                buildingRequest.getRepositorySession(),
                RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ) );
    }

    private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
    }

}
