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

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.collect.CollectorResult;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Singleton
@Named( Selector.MAVEN_3_1_X )
public class Maven31DependencyCollector
        extends DelegateSupport
        implements DependencyCollectorDelegate
{
    private final RepositorySystem repositorySystem;

    private final ArtifactHandlerManager artifactHandlerManager;

    @Inject
    public Maven31DependencyCollector( RepositorySystem repositorySystem,
                                       ArtifactHandlerManager artifactHandlerManager )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
        this.artifactHandlerManager = Objects.requireNonNull( artifactHandlerManager );
    }

    private static Dependency toDependency( org.apache.maven.model.Dependency mavenDependency,
                                            ArtifactTypeRegistry typeRegistry )
    {
        return RepositoryUtils.toDependency( mavenDependency, typeRegistry );
    }

    @Override
    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                org.apache.maven.model.Dependency root )
            throws DependencyCollectorException
    {
        ArtifactTypeRegistry typeRegistry = RepositoryUtils.newArtifactTypeRegistry( artifactHandlerManager );

        CollectRequest request = new CollectRequest();
        request.setRoot( toDependency( root, typeRegistry ) );

        return collectDependencies( buildingRequest, request );
    }

    @Override
    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                DependableCoordinate root )
            throws DependencyCollectorException
    {
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( root.getType() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact = new DefaultArtifact( root.getGroupId(), root.getArtifactId(), root.getClassifier(),
                extension, root.getVersion() );

        CollectRequest request = new CollectRequest();
        request.setRoot( new Dependency( aetherArtifact, null ) );

        return collectDependencies( buildingRequest, request );
    }

    @Override
    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                Model root )
            throws DependencyCollectorException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( root.getPackaging() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact = new DefaultArtifact( root.getGroupId(), root.getArtifactId(), extension,
                root.getVersion() );

        CollectRequest request = new CollectRequest();
        request.setRoot( new Dependency( aetherArtifact, null ) );

        ArtifactTypeRegistry typeRegistry = RepositoryUtils.newArtifactTypeRegistry( artifactHandlerManager );

        List<Dependency> aetherDependencies = new ArrayList<>( root.getDependencies().size() );
        for ( org.apache.maven.model.Dependency mavenDependency : root.getDependencies() )
        {
            aetherDependencies.add( toDependency( mavenDependency, typeRegistry ) );
        }
        request.setDependencies( aetherDependencies );

        if ( root.getDependencyManagement() != null )
        {
            List<Dependency> aetherManagerDependencies = new ArrayList<>(
                    root.getDependencyManagement().getDependencies().size() );

            for ( org.apache.maven.model.Dependency mavenDependency : root.getDependencyManagement().getDependencies() )
            {
                aetherManagerDependencies.add( toDependency( mavenDependency, typeRegistry ) );
            }

            request.setManagedDependencies( aetherManagerDependencies );
        }

        return collectDependencies( buildingRequest, request );
    }

    private CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                 CollectRequest request )
            throws DependencyCollectorException
    {
        request.setRepositories( RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ) );

        try
        {
            return new Maven31CollectorResult(
                    repositorySystem.collectDependencies( buildingRequest.getRepositorySession(), request )
            );
        }
        catch ( DependencyCollectionException e )
        {
            throw new DependencyCollectorException( e.getMessage(), e );
        }
    }
}
