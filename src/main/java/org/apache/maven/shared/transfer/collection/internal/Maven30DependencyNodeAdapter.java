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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;
import org.apache.maven.shared.transfer.graph.DependencyNode;
import org.apache.maven.shared.transfer.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * DependencyCollectorNode wrapper around {@link org.sonatype.aether.graph.DependencyNode}
 * 
 * @author Pim Moerenhout
 *
 */
class Maven30DependencyNodeAdapter implements DependencyNode
{

    private org.sonatype.aether.graph.DependencyNode dependencyNode;

    /**
     * @param dependencyNode {@link org.sonatype.aether.graph.DependencyNode}
     */
    Maven30DependencyNodeAdapter( org.sonatype.aether.graph.DependencyNode dependencyNode )
    {
        this.dependencyNode = dependencyNode;
    }

    @Override
    public Artifact getArtifact()
    {
        return getArtifact( dependencyNode.getDependency().getArtifact() );
    }

    @Override
    public List<DependencyNode> getChildren()
    {
        List<org.sonatype.aether.graph.DependencyNode> aetherChildren = dependencyNode.getChildren();
        List<DependencyNode> children = new ArrayList<>( aetherChildren.size() );
        for ( org.sonatype.aether.graph.DependencyNode aetherChild : aetherChildren )
        {
            children.add( new Maven30DependencyNodeAdapter( aetherChild ) );
        }
        return children;
    }

    @Override
    public List<ArtifactRepository> getRemoteRepositories()
    {
        List<RemoteRepository> aetherRepositories = dependencyNode.getRepositories();
        List<ArtifactRepository> mavenRepositories = new ArrayList<ArtifactRepository>( aetherRepositories.size() );

        for ( RemoteRepository aetherRepository : aetherRepositories )
        {
            mavenRepositories.add( new Maven30ArtifactRepositoryAdapter( aetherRepository ) );
        }

        return mavenRepositories;
    }

    @Override
    public Boolean getOptional()
    {
        return dependencyNode.getDependency().isOptional();
    }

    @Override
    public String getScope()
    {
        return dependencyNode.getDependency().getScope();
    }

    @Override
    public boolean accept( DependencyVisitor visitor )
    {
        if ( visitor.visitEnter( this ) )
        {
            for ( org.sonatype.aether.graph.DependencyNode aetherNode : dependencyNode.getChildren() )
            {
                DependencyNode child = new Maven30DependencyNodeAdapter( aetherNode );
                if ( !child.accept( visitor ) )
                {
                    break;
                }
            }
        }

        return visitor.visitLeave( this );
    }

    @Override
    public int hashCode()
    {
        return dependencyNode.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }

        Maven30DependencyNodeAdapter other = (Maven30DependencyNodeAdapter) obj;
        if ( dependencyNode == null )
        {
            if ( other.dependencyNode != null )
            {
                return false;
            }
        }
        else if ( !dependencyNode.equals( other.dependencyNode ) )
        {
            return false;
        }
        return true;
    }

    private Artifact getArtifact( org.sonatype.aether.artifact.Artifact aetherArtifact )
    {
        try
        {
            return Invoker.invoke( RepositoryUtils.class, "toArtifact",
                org.sonatype.aether.artifact.Artifact.class, aetherArtifact );
        }
        catch ( DependencyCollectionException e )
        {
            throw new RuntimeException( e );
        }
    }
}
