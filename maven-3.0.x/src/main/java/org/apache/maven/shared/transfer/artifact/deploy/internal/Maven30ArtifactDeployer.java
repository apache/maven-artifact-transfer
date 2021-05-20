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

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.metadata.internal.Maven30MetadataBridge;
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.SubArtifact;

import java.util.Collection;
import java.util.Objects;

/**
 *
 */
@Component( role = ArtifactDeployerDelegate.class, hint = Selector.MAVEN_3_0_X )
public class Maven30ArtifactDeployer
        extends DelegateSupport
        implements ArtifactDeployerDelegate
{
    @Requirement
    private RepositorySystem repositorySystem;

    public Maven30ArtifactDeployer()
    {
    }

    public Maven30ArtifactDeployer( RepositorySystem repositorySystem )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
    }

    @Override
    public void deploy( ProjectBuildingRequest buildingRequest,
                        Collection<org.apache.maven.artifact.Artifact> mavenArtifacts )
            throws ArtifactDeployerException
    {
        deploy( buildingRequest, null, mavenArtifacts );
    }

    @Override
    public void deploy( ProjectBuildingRequest buildingRequest,
                        ArtifactRepository remoteRepository,
                        Collection<org.apache.maven.artifact.Artifact> mavenArtifacts )
            throws ArtifactDeployerException
    {
        // prepare request
        DeployRequest request = new DeployRequest();

        RemoteRepository defaultRepository = null;

        if ( remoteRepository != null )
        {
            defaultRepository = getRemoteRepository( buildingRequest.getRepositorySession(), remoteRepository );
        }

        // transform artifacts
        for ( org.apache.maven.artifact.Artifact mavenArtifact : mavenArtifacts )
        {
            Artifact aetherArtifact = RepositoryUtils.toArtifact( mavenArtifact );
            request.addArtifact( aetherArtifact );

            RemoteRepository aetherRepository;
            if ( remoteRepository == null )
            {
                aetherRepository = getRemoteRepository( buildingRequest.getRepositorySession(),
                        mavenArtifact.getRepository() );
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
                    org.apache.maven.shared.transfer.metadata.ArtifactMetadata transferMedata =
                            (org.apache.maven.shared.transfer.metadata.ArtifactMetadata) metadata;

                    request.addMetadata( new Maven30MetadataBridge( metadata ).setFile( transferMedata.getFile() ) );
                }
            }
        }

        // deploy
        try
        {
            repositorySystem.deploy( buildingRequest.getRepositorySession(), request );
        }
        catch ( DeploymentException e )
        {
            throw new ArtifactDeployerException( e.getMessage(), e );
        }
    }

    private RemoteRepository getRemoteRepository( RepositorySystemSession session,
                                                  ArtifactRepository remoteRepository )
    {
        RemoteRepository aetherRepo = RepositoryUtils.toRepo( remoteRepository );

        if ( aetherRepo.getAuthentication() == null )
        {
            aetherRepo.setAuthentication( session.getAuthenticationSelector().getAuthentication( aetherRepo ) );
        }

        if ( aetherRepo.getProxy() == null )
        {
            aetherRepo.setProxy( session.getProxySelector().getProxy( aetherRepo ) );
        }

        return aetherRepo;
    }
}
