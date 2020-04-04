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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Model;
import org.apache.maven.shared.transfer.collection.CollectResult;
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;
import org.apache.maven.shared.transfer.collection.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Maven 3.0 implementation of the {@link DependencyCollector}
 * 
 * @author Robert Scholte
 *
 */
class Maven30DependencyCollector
    implements MavenDependencyCollector
{
    private final RepositorySystem repositorySystem;

    private final ArtifactHandlerManager artifactHandlerManager;

    private final RepositorySystemSession session;
    
    private final List<RemoteRepository> aetherRepositories;
    
    Maven30DependencyCollector( RepositorySystem repositorySystem, ArtifactHandlerManager artifactHandlerManager,
                                RepositorySystemSession session, List<RemoteRepository> aetherRepositories )
    {
        super();
        this.repositorySystem = repositorySystem;
        this.artifactHandlerManager = artifactHandlerManager;
        this.session = session;
        this.aetherRepositories = aetherRepositories;
    }

    @Override
    public CollectResult collectDependencies( org.apache.maven.model.Dependency root )
        throws DependencyCollectionException
    {
        ArtifactTypeRegistry typeRegistry = Invoker
            .invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                               ArtifactHandlerManager.class, artifactHandlerManager );

        CollectRequest request = new CollectRequest();
        request.setRoot( toDependency( root, typeRegistry ) );

        return collectDependencies( request );
    }

    @Override
    public CollectResult collectDependencies( DependableCoordinate root )
        throws DependencyCollectionException
    {
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( root.getType() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact = new DefaultArtifact( root.getGroupId(), root.getArtifactId(), root.getClassifier(),
                                                       extension, root.getVersion() );

        CollectRequest request = new CollectRequest();
        request.setRoot( new Dependency( aetherArtifact, null ) );

        return collectDependencies( request );
    }

    @Override
    public CollectResult collectDependencies( Model root )
        throws DependencyCollectionException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( root.getPackaging() );

        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;

        Artifact aetherArtifact =
            new DefaultArtifact( root.getGroupId(), root.getArtifactId(), extension, root.getVersion() );

        CollectRequest request = new CollectRequest();
        request.setRoot( new Dependency( aetherArtifact, null ) );

        ArtifactTypeRegistry typeRegistry = Invoker
            .invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                               ArtifactHandlerManager.class, artifactHandlerManager );

        List<Dependency> aetherDependencies = new ArrayList<Dependency>( root.getDependencies().size() );
        for ( org.apache.maven.model.Dependency mavenDependency : root.getDependencies() )
        {
            aetherDependencies.add( toDependency( mavenDependency, typeRegistry ) );
        }
        request.setDependencies( aetherDependencies );

        if ( root.getDependencyManagement() != null )
        {
            List<Dependency> aetherManagerDependencies =
                new ArrayList<Dependency>( root.getDependencyManagement().getDependencies().size() );

            for ( org.apache.maven.model.Dependency mavenDependency : root.getDependencyManagement().getDependencies() )
            {
                aetherManagerDependencies.add( toDependency( mavenDependency, typeRegistry ) );
            }

            request.setManagedDependencies( aetherManagerDependencies );
        }

        return collectDependencies( request );
    }

    private CollectResult collectDependencies( CollectRequest request )
        throws DependencyCollectionException
    {
        request.setRepositories( aetherRepositories );

        try
        {
            return new Maven30CollectResult( repositorySystem.collectDependencies( session, request ) );
        }
        catch ( org.sonatype.aether.collection.DependencyCollectionException e )
        {
            throw new DependencyCollectionException( e.getMessage(), e );
        }
    }

    private static Dependency toDependency( org.apache.maven.model.Dependency mavenDependency,
                                            ArtifactTypeRegistry typeRegistry )
        throws DependencyCollectionException
    {
        Class<?>[] argClasses = new Class<?>[] { org.apache.maven.model.Dependency.class, ArtifactTypeRegistry.class };

        Object[] args = new Object[] { mavenDependency, typeRegistry };

        return Invoker.invoke( RepositoryUtils.class, "toDependency", argClasses, args );
    }
}
