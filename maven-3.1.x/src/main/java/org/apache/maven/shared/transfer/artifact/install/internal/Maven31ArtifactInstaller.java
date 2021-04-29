package org.apache.maven.shared.transfer.artifact.install.internal;

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
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.metadata.internal.Maven31MetadataBridge;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.util.artifact.SubArtifact;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collection;
import java.util.Objects;

/**
 *
 */
@Singleton
@Named( Selector.MAVEN_3_1_X )
public class Maven31ArtifactInstaller
        extends DelegateSupport
        implements ArtifactInstallerDelegate
{
    private final RepositorySystem repositorySystem;

    private final RepositoryManager repositoryManager;

    @Inject
    public Maven31ArtifactInstaller( RepositorySystem repositorySystem,
                                     RepositoryManager repositoryManager )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
        this.repositoryManager = Objects.requireNonNull( repositoryManager );
    }

    @Override
    public void install( ProjectBuildingRequest request,
                         Collection<org.apache.maven.artifact.Artifact> mavenArtifacts )
            throws ArtifactInstallerException
    {
        install( request, null, mavenArtifacts );
    }

    @Override
    public void install( ProjectBuildingRequest buildingRequest,
                         File localRepository,
                         Collection<org.apache.maven.artifact.Artifact> mavenArtifacts )
            throws ArtifactInstallerException
    {
        ProjectBuildingRequest currentRequest = buildingRequest;
        if ( localRepository != null )
        {
            currentRequest = repositoryManager.setLocalRepositoryBasedir( buildingRequest, localRepository );
        }

        // prepare installRequest
        InstallRequest request = new InstallRequest();

        // transform artifacts
        for ( org.apache.maven.artifact.Artifact mavenArtifact : mavenArtifacts )
        {
            Artifact mainArtifact = RepositoryUtils.toArtifact( mavenArtifact );
            request.addArtifact( mainArtifact );

            for ( ArtifactMetadata metadata : mavenArtifact.getMetadataList() )
            {
                if ( metadata instanceof ProjectArtifactMetadata )
                {
                    Artifact pomArtifact = new SubArtifact( mainArtifact, "", "pom" );
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

        // install
        try
        {
            repositorySystem.install( currentRequest.getRepositorySession(), request );
        }
        catch ( InstallationException e )
        {
            throw new ArtifactInstallerException( e.getMessage(), e );
        }
    }
}
