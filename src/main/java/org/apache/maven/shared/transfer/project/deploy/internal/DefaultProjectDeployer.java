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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployer;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        List<Artifact> deployableArtifacts = new ArrayList<>();
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

        deployableArtifacts.addAll( attachedArtifacts );

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

}
