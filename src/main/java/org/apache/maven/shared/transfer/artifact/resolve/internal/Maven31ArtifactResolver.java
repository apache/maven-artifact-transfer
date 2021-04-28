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
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.internal.ComponentSupport;
import org.apache.maven.shared.transfer.internal.Selector;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * 
 */
@Singleton
@Named(Selector.MAVEN_3_1)
public class Maven31ArtifactResolver
    extends ComponentSupport
    implements ArtifactResolverDelegate
{
    private final RepositorySystem repositorySystem;
    
    private final List<RemoteRepository> aetherRepositories;

    @Inject
    public Maven31ArtifactResolver( RepositorySystem repositorySystem,
                                    List<RemoteRepository> aetherRepositories )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
        this.aetherRepositories = Objects.requireNonNull( aetherRepositories );
    }

    @Override
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
            ProjectBuildingRequest buildingRequest,
            org.apache.maven.artifact.Artifact mavenArtifact ) throws ArtifactResolverException
    {
        Artifact aetherArtifact = RepositoryUtils.toArtifact(mavenArtifact);
        return resolveArtifact( buildingRequest.getRepositorySession(), aetherArtifact );
    }

    @Override
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
        ProjectBuildingRequest buildingRequest,
        ArtifactCoordinate coordinate ) throws ArtifactResolverException
    {
        Artifact aetherArtifact =
            new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(),
                                 coordinate.getExtension(), coordinate.getVersion() );

        return resolveArtifact( buildingRequest.getRepositorySession(), aetherArtifact );
    }

    private org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact(
        RepositorySystemSession session,
        Artifact aetherArtifact ) throws ArtifactResolverException
    {
        try
        {
            // use descriptor to respect relocation
            ArtifactDescriptorRequest descriptorRequest =
                new ArtifactDescriptorRequest( aetherArtifact, aetherRepositories, null );

            ArtifactDescriptorResult descriptorResult =
                repositorySystem.readArtifactDescriptor( session, descriptorRequest );

            ArtifactRequest request = new ArtifactRequest( descriptorResult.getArtifact(), aetherRepositories, null );

            return new Maven31ArtifactResult( repositorySystem.resolveArtifact( session, request ) );
        }
        catch ( ArtifactDescriptorException | ArtifactResolutionException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

}
