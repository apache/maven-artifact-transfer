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

import java.io.File;
import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.internal.ComponentSupport;
import org.apache.maven.shared.transfer.internal.Selector;
import org.apache.maven.shared.transfer.metadata.internal.Maven31MetadataBridge;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.util.artifact.SubArtifact;

/**
 * 
 */
@Singleton
@Named(Selector.MAVEN_3_1)
public class Maven31ArtifactInstaller
    extends ComponentSupport
    implements ArtifactInstallerDelegate
{
    private final RepositorySystem repositorySystem;

    @Inject
    public Maven31ArtifactInstaller( RepositorySystem repositorySystem )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
    }


    @Override
    public void install(ProjectBuildingRequest buildingRequest,
                        Collection<org.apache.maven.artifact.Artifact> mavenArtifacts )
        throws ArtifactInstallerException
    {
        // prepare installRequest
        InstallRequest request = new InstallRequest();

        // transform artifacts
        for ( org.apache.maven.artifact.Artifact mavenArtifact : mavenArtifacts )
        {
            Artifact mainArtifact = RepositoryUtils.toArtifact(mavenArtifact);
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
            repositorySystem.install( buildingRequest.getRepositorySession(), request );
        }
        catch ( InstallationException e )
        {
            throw new ArtifactInstallerException( e.getMessage(), e );
        }
    }

    @Override
    public void install(final ProjectBuildingRequest request,
                        final File localRepository,
                        final Collection<org.apache.maven.artifact.Artifact> mavenArtifacts)
        throws ArtifactInstallerException
    {
        throw new UnsupportedOperationException("how did we get here?");
    }
}
