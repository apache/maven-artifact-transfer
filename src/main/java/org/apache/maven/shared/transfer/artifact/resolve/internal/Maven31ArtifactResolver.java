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
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
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
@Component( role = ArtifactResolver.class, hint = "maven31" )
class Maven31ArtifactResolver
    implements ArtifactResolver
{
    @Requirement
    private RepositorySystem repositorySystem;

    @Requirement
    private ArtifactHandlerManager artifactHandlerManager;

    @Override
    // CHECKSTYLE_OFF: LineLength
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest,
                                                                                    org.apache.maven.artifact.Artifact mavenArtifact )
                                                                                        throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        Artifact aetherArtifact = (Artifact) Invoker.invoke( RepositoryUtils.class, "toArtifact",
                                                             org.apache.maven.artifact.Artifact.class, mavenArtifact );

        return resolveArtifact( buildingRequest, aetherArtifact );
    }

    @Override
    // CHECKSTYLE_OFF: LineLength
    public org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest,
                                                                                    ArtifactCoordinate coordinate )
                                                                                        throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        Artifact aetherArtifact =
            new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(),
                                 coordinate.getExtension(), coordinate.getVersion() );

        return resolveArtifact( buildingRequest, aetherArtifact );
    }

    // CHECKSTYLE_OFF: LineLength
    private org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest,
                                                                                     Artifact aetherArtifact )
                                                                                         throws ArtifactResolverException
    // CHECKSTYLE_ON: LineLength
    {
        @SuppressWarnings( "unchecked" )
        List<RemoteRepository> aetherRepositories =
            (List<RemoteRepository>) Invoker.invoke( RepositoryUtils.class, "toRepos", List.class,
                                                     buildingRequest.getRemoteRepositories() );

        RepositorySystemSession session =
            (RepositorySystemSession) Invoker.invoke( buildingRequest, "getRepositorySession" );

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
        catch ( ArtifactDescriptorException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArtifactResolverException( e.getMessage(), e );
        }
    }

}
