package org.apache.maven.shared.transfer.project.deploy.internal;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployer;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will deploy a whole project into the appropriate remote repository.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a> Most of the code is
 *         taken from maven-deploy-plugin.
 */
@Component( role = ProjectDeployer.class )
class DefaultProjectDeployer
    implements ProjectDeployer
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultProjectDeployer.class );

    @Requirement
    private ArtifactDeployer deployer;

    @Requirement
    private RepositoryManager repositoryManager;

    private final DualDigester digester = new DualDigester();

    /**
     * {@inheritDoc}
     */
    public void deploy( ProjectBuildingRequest buildingRequest, ProjectDeployerRequest projectDeployerRequest,
                        ArtifactRepository artifactRepository )
        throws NoFileAssignedException, IllegalArgumentException, ArtifactDeployerException
    {
        validateParameters( buildingRequest, projectDeployerRequest, artifactRepository );

        Artifact artifact = projectDeployerRequest.getProject().getArtifact();
        String packaging = projectDeployerRequest.getProject().getPackaging();
        File pomFile = projectDeployerRequest.getProject().getFile();

        List<Artifact> attachedArtifacts = projectDeployerRequest.getProject().getAttachedArtifacts();

        // Deploy the POM
        boolean isPomArtifact = "pom".equals( packaging );
        if ( isPomArtifact )
        {
            artifact.setFile( pomFile );
        }
        else
        {
            ProjectArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, pomFile );
            artifact.addMetadata( metadata );
        }

        // What consequence does this have?
        // artifact.setRelease( true );

        artifact.setRepository( artifactRepository );

        int retryFailedDeploymentCount = projectDeployerRequest.getRetryFailedDeploymentCount();

        List<Artifact> deployableArtifacts = new ArrayList<Artifact>();
        if ( isPomArtifact )
        {
            deployableArtifacts.add( artifact );
        }
        else
        {
            File file = artifact.getFile();

            if ( file != null && file.isFile() )
            {
                deployableArtifacts.add( artifact );
                // installChecksums( buildingRequest, artifact, createChecksum );
            }
            else if ( !attachedArtifacts.isEmpty() )
            {
                // TODO: Reconsider this exception? Better Exception type?
                throw new NoFileAssignedException( "The packaging plugin for this project did not assign "
                    + "a main file to the project but it has attachments. Change packaging to 'pom'." );
            }
            else
            {
                // TODO: Reconsider this exception? Better Exception type?
                throw new NoFileAssignedException( "The packaging for this project did not assign "
                    + "a file to the build artifact" );
            }
        }

        for ( Artifact attached : attachedArtifacts )
        {
            // installChecksums( buildingRequest, artifact, createChecksum );
            deployableArtifacts.add( attached );
        }

        installChecksumsForAllArtifacts( buildingRequest, deployableArtifacts );
        deploy( buildingRequest, deployableArtifacts, artifactRepository, retryFailedDeploymentCount );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest,
                                     ProjectDeployerRequest projectDeployerRequest,
                                     ArtifactRepository artifactRepository )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( projectDeployerRequest == null )
        {
            throw new IllegalArgumentException( "The parameter projectDeployerRequest is not allowed to be null." );
        }
        if ( artifactRepository == null )
        {
            throw new IllegalArgumentException( "The parameter artifactRepository is not allowed to be null." );
        }
    }

    private void installChecksumsForAllArtifacts( ProjectBuildingRequest request, Collection<Artifact> artifacts )
    {
        for ( Artifact item : artifacts )
        {
            try
            {
                LOGGER.debug( "Installing checksum for " + item.getId() );
                installChecksums( request, item );
            }
            catch ( IOException e )
            {
                // THINK HARD ABOUT IT
                LOGGER.error( "Failure during checksum generation for " + item.getId() );
            }
        }
    }

    private void deploy( ProjectBuildingRequest request, Collection<Artifact> artifacts,
                         ArtifactRepository deploymentRepository, int retryFailedDeploymentCount )
        throws ArtifactDeployerException
    {

        // for now retry means redeploy the complete artifacts collection
        int retryFailedDeploymentCounter = Math.max( 1, Math.min( 10, retryFailedDeploymentCount ) );
        ArtifactDeployerException exception = null;
        for ( int count = 0; count < retryFailedDeploymentCounter; count++ )
        {
            try
            {
                if ( count > 0 )
                {
                    LOGGER.info( "Retrying deployment attempt " + ( count + 1 ) + " of "
                        + retryFailedDeploymentCounter );
                }

                deployer.deploy( request, deploymentRepository, artifacts );
                exception = null;
                break;
            }
            catch ( ArtifactDeployerException e )
            {
                if ( count + 1 < retryFailedDeploymentCounter )
                {
                    LOGGER.warn( "Encountered issue during deployment: " + e.getLocalizedMessage() );
                    LOGGER.debug( e.getMessage() );
                }
                if ( exception == null )
                {
                    exception = e;
                }
            }
        }
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * @param buildingRequest The project building request, must not be <code>null</code>.
     * @param artifact The artifact for which to create checksums, must not be <code>null</code>.
     * @param createChecksum {@code true} if checksum should be created, otherwise {@code false}.
     * @throws IOException If the checksums could not be installed.
     */
    private void installChecksums( ProjectBuildingRequest buildingRequest, Artifact artifact )
        throws IOException
    {
        File artifactFile = getLocalRepoFile( buildingRequest, artifact );
        installChecksums( artifactFile );
    }

    /**
     * Installs the checksums for the specified metadata files.
     *
     * @param metadataFiles The collection of metadata files to install checksums for, must not be <code>null</code>.
     * @throws IOException If the checksums could not be installed.
     */
    private void installChecksums( Collection<File> metadataFiles )
        throws IOException
    {
        for ( File metadataFile : metadataFiles )
        {
            installChecksums( metadataFile );
        }
    }

    /**
     * Installs the checksums for the specified file (if it exists).
     *
     * @param installedFile The path to the already installed file in the local repo for which to generate checksums,
     *            must not be <code>null</code>.
     * @throws IOException In case of errors. Could not install checksums.
     */
    private void installChecksums( File installedFile )
        throws IOException
    {
        boolean signatureFile = installedFile.getName().endsWith( ".asc" );
        if ( installedFile.isFile() && !signatureFile )
        {
            LOGGER.debug( "Calculating checksums for " + installedFile );
            digester.calculate( installedFile );
            installChecksum( installedFile, ".md5", digester.getMd5() );
            installChecksum( installedFile, ".sha1", digester.getSha1() );
        }
    }

    /**
     * Installs a checksum for the specified file.
     *
     * @param installedFile The base path from which the path to the checksum files is derived by appending the given
     *            file extension, must not be <code>null</code>.
     * @param ext The file extension (including the leading dot) to use for the checksum file, must not be
     *            <code>null</code>.
     * @param checksum the checksum to write
     * @throws IOException If the checksum could not be installed.
     */
    private void installChecksum( File installedFile, String ext, String checksum )
        throws IOException
    {
        File checksumFile = new File( installedFile.getAbsolutePath() + ext );
        LOGGER.debug( "Installing checksum to " + checksumFile );
        try
        {
            // noinspection ResultOfMethodCallIgnored
            checksumFile.getParentFile().mkdirs();
            FileUtils.fileWrite( checksumFile.getAbsolutePath(), "UTF-8", checksum );
        }
        catch ( IOException e )
        {
            throw new IOException( "Failed to install checksum to " + checksumFile, e );
        }
    }

    /**
     * Gets the path of the specified artifact within the local repository. Note that the returned path need not exist
     * (yet).
     *
     * @param buildingRequest The project building request, must not be <code>null</code>.
     * @param artifact The artifact whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact when installed, never <code>null</code>.
     */
    private File getLocalRepoFile( ProjectBuildingRequest buildingRequest, Artifact artifact )
    {
        String path = repositoryManager.getPathForLocalArtifact( buildingRequest, artifact );
        return new File( repositoryManager.getLocalRepositoryBasedir( buildingRequest ), path );
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
