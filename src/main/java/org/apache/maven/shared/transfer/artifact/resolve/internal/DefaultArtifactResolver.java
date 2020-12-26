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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.RepositorySystem;

/**
 *
 */
@Component( role = ArtifactResolver.class, hint = "default" )
class DefaultArtifactResolver implements ArtifactResolver
{
    @Requirement
    RepositorySystem repositorySystem;

    @Override
    public ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
            throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, mavenArtifact );
        return getMavenArtifactResolver( buildingRequest ).resolveArtifact( mavenArtifact );
    }

    @Override
    public ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
            throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, coordinate );
        return getMavenArtifactResolver( buildingRequest ).resolveArtifact( coordinate );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( mavenArtifact == null )
        {
            throw new IllegalArgumentException( "The parameter mavenArtifact is not allowed to be null." );
        }
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( coordinate == null )
        {
            throw new IllegalArgumentException( "The parameter coordinate is not allowed to be null." );
        }
    }

    private MavenArtifactResolver getMavenArtifactResolver( ProjectBuildingRequest buildingRequest )
    {
        return new Maven31ArtifactResolver( repositorySystem,
                RepositoryUtils.toRepos( buildingRequest.getRemoteRepositories() ),
                buildingRequest.getRepositorySession() );
    }
}
