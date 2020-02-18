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
import org.apache.maven.shared.artifact.filter.resolve.transform.SonatypeAetherFilterTransformer;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;

/**
 * 
 */
class Maven30DependencyResolver
    implements MavenDependencyResolver
{
    private final RepositorySystem repositorySystem;

    private final ArtifactHandlerManager artifactHandlerManager;
    
    private final RepositorySystemSession session;
    
    private final List<RemoteRepository> aetherRepositories;
    
    Maven30DependencyResolver( RepositorySystem repositorySystem, ArtifactHandlerManager artifactHandlerManager,
                                      RepositorySystemSession session, List<RemoteRepository> aetherRepositories )
    {
        super();
        this.repositorySystem = repositorySystem;
        this.artifactHandlerManager = artifactHandlerManager;
        this.session = session;
        this.aetherRepositories = aetherRepositories;
    }

    @Override
    // CHECKSTYLE_OFF: LineLength
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies( DependableCoordinate coordinate,
                                                                                                  TransformableFilter dependencyFilter )
    // CHECKSTYLE_ON: LineLength
        throws DependencyResolverException
    {
        ArtifactTypeRegistry typeRegistry =
            (ArtifactTypeRegistry) Invoker.invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                                   ArtifactHandlerManager.class, artifactHandlerManager );

        Dependency aetherRoot = toDependency( coordinate, typeRegistry );

        CollectRequest request = new CollectRequest( aetherRoot, aetherRepositories );

        return resolveDependencies( dependencyFilter, request );
    }
    
    @Override
    // CHECKSTYLE_OFF: LineLength
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies( Model model,
                                                                                                  TransformableFilter dependencyFilter )
    // CHECKSTYLE_ON: LineLength
        throws DependencyResolverException
    {
        // Are there examples where packaging and type are NOT in sync
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( model.getPackaging() );
        
        String extension = artifactHandler != null ? artifactHandler.getExtension() : null;
        
        Artifact aetherArtifact =
            new DefaultArtifact( model.getGroupId(), model.getArtifactId(), extension, model.getVersion() );
        
        Dependency aetherRoot = new Dependency( aetherArtifact, null );
        
        CollectRequest request = new CollectRequest( aetherRoot, aetherRepositories );
        
        ArtifactTypeRegistry typeRegistry =
                        (ArtifactTypeRegistry) Invoker.invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                                               ArtifactHandlerManager.class, artifactHandlerManager );

        List<Dependency> aetherDependencies = new ArrayList<Dependency>( model.getDependencies().size() );
        for ( org.apache.maven.model.Dependency mavenDependency : model.getDependencies() )
        {
            aetherDependencies.add( toDependency( mavenDependency, typeRegistry ) );
        }
        request.setDependencies( aetherDependencies );

        DependencyManagement mavenDependencyManagement = model.getDependencyManagement();
        if ( mavenDependencyManagement != null )
        {
            List<Dependency> aetherManagerDependencies =
                new ArrayList<Dependency>( mavenDependencyManagement.getDependencies().size() );
            
            for ( org.apache.maven.model.Dependency mavenDependency : mavenDependencyManagement.getDependencies() )
            {
                aetherManagerDependencies.add( toDependency( mavenDependency, typeRegistry ) );
            }
            
            request.setManagedDependencies( aetherManagerDependencies );
        }

        return resolveDependencies( dependencyFilter, request );
    }

    @Override
    // CHECKSTYLE_OFF: LineLength
    public Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies( Collection<org.apache.maven.model.Dependency> mavenDependencies,
                                                                                                  Collection<org.apache.maven.model.Dependency> managedMavenDependencies,
                                                                                                  TransformableFilter filter )
    // CHECKSTYLE_ON: LineLength
        throws DependencyResolverException
    {
        ArtifactTypeRegistry typeRegistry =
            (ArtifactTypeRegistry) Invoker.invoke( RepositoryUtils.class, "newArtifactTypeRegistry",
                                                   ArtifactHandlerManager.class, artifactHandlerManager );

        final Class<?>[] argClasses =
            new Class<?>[] { org.apache.maven.model.Dependency.class, ArtifactTypeRegistry.class };

         List<Dependency> aetherDependencies = mavenDependencies != null
                                                 ? new ArrayList<Dependency>( mavenDependencies.size() )
                                                 : Collections.<Dependency>emptyList();

         if ( mavenDependencies != null )
         {
             for ( org.apache.maven.model.Dependency mavenDependency : mavenDependencies )
             {
                 Object[] args = new Object[] { mavenDependency, typeRegistry };

                 Dependency aetherDependency =
                     (Dependency) Invoker.invoke( RepositoryUtils.class, "toDependency", argClasses, args );

                 aetherDependencies.add( aetherDependency );
             }
         }

        List<Dependency> aetherManagedDependencies = null;

        if ( managedMavenDependencies != null )
        {
            aetherManagedDependencies = new ArrayList<Dependency>( managedMavenDependencies.size() );

            for ( org.apache.maven.model.Dependency mavenDependency : managedMavenDependencies )
            {
                Object[] args = new Object[] { mavenDependency, typeRegistry };

                Dependency aetherDependency =
                    (Dependency) Invoker.invoke( RepositoryUtils.class, "toDependency", argClasses, args );

                aetherManagedDependencies.add( aetherDependency );
            }
        }

        CollectRequest request =
            new CollectRequest( aetherDependencies, aetherManagedDependencies, aetherRepositories );

        return resolveDependencies( filter, request );
    }

    // CHECKSTYLE_OFF: LineLength
    private Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> resolveDependencies( TransformableFilter dependencyFilter,
                                                                                                   CollectRequest request )
                                                                                                       throws DependencyResolverException
    // CHECKSTYLE_ON :LineLength
    {
        try
        {
            DependencyFilter depFilter = null;
            if ( dependencyFilter != null )
            {
                depFilter = dependencyFilter.transform( new SonatypeAetherFilterTransformer() );
            }

            final List<ArtifactResult> dependencyResults =
                repositorySystem.resolveDependencies( session, request, depFilter );

            // Keep it lazy! Often artifactsResults aren't used, so transforming up front is too expensive
            return new Iterable<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult>()
            {
                @Override
                public Iterator<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> iterator()
                {
                    // CHECKSTYLE_OFF: LineLength
                    Collection<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult> artResults =
                        new ArrayList<org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult>( dependencyResults.size() );
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

    /**
     * Based on RepositoryUtils#toDependency(org.apache.maven.model.Dependency, ArtifactTypeRegistry)
     * 
     * @param coordinate
     * @param stereotypes
     * @return as Aether Dependency
     */
    private static Dependency toDependency( DependableCoordinate coordinate, ArtifactTypeRegistry stereotypes )
    {
        ArtifactType stereotype = stereotypes.get( coordinate.getType() );
        if ( stereotype == null )
        {
            stereotype = new DefaultArtifactType( coordinate.getType() );
        }

        Artifact artifact =
            new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(), null,
                                 coordinate.getVersion(), null, stereotype );

        return new Dependency( artifact, null );
    }
    
    private static Dependency toDependency( org.apache.maven.model.Dependency mavenDependency,
                                            ArtifactTypeRegistry typeRegistry )
        throws DependencyResolverException
    {
        Class<?>[] argClasses = new Class<?>[] { org.apache.maven.model.Dependency.class, ArtifactTypeRegistry.class };

        Object[] args = new Object[] { mavenDependency, typeRegistry };

        return (Dependency) Invoker.invoke( RepositoryUtils.class, "toDependency", argClasses, args );
    }  
}
