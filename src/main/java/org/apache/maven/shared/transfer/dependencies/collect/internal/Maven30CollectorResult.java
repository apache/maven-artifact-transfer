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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.apache.maven.shared.transfer.dependencies.collect.CollectorResult;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.VersionConstraint;

/**
 * CollectorResult wrapper around {@link CollectResult} 
 * 
 * @author Robert Scholte
 *
 */
class Maven30CollectorResult implements CollectorResult
{
    private final CollectResult collectResult;
    
    /**
     * @param collectResult {@link CollectorResult}
     */
    Maven30CollectorResult( CollectResult collectResult )
    {
        this.collectResult = collectResult;
    }

    @Override
    public List<ArtifactRepository> getRemoteRepositories()
    {
        final Set<RemoteRepository> aetherRepositories = new HashSet<RemoteRepository>();
        
        DependencyVisitor visitor = new DependencyVisitor()
        {
            @Override
            public boolean visitEnter( DependencyNode node )
            {
                aetherRepositories.addAll( node.getRepositories() );
                return true;
            }
            
            @Override
            public boolean visitLeave( DependencyNode node )
            {
                return true;
            }
        };
        
        collectResult.getRoot().accept( visitor );
        
        List<ArtifactRepository> mavenRepositories = new ArrayList<ArtifactRepository>( aetherRepositories.size() );
        
        for ( RemoteRepository aetherRepository : aetherRepositories )
        {
            mavenRepositories.add( new Maven30ArtifactRepositoryAdapter( aetherRepository ) );
        }
        
        return mavenRepositories;
    }

    @Override
    public org.apache.maven.shared.dependency.graph.DependencyNode getDependencyGraphRoot()
    {
        DependencyNode root = collectResult.getRoot();
        org.apache.maven.artifact.Artifact rootArtifact = getDependencyArtifact( root.getDependency() );

        return buildDependencyNode( null, root, rootArtifact, null );
    }

    // CHECKSTYLE_OFF: LineLength
    private org.apache.maven.shared.dependency.graph.DependencyNode buildDependencyNode( org.apache.maven.shared.dependency.graph.DependencyNode parent,
                                                                                         DependencyNode node,
                                                                                         org.apache.maven.artifact.Artifact artifact,
                                                                                         ArtifactFilter filter )
    // CHECKSTYLE_ON: LineLength
    {
        String premanagedVersion = node.getPremanagedVersion();
        String premanagedScope = node.getPremanagedScope();

        Boolean optional = null;
        if ( node.getDependency() != null )
        {
            optional = node.getDependency().isOptional();
        }

        DefaultDependencyNode current =
            new DefaultDependencyNode( parent, artifact, premanagedVersion, premanagedScope,
                                       getVersionSelectedFromRange( node.getVersionConstraint() ), optional );

        List<org.apache.maven.shared.dependency.graph.DependencyNode> nodes =
            new ArrayList<org.apache.maven.shared.dependency.graph.DependencyNode>( node.getChildren().size() );
        for ( DependencyNode child : node.getChildren() )
        {
            org.apache.maven.artifact.Artifact childArtifact = getDependencyArtifact( child.getDependency() );

            if ( ( filter == null ) || filter.include( childArtifact ) )
            {
                nodes.add( buildDependencyNode( current, child, childArtifact, filter ) );
            }
        }

        current.setChildren( Collections.unmodifiableList( nodes ) );

        return current;
    }

    private String getVersionSelectedFromRange( VersionConstraint constraint )
    {
        if ( ( constraint == null ) || ( constraint.getVersion() != null ) || ( constraint.getRanges().isEmpty() ) )
        {
            return null;
        }

        return constraint.getRanges().iterator().next().toString();
    }

    private org.apache.maven.artifact.Artifact getDependencyArtifact( Dependency dep )
    {
        Artifact artifact = dep.getArtifact();

        try
        {
            org.apache.maven.artifact.Artifact mavenArtifact =
                (org.apache.maven.artifact.Artifact) Invoker.invoke( RepositoryUtils.class, "toArtifact",
                                                                     Artifact.class, artifact );

            mavenArtifact.setScope( dep.getScope() );
            mavenArtifact.setOptional( dep.isOptional() );

            return mavenArtifact;
        }
        catch ( DependencyCollectorException e )
        {
            // ReflectionException should not happen
            throw new RuntimeException( e.getMessage(), e );
        }
    }
}
