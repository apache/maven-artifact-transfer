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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstaller;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.RepositorySystem;

import java.io.File;
import java.util.Collection;

/**
 *
 */
@Component( role = ArtifactInstaller.class )
class DefaultArtifactInstaller implements ArtifactInstaller
{
    @Requirement
    private RepositoryManager repositoryManager;

    @Requirement
    private RepositorySystem repositorySystem;

    @Override
    public void install( ProjectBuildingRequest request, Collection<Artifact> mavenArtifacts )
            throws ArtifactInstallerException, IllegalArgumentException
    {
        validateParameters( request, mavenArtifacts );
        getMavenArtifactInstaller( request ).install( mavenArtifacts );
    }

    @Override
    public void install( ProjectBuildingRequest request, File localRepositry, Collection<Artifact> mavenArtifacts )
            throws ArtifactInstallerException
    {
        validateParameters( request, mavenArtifacts );
        if ( localRepositry == null )
        {
            throw new IllegalArgumentException( "The parameter localRepository is not allowed to be null." );
        }
        if ( !localRepositry.isDirectory() )
        {
            throw new IllegalArgumentException( "The parameter localRepository must be a directory." );
        }

        // TODO: Should we check for exists() ?

        // update local repo in request 
        ProjectBuildingRequest newRequest = repositoryManager.setLocalRepositoryBasedir( request, localRepositry );

        getMavenArtifactInstaller( newRequest ).install( mavenArtifacts );
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

    private MavenArtifactInstaller getMavenArtifactInstaller( ProjectBuildingRequest buildingRequest )
    {
        return new Maven31ArtifactInstaller( repositorySystem, buildingRequest.getRepositorySession() );
    }
}
