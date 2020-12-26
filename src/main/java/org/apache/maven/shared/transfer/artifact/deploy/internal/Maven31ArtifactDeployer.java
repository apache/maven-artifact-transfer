package org.apache.maven.shared.transfer.artifact.deploy.internal;

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

import java.util.Collection;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.metadata.internal.Maven31MetadataBridge;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.SubArtifact;

/**
 *
 */
class Maven31ArtifactDeployer implements MavenArtifactDeployer
{
    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession session;

    Maven31ArtifactDeployer( RepositorySystem repositorySystem, RepositorySystemSession session )
    {
        super();
        this.repositorySystem = repositorySystem;
        this.session = session;
    }

    @Override
    public void deploy( Collection<org.apache.maven.artifact.Artifact> mavenArtifacts ) throws ArtifactDeployerException
    {
        deploy( null, mavenArtifacts );
    }

    @Override
    public void deploy( ArtifactRepository remoteRepository,
            Collection<org.apache.maven.artifact.Artifact> mavenArtifacts ) throws ArtifactDeployerException
    {
        // prepare request
        DeployRequest request = new DeployRequest();

        RemoteRepository defaultRepository = null;

        if ( remoteRepository != null )
        {
            defaultRepository = getRemoteRepository( session, remoteRepository );
        }

        // transform artifacts
        for ( org.apache.maven.artifact.Artifact mavenArtifact : mavenArtifacts )
        {
            Artifact aetherArtifact = RepositoryUtils.toArtifact( mavenArtifact );
            request.addArtifact( aetherArtifact );

            RemoteRepository aetherRepository;
            if ( remoteRepository == null )
            {
                aetherRepository = getRemoteRepository( session, mavenArtifact.getRepository() );
            }
            else
            {
                aetherRepository = defaultRepository;
            }

            request.setRepository( aetherRepository );

            for ( ArtifactMetadata metadata : mavenArtifact.getMetadataList() )
            {
                if ( metadata instanceof ProjectArtifactMetadata )
                {
                    Artifact pomArtifact = new SubArtifact( aetherArtifact, "", "pom" );
                    pomArtifact = pomArtifact.setFile( ( (ProjectArtifactMetadata) metadata ).getFile() );
                    request.addArtifact( pomArtifact );
                }
                else if ( // metadata instanceof SnapshotArtifactRepositoryMetadata ||
                        metadata instanceof ArtifactRepositoryMetadata )
                {
                    // eaten, handled by repo system
                }
                else if ( metadata instanceof org.apache.maven.shared.transfer.metadata.ArtifactMetadata )
                {
                    org.apache.maven.shared.transfer.metadata.ArtifactMetadata transferMetadata =
                            (org.apache.maven.shared.transfer.metadata.ArtifactMetadata) metadata;

                    request.addMetadata( new Maven31MetadataBridge( metadata ).setFile( transferMetadata.getFile() ) );
                }
            }
        }

        // deploy
        try
        {
            repositorySystem.deploy( session, request );
        }
        catch ( DeploymentException e )
        {
            throw new ArtifactDeployerException( e.getMessage(), e );
        }
    }

    private RemoteRepository getRemoteRepository( RepositorySystemSession session, ArtifactRepository remoteRepository )
            throws ArtifactDeployerException
    {
        RemoteRepository aetherRepo = RepositoryUtils.toRepo( remoteRepository );

        if ( aetherRepo.getAuthentication() == null || aetherRepo.getProxy() == null )
        {
            RemoteRepository.Builder builder = new RemoteRepository.Builder( aetherRepo );

            if ( aetherRepo.getAuthentication() == null )
            {
                builder.setAuthentication( session.getAuthenticationSelector().getAuthentication( aetherRepo ) );
            }

            if ( aetherRepo.getProxy() == null )
            {
                builder.setProxy( session.getProxySelector().getProxy( aetherRepo ) );
            }

            aetherRepo = builder.build();
        }

        return aetherRepo;
    }
}
