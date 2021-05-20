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

import org.apache.maven.RepositoryUtils;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.util.Objects;

/**
 *
 */
@Component( role = ArtifactResolverDelegate.class, hint = Selector.MAVEN_3_0_X )
public class Maven30ArtifactResolver
        extends DelegateSupport
        implements ArtifactResolverDelegate
{
    @Requirement
    private RepositorySystem repositorySystem;

    public Maven30ArtifactResolver()
    {
    }

    public Maven30ArtifactResolver( RepositorySystem repositorySystem )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
    }

    @Override
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
            ProjectBuildingRequest buildingRequest,
            org.apache.maven.artifact.Artifact mavenArtifact ) throws ArtifactResolverException
    {
        Artifact aetherArtifact = RepositoryUtils.toArtifact( mavenArtifact );

        return resolveArtifact( buildingRequest, aetherArtifact );
    }

    @Override
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
            ProjectBuildingRequest buildingRequest,
            ArtifactCoordinate coordinate ) throws ArtifactResolverException
    {
        Artifact aetherArtifact = new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(),
                coordinate.getClassifier(), coordinate.getExtension(), coordinate.getVersion() );

        return resolveArtifact( buildingRequest, aetherArtifact );
    }

    private org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
            ProjectBuildingRequest buildingRequest,
            Artifact aetherArtifact ) throws ArtifactResolverException
    {
        try
        {
            // use descriptor to respect relocation
            ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest( aetherArtifact,
                    RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ),
                    null );

            ArtifactDescriptorResult descriptorResult = repositorySystem.readArtifactDescriptor(
                    buildingRequest.getRepositorySession(),
                    descriptorRequest );

            ArtifactRequest request = new ArtifactRequest( descriptorResult.getArtifact(),
                    RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ),
                    null );

            return new Maven30ArtifactResult( repositorySystem.resolveArtifact(
                    buildingRequest.getRepositorySession(), request ) );
        }
        catch ( ArtifactDescriptorException | ArtifactResolutionException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

}
