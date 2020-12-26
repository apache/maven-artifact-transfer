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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployer;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.RepositorySystem;

import java.util.Collection;

/**
 *
 */
@Component( role = ArtifactDeployer.class )
class DefaultArtifactDeployer implements ArtifactDeployer
{
    @Requirement
    RepositorySystem repositorySystem;

    @Override
    public void deploy( ProjectBuildingRequest request, Collection<Artifact> mavenArtifacts )
            throws ArtifactDeployerException
    {
        validateParameters( request, mavenArtifacts );

        getMavenArtifactDeployer( request ).deploy( mavenArtifacts );
    }

    @Override
    public void deploy( ProjectBuildingRequest request, ArtifactRepository remoteRepository,
            Collection<Artifact> mavenArtifacts ) throws ArtifactDeployerException
    {
        validateParameters( request, mavenArtifacts );
        getMavenArtifactDeployer( request ).deploy( remoteRepository, mavenArtifacts );
    }

    private void validateParameters( ProjectBuildingRequest request, Collection<Artifact> mavenArtifacts )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "The parameter request is not allowed to be null." );
        }
        if ( mavenArtifacts == null )
        {
            throw new IllegalArgumentException( "The parameter mavenArtifacts is not allowed to be null." );
        }
        if ( mavenArtifacts.isEmpty() )
        {
            throw new IllegalArgumentException( "The collection mavenArtifacts is not allowed to be empty." );
        }
    }

    private MavenArtifactDeployer getMavenArtifactDeployer( ProjectBuildingRequest buildingRequest )
    {
        return new Maven31ArtifactDeployer( repositorySystem, buildingRequest.getRepositorySession() );
    }
}
