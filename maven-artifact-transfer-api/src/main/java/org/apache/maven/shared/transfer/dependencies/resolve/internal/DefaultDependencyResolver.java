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

import java.util.Collection;
import java.util.Map;

import static org.apache.maven.shared.transfer.support.Selector.selectDelegate;

/**
 *
 */
@Component( role = DependencyResolver.class, hint = "default" )
public class DefaultDependencyResolver
        implements DependencyResolver
{
    @Requirement( role = DependencyResolverDelegate.class )
    private Map<String, DependencyResolverDelegate> delegates;

    public DefaultDependencyResolver()
    {
    }

    public DefaultDependencyResolver( Map<String, DependencyResolverDelegate> delegates )
    {
        this.delegates = delegates;
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
                                                         DependableCoordinate coordinate,
                                                         TransformableFilter filter )
            throws DependencyResolverException
    {
        validateParameters( buildingRequest, coordinate );
        return selectDelegate( delegates ).resolveDependencies( buildingRequest, coordinate, filter );
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
                                                         Model model,
                                                         TransformableFilter filter )
            throws DependencyResolverException
    {
        validateParameters( buildingRequest, model );
        return selectDelegate( delegates ).resolveDependencies( buildingRequest, model, filter );
    }

    @Override
    public Iterable<ArtifactResult> resolveDependencies( ProjectBuildingRequest buildingRequest,
                                                         Collection<Dependency> dependencies,
                                                         Collection<Dependency> managedDependencies,
                                                         TransformableFilter filter )
            throws DependencyResolverException
    {
        validateBuildingRequest( buildingRequest );
        return selectDelegate( delegates ).resolveDependencies(
                buildingRequest, dependencies, managedDependencies, filter );
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

    private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
    }
}
