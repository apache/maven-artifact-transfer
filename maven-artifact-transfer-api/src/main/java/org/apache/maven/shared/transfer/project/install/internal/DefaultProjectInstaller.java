package org.apache.maven.shared.transfer.project.install.internal;

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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstaller;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.install.ProjectInstaller;
import org.apache.maven.shared.transfer.project.install.ProjectInstallerRequest;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will install a whole project into the appropriate repository.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
@Component( role = ProjectInstaller.class )
class DefaultProjectInstaller
    implements ProjectInstaller
{

    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultProjectInstaller.class );

    @Requirement
    private ArtifactInstaller installer;

    @Requirement
    private RepositoryManager repositoryManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void install( ProjectBuildingRequest buildingRequest, ProjectInstallerRequest installerRequest )
        throws IOException, ArtifactInstallerException, NoFileAssignedException, IllegalArgumentException
    {

        validateParameters( buildingRequest, installerRequest );
        MavenProject project = installerRequest.getProject();

        Artifact artifact = project.getArtifact();
        String packaging = project.getPackaging();
        File pomFile = project.getFile();

        List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

        // TODO: push into transformation
        boolean isPomArtifact = "pom".equals( packaging );

        ProjectArtifactMetadata metadata;

        Collection<File> metadataFiles = new LinkedHashSet<>();

        if ( isPomArtifact )
        {
            if ( pomFile != null )
            {
                installer.install( buildingRequest,
                                   Collections.<Artifact>singletonList( new ProjectArtifact( project ) ) );
                addMetaDataFilesForArtifact( buildingRequest, artifact, metadataFiles );
            }
        }
        else
        {
            if ( pomFile != null )
            {
                metadata = new ProjectArtifactMetadata( artifact, pomFile );
                artifact.addMetadata( metadata );
            }

            File file = artifact.getFile();

            // Here, we have a temporary solution to MINSTALL-3 (isDirectory() is true if it went through compile
            // but not package). We are designing in a proper solution for Maven 2.1
            if ( file != null && file.isFile() )
            {
                installer.install( buildingRequest, Collections.singletonList( artifact ) );
                addMetaDataFilesForArtifact( buildingRequest, artifact, metadataFiles );
            }
            else if ( !attachedArtifacts.isEmpty() )
            {
                throw new NoFileAssignedException( "The packaging plugin for this project did not assign "
                    + "a main file to the project but it has attachments. Change packaging to 'pom'." );
            }
            else
            {
                // CHECKSTYLE_OFF: LineLength
                throw new NoFileAssignedException( "The packaging for this project did not assign a file to the build artifact" );
                // CHECKSTYLE_ON: LineLength
            }
        }

        for ( Artifact attached : attachedArtifacts )
        {
            LOGGER.debug( "Installing artifact: {}", attached.getId() );
            installer.install( buildingRequest, Collections.singletonList( attached ) );
            addMetaDataFilesForArtifact( buildingRequest, attached, metadataFiles );
        }

    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, ProjectInstallerRequest installerRequest )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( installerRequest == null )
        {
            throw new IllegalArgumentException( "The parameter installerRequest is not allowed to be null." );
        }
    }

    private void addMetaDataFilesForArtifact( ProjectBuildingRequest buildingRequest, Artifact artifact,
            Collection<File> targetMetadataFiles )
    {
        Collection<ArtifactMetadata> metadatas = artifact.getMetadataList();
        if ( metadatas != null )
        {
            for ( ArtifactMetadata metadata : metadatas )
            {
                File metadataFile = getLocalRepoFile( buildingRequest, metadata );
                targetMetadataFiles.add( metadataFile );
            }
        }
    }

    /**
     * Gets the path of the specified artifact metadata within the local repository. Note that the returned path need
     * not exist (yet).
     *
     * @param buildingRequest The project building request, must not be <code>null</code>.
     * @param metadata The artifact metadata whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact metadata when installed, never <code>null</code>.
     */
    private File getLocalRepoFile( ProjectBuildingRequest buildingRequest, ArtifactMetadata metadata )
    {
        String path = repositoryManager.getPathForLocalMetadata( buildingRequest, metadata );
        return new File( repositoryManager.getLocalRepositoryBasedir( buildingRequest ), path );
    }

}
