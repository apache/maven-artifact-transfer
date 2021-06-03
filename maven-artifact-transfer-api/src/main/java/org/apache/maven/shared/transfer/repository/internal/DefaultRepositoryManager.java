package org.apache.maven.shared.transfer.repository.internal;

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
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.Map;

import static org.apache.maven.shared.transfer.support.Selector.selectDelegate;

/**
 *
 */
@Component( role = RepositoryManager.class, hint = "default" )
public class DefaultRepositoryManager
        implements RepositoryManager
{
    @Requirement( role = RepositoryManagerDelegate.class )
    private Map<String, RepositoryManagerDelegate> delegate;

    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest, Artifact artifact )
    {
        return selectDelegate( delegate ).getPathForLocalArtifact( buildingRequest, artifact );
    }

    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
    {
        return selectDelegate( delegate ).getPathForLocalArtifact( buildingRequest, coordinate );
    }

    @Override
    public String getPathForLocalMetadata( ProjectBuildingRequest buildingRequest, ArtifactMetadata metadata )
    {
        if ( metadata instanceof ProjectArtifactMetadata )
        {
            DefaultArtifactCoordinate pomCoordinate = new DefaultArtifactCoordinate();
            pomCoordinate.setGroupId( metadata.getGroupId() );
            pomCoordinate.setArtifactId( metadata.getArtifactId() );
            pomCoordinate.setVersion( metadata.getBaseVersion() );
            pomCoordinate.setExtension( "pom" );
            return getPathForLocalArtifact( buildingRequest, pomCoordinate );
        }

        return selectDelegate( delegate ).getPathForLocalMetadata( buildingRequest, metadata );
    }

    @Override
    public ProjectBuildingRequest setLocalRepositoryBasedir( ProjectBuildingRequest request, File basedir )
    {
        return selectDelegate( delegate ).setLocalRepositoryBasedir( request, basedir );
    }

    @Override
    public File getLocalRepositoryBasedir( ProjectBuildingRequest request )
    {
        return selectDelegate( delegate ).getLocalRepositoryBasedir( request );
    }
}
