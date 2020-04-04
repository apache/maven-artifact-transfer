package org.apache.maven.shared.transfer.artifact.resolve.internal;

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

import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * 
 */
class Maven30ArtifactResolver
    implements MavenArtifactResolver
{
    private final RepositorySystem repositorySystem;

    private final List<RemoteRepository> aetherRepositories;
    
    private final RepositorySystemSession session;

    Maven30ArtifactResolver( RepositorySystem repositorySystem, List<RemoteRepository> aetherRepositories,
                                    RepositorySystemSession session )
    {
        this.repositorySystem = repositorySystem;
        this.aetherRepositories = aetherRepositories;
        this.session = session;
    }

    @Override
    // CHECKSTYLE_OFF: LineLength
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
                                                                                        throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        Artifact aetherArtifact = Invoker.invoke( RepositoryUtils.class, "toArtifact",
                                                             org.apache.maven.artifact.Artifact.class, mavenArtifact );

        return resolveArtifact( aetherArtifact );
    }

    @Override
    // CHECKSTYLE_OFF: LineLength
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( ArtifactCoordinate coordinate )
                                                                                        throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        Artifact aetherArtifact =
            new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(),
                                 coordinate.getExtension(), coordinate.getVersion() );

        return resolveArtifact( aetherArtifact );
    }

    // CHECKSTYLE_OFF: LineLength
    private org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( Artifact aetherArtifact )
                                                                                         throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        try
        {
            // use descriptor to respect relocation
            ArtifactDescriptorRequest descriptorRequest =
                new ArtifactDescriptorRequest( aetherArtifact, aetherRepositories, null );

            ArtifactDescriptorResult descriptorResult =
                repositorySystem.readArtifactDescriptor( session, descriptorRequest );

            ArtifactRequest request = new ArtifactRequest( descriptorResult.getArtifact(), aetherRepositories, null );

            return new Maven30ArtifactResult( repositorySystem.resolveArtifact( session, request ) );
        }
        catch ( ArtifactDescriptorException | ArtifactResolutionException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

}
