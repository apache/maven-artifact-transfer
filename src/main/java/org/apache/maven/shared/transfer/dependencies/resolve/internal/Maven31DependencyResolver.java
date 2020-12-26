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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.EclipseAetherFilterTransformer;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

/**
 *
 */
class Maven31DependencyResolver implements MavenDependencyResolver
{
    private static final Class<?>[] ARG_CLASSES = new Class<?>[] {org.apache.maven.model.Dependency.class,
            ArtifactTypeRegistry.class};
    private final RepositorySystem repositorySystem;
    private final ArtifactHandlerManager artifactHandlerManager;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> aetherRepositories;

    Maven31DependencyResolver( RepositorySystem repositorySystem, ArtifactHandlerManager artifactHandlerManager,
            RepositorySystemSession session, List<RemoteRepository> aetherRepositories )
    {
        super();
        this.repositorySystem = repositorySystem;
        this.artifactHandlerManager = artifactHandlerManager;
        this.session = session;
        this.aetherRepositories = aetherRepositories;
    }

    /**
     * Based on RepositoryUtils#toDependency(org.apache.maven.model.Dependency, ArtifactTypeRegistry)
     *
     * @param coordinate  {@link DependableCoordinate}
     * @param stereotypes {@link ArtifactTypeRegistry
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

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            DependableCoordinate coordinate, TransformableFilter dependencyFilter ) throws DependencyResolverException
    {
        ArtifactTypeRegistry typeRegistry = RepositoryUtils.newArtifactTypeRegistry( artifactHandlerManager );

        Dependency aetherRoot = toDependency( coordinate, typeRegistry );

        CollectRequest request = new CollectRequest( aetherRoot, aetherRepositories );

        return resolveDependencies( dependencyFilter, request );
    }

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies( Model model,
            TransformableFilter dependencyFilter ) throws DependencyResolverException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( model.getPackaging() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact = new DefaultArtifact( model.getGroupId(), model.getArtifactId(), extension,
                model.getVersion() );

        Dependency aetherRoot = new Dependency( aetherArtifact, null );

        CollectRequest request = new CollectRequest( aetherRoot, aetherRepositories );

        request.setDependencies( resolveDependencies( model.getDependencies() ) );

        DependencyManagement mavenDependencyManagement = model.getDependencyManagement();
        if ( mavenDependencyManagement != null )
        {
            request.setManagedDependencies( resolveDependencies( model.getDependencyManagement().getDependencies() ) );
        }

        return resolveDependencies( dependencyFilter, request );
    }

    /**
     * @param mavenDependencies {@link org.apache.maven.model.Dependency} can be {@code null}.
     * @return List of resolved dependencies.
     */
    private List<Dependency> resolveDependencies( Collection<org.apache.maven.model.Dependency> mavenDependencies )
    {
        if ( mavenDependencies == null )
        {
            return Collections.emptyList();
        }

        ArtifactTypeRegistry typeRegistry = RepositoryUtils.newArtifactTypeRegistry( artifactHandlerManager );

        List<Dependency> aetherDependencies = new ArrayList<>( mavenDependencies.size() );

        for ( org.apache.maven.model.Dependency mavenDependency : mavenDependencies )
        {
            aetherDependencies.add( RepositoryUtils.toDependency( mavenDependency, typeRegistry ) );
        }

        return aetherDependencies;
    }

    @Override
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            Collection<org.apache.maven.model.Dependency> mavenDependencies,
            Collection<org.apache.maven.model.Dependency> managedMavenDependencies, TransformableFilter filter )
            throws DependencyResolverException
    {

        List<Dependency> aetherDeps = resolveDependencies( mavenDependencies );

        List<Dependency> aetherManagedDependencies = resolveDependencies( managedMavenDependencies );

        CollectRequest request = new CollectRequest( aetherDeps, aetherManagedDependencies, aetherRepositories );

        return resolveDependencies( filter, request );
    }

    private Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies(
            TransformableFilter dependencyFilter, CollectRequest request ) throws DependencyResolverException
    {
        try
        {
            DependencyFilter depFilter = null;
            if ( dependencyFilter != null )
            {
                depFilter = dependencyFilter.transform( new EclipseAetherFilterTransformer() );
            }

            DependencyRequest depRequest = new DependencyRequest( request, depFilter );

            final DependencyResult dependencyResults = repositorySystem.resolveDependencies( session, depRequest );

            // Keep it lazy! Often artifactsResults aren't used, so transforming up front is too expensive
            return new Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult>()
            {
                @Override
                public Iterator<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> iterator()
                {
                    // CHECKSTYLE_OFF: LineLength
                    Collection<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> artResults = new ArrayList<>(
                            dependencyResults.getArtifactResults().size() );
                    // CHECKSTYLE_ON: LineLength

                    for ( ArtifactResult artifactResult : dependencyResults.getArtifactResults() )
                    {
                        artResults.add( new Maven31ArtifactResult( artifactResult ) );
                    }

                    return artResults.iterator();
                }
            };
        }
        catch ( DependencyResolutionException e )
        {
            throw new Maven31DependencyResolverException( e );
        }
    }
}
