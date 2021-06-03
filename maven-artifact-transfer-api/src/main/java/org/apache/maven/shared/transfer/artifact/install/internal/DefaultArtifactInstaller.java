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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static org.apache.maven.shared.transfer.support.Selector.selectDelegate;

/**
 *
 */
@Component( role = ArtifactInstaller.class, hint = "default" )
public class DefaultArtifactInstaller
        implements ArtifactInstaller
{
    @Requirement( role = ArtifactInstallerDelegate.class )
    private Map<String, ArtifactInstallerDelegate> delegates;

    public DefaultArtifactInstaller()
    {
    }

    public DefaultArtifactInstaller( Map<String, ArtifactInstallerDelegate> delegates )
    {
        this.delegates = delegates;
    }

    @Override
    public void install( ProjectBuildingRequest request,
                         Collection<Artifact> mavenArtifacts )
            throws ArtifactInstallerException, IllegalArgumentException
    {
        validateParameters( request, mavenArtifacts );
        selectDelegate( delegates ).install( request, mavenArtifacts );
    }

    @Override
    public void install( ProjectBuildingRequest request,
                         File localRepository,
                         Collection<Artifact> mavenArtifacts ) throws ArtifactInstallerException
    {
        validateParameters( request, mavenArtifacts );
        if ( localRepository == null )
        {
            throw new IllegalArgumentException( "The parameter localRepository is not allowed to be null." );
        }
        if ( !localRepository.isDirectory() )
        {
            throw new IllegalArgumentException( "The parameter localRepository must be a directory." );
        }
        selectDelegate( delegates ).install( request, localRepository, mavenArtifacts );
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
}
