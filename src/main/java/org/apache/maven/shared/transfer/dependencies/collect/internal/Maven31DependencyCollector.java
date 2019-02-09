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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.collect.CollectorResult;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;

/**
 * Maven 3.1+ implementation of the {@link DependencyCollector}
 * 
 * @author Robert Scholte
 *
 */
class Maven31DependencyCollector
    implements MavenDependencyCollector
{
    private final RepositorySystem repositorySystem;

    private final ArtifactHandlerManager artifactHandlerManager;
    
    private final RepositorySystemSession session;
    
    private final List<RemoteRepository> aetherRepositories;
    
    Maven31DependencyCollector( RepositorySystem repositorySystem, ArtifactHandlerManager artifactHandlerManager,
                                RepositorySystemSession session, List<RemoteRepository> aetherRepositories )
    {
        super();
        this.repositorySystem = repositorySystem;
        this.artifactHandlerManager = artifactHandlerManager;
        this.session = session;
        this.aetherRepositories = aetherRepositories;
    }

    @Override
    public CollectorResult collectDependencies( org.apache.maven.model.Dependency root )
        throws DependencyCollectorException
    {
        ArtifactTypeRegistry typeRegistry =
                        (ArtifactTypeRegistry) Invoker.invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                                               ArtifactHandlerManager.class, artifactHandlerManager );

        CollectRequest request = new CollectRequest();
        request.setRoot( toDependency( root, typeRegistry ) );

        return collectDependencies( request );
    }

    @Override
    public CollectorResult collectDependencies( DependableCoordinate root )
        throws DependencyCollectorException
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
    public CollectorResult collectDependencies( Model root )
        throws DependencyCollectorException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( root.getPackaging() );
        
        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;
        
        Artifact aetherArtifact =
            new DefaultArtifact( root.getGroupId(), root.getArtifactId(), extension, root.getVersion() );
        
        CollectRequest request = new CollectRequest();
        request.setRoot( new Dependency( aetherArtifact, null ) );
        
        ArtifactTypeRegistry typeRegistry =
                        (ArtifactTypeRegistry) Invoker.invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
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

    private CollectorResult collectDependencies( CollectRequest request )
        throws DependencyCollectorException
    {
        request.setRepositories( aetherRepositories );

        try
        {
            return new Maven31CollectorResult( repositorySystem.collectDependencies( session, request ) );
        }
        catch ( DependencyCollectionException e )
        {
            throw new DependencyCollectorException( e.getMessage(), e );
        }
    }

    @Override
    public CollectorResult collectDependenciesGraph( ProjectBuildingRequest buildingRequest )
        throws DependencyCollectorException
    {
        DefaultRepositorySystemSession session = null;
        try
        {
            MavenProject project = buildingRequest.getProject();

            org.apache.maven.artifact.Artifact projectArtifact = project.getArtifact();
            List<ArtifactRepository> remoteArtifactRepositories = project.getRemoteArtifactRepositories();

            DefaultRepositorySystemSession repositorySession =
                (DefaultRepositorySystemSession) Invoker.invoke( buildingRequest, "getRepositorySession" );

            session = new DefaultRepositorySystemSession( repositorySession );

            DependencyGraphTransformer transformer =
                new ConflictResolver( new NearestVersionSelector(), new JavaScopeSelector(),
                                      new SimpleOptionalitySelector(), new JavaScopeDeriver() );
            session.setDependencyGraphTransformer( transformer );

            DependencySelector depFilter =
                new AndDependencySelector( new Maven31DirectScopeDependencySelector( JavaScopes.TEST ),
                                           new OptionalDependencySelector(), new ExclusionDependencySelector() );
            session.setDependencySelector( depFilter );

            session.setConfigProperty( ConflictResolver.CONFIG_PROP_VERBOSE, true );
            session.setConfigProperty( DependencyManagerUtils.CONFIG_PROP_VERBOSE, true );

            Artifact aetherArtifact =
                (Artifact) Invoker.invoke( RepositoryUtils.class, "toArtifact",
                                           org.apache.maven.artifact.Artifact.class, projectArtifact );

            @SuppressWarnings( "unchecked" )
            List<org.eclipse.aether.repository.RemoteRepository> aetherRepos =
                (List<org.eclipse.aether.repository.RemoteRepository>) Invoker.invoke( RepositoryUtils.class, "toRepos",
                                                                                       List.class,
                                                                                       remoteArtifactRepositories );

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot( new org.eclipse.aether.graph.Dependency( aetherArtifact, "" ) );
            collectRequest.setRepositories( aetherRepos );

            org.eclipse.aether.artifact.ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();
            collectDependencyList( collectRequest, project, stereotypes );
            collectManagedDependencyList( collectRequest, project, stereotypes );

            CollectResult collectResult = repositorySystem.collectDependencies( session, collectRequest );

            return new Maven31CollectorResult( collectResult );

//            org.eclipse.aether.graph.DependencyNode rootNode = collectResult.getRoot();

//            if ( getLogger().isDebugEnabled() )
//            {
//                logTree( rootNode );
//            }

//            return buildDependencyNode( null, rootNode, projectArtifact, filter );
        }
        catch ( DependencyCollectionException e )
        {
            throw new DependencyCollectorException( "Could not collect dependencies: " + e.getResult(), e );
        }
        finally
        {
            if ( session != null )
            {
                session.setReadOnly();
            }
        }
    }

    private void collectManagedDependencyList( CollectRequest collectRequest, MavenProject project,
                                               ArtifactTypeRegistry typeRegistry )
        throws DependencyCollectorException
    {
        if ( project.getDependencyManagement() != null )
        {
            for ( org.apache.maven.model.Dependency dependency : project.getDependencyManagement().getDependencies() )
            {
                Dependency aetherDep = toDependency( dependency, typeRegistry );
                collectRequest.addManagedDependency( aetherDep );
            }
        }
    }

    private void collectDependencyList( CollectRequest collectRequest, MavenProject project,
                                        org.eclipse.aether.artifact.ArtifactTypeRegistry typeRegistry )
        throws DependencyCollectorException
    {
        for ( org.apache.maven.model.Dependency dependency : project.getDependencies() )
        {
            Dependency aetherDep = toDependency( dependency, typeRegistry );
            collectRequest.addDependency( aetherDep );
        }
    }

    private static Dependency toDependency( org.apache.maven.model.Dependency root, ArtifactTypeRegistry typeRegistry )
                    throws DependencyCollectorException
    {
        Class<?>[] argClasses = new Class<?>[] { org.apache.maven.model.Dependency.class, ArtifactTypeRegistry.class };

        Object[] args = new Object[] { root, typeRegistry };

        return (Dependency) Invoker.invoke( RepositoryUtils.class, "toDependency", argClasses, args );
    }
}
