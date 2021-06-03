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
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.SonatypeAetherFilterTransformer;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Component( role = DependencyResolverDelegate.class, hint = Selector.MAVEN_3_0_X )
public class Maven30DependencyResolver
        extends DelegateSupport
        implements DependencyResolverDelegate
{
    @Requirement
    private RepositorySystem repositorySystem;

    @Requirement
    private ArtifactHandlerManager artifactHandlerManager;

    public Maven30DependencyResolver()
    {
    }

    public Maven30DependencyResolver( RepositorySystem repositorySystem,
                                      ArtifactHandlerManager artifactHandlerManager )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
        this.artifactHandlerManager = Objects.requireNonNull( artifactHandlerManager );
    }

    /**
     * Based on RepositoryUtils#toDependency(org.apache.maven.model.Dependency, ArtifactTypeRegistry)
     *
     * @param coordinate  {@link DependableCoordinate}
     * @param stereotypes {@link org.sonatype.aether.artifact.ArtifactTypeRegistry
     * @return as Aether Dependency
     */
    private static Dependency toDependency( DependableCoordinate coordinate, ArtifactTypeRegistry stereotypes )
    {
        ArtifactType stereotype = stereotypes.get( coordinate.getType() );
        if ( stereotype == null )
        {
            stereotype = new DefaultArtifactType( coordinate.getType() );
        }

        Artifact artifact = new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(),
                coordinate.getClassifier(), null, coordinate.getVersion(), null, stereotype );

        return new Dependency( artifact, null );
    }

    private static Dependency toDependency( org.apache.maven.model.Dependency mavenDependency,
                                            ArtifactTypeRegistry typeRegistry )
    {
        return RepositoryUtils.toDependency( mavenDependency, typeRegistry );
    }

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            ProjectBuildingRequest buildingRequest,
            DependableCoordinate coordinate,
            TransformableFilter dependencyFilter ) throws DependencyResolverException
    {
        ArtifactTypeRegistry typeRegistry = createTypeRegistry();

        Dependency aetherRoot = toDependency( coordinate, typeRegistry );

        CollectRequest request = new CollectRequest( aetherRoot,
                RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ) );

        return resolveDependencies( buildingRequest, dependencyFilter, request );
    }

    private ArtifactTypeRegistry createTypeRegistry()
    {
        return RepositoryUtils.newArtifactTypeRegistry( artifactHandlerManager );
    }

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            ProjectBuildingRequest buildingRequest,
            Model model,
            TransformableFilter dependencyFilter ) throws DependencyResolverException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( model.getPackaging() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact = new DefaultArtifact( model.getGroupId(), model.getArtifactId(), extension,
                model.getVersion() );

        Dependency aetherRoot = new Dependency( aetherArtifact, null );

        CollectRequest request = new CollectRequest( aetherRoot,
                RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ) );

        request.setDependencies( resolveDependencies( model.getDependencies() ) );

        DependencyManagement mavenDependencyManagement = model.getDependencyManagement();
        if ( mavenDependencyManagement != null )
        {
            request.setManagedDependencies( resolveDependencies( mavenDependencyManagement.getDependencies() ) );
        }

        return resolveDependencies( buildingRequest, dependencyFilter, request );
    }

    /**
     * @param mavenDependencies {@link org.apache.maven.model.Dependency} can be {@code null}.
     * @return List of resolved dependencies.
     * @throws DependencyResolverException in case of a failure of the typeRegistry error.
     */
    private List<Dependency> resolveDependencies( Collection<org.apache.maven.model.Dependency> mavenDependencies )
    {
        if ( mavenDependencies == null )
        {
            return Collections.emptyList();
        }

        ArtifactTypeRegistry typeRegistry = createTypeRegistry();

        List<Dependency> aetherDependencies = new ArrayList<>( mavenDependencies.size() );

        for ( org.apache.maven.model.Dependency mavenDependency : mavenDependencies )
        {
            aetherDependencies.add( toDependency( mavenDependency, typeRegistry ) );
        }

        return aetherDependencies;
    }

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            ProjectBuildingRequest buildingRequest,
            Collection<org.apache.maven.model.Dependency> mavenDependencies,
            Collection<org.apache.maven.model.Dependency> managedMavenDependencies,
            TransformableFilter filter ) throws DependencyResolverException
    {
        List<Dependency> aetherDependencies = resolveDependencies( mavenDependencies );

        List<Dependency> aetherManagedDependencies = resolveDependencies( managedMavenDependencies );

        CollectRequest request = new CollectRequest( aetherDependencies, aetherManagedDependencies,
                RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ) );

        return resolveDependencies( buildingRequest, filter, request );
    }

    private Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            ProjectBuildingRequest buildingRequest,
            TransformableFilter dependencyFilter,
            CollectRequest request ) throws DependencyResolverException
    {
        try
        {
            DependencyFilter depFilter = null;
            if ( dependencyFilter != null )
            {
                depFilter = dependencyFilter.transform( new SonatypeAetherFilterTransformer() );
            }

            final List<ArtifactResult> dependencyResults = repositorySystem.resolveDependencies(
                    buildingRequest.getRepositorySession(), request, depFilter );

            // Keep it lazy! Often artifactsResults aren't used, so transforming up front is too expensive
            return new Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult>()
            {
                @Override
                public Iterator<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> iterator()
                {
                    // CHECKSTYLE_OFF: LineLength
                    Collection<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> artResults = new ArrayList<>(
                            dependencyResults.size() );
                    // CHECKSTYLE_ON: LineLength

                    for ( ArtifactResult artifactResult : dependencyResults )
                    {
                        artResults.add( new Maven30ArtifactResult( artifactResult ) );
                    }

                    return artResults.iterator();
                }
            };
        }
        catch ( ArtifactResolutionException e )
        {
            throw new Maven30DependencyResolverException( e );
        }
        catch ( DependencyCollectionException e )
        {
            throw new Maven30DependencyResolverException( e );
        }
    }
}
