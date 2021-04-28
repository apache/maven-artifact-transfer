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

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.internal.Selector;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.apache.maven.shared.transfer.repository.RepositoryManagerException;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.metadata.Metadata.Nature;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;

/**
 * 
 */
@Singleton
@Named( Selector.MAVEN_3_1 )
public final class Maven31RepositoryManager
    implements RepositoryManagerDelegate
{
    private final RepositorySystem repositorySystem;

    @Inject
    public Maven31RepositoryManager( final RepositorySystem repositorySystem )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
    }

    @Override
    public String getPathForLocalArtifact( final ProjectBuildingRequest buildingRequest,
                                           final org.apache.maven.artifact.Artifact artifact )
    {
        Artifact aetherArtifact = RepositoryUtils.toArtifact( artifact );
        return buildingRequest.getRepositorySession()
            .getLocalRepositoryManager().getPathForLocalArtifact( aetherArtifact );
    }

    @Override
    public String getPathForLocalArtifact( final ProjectBuildingRequest buildingRequest,
                                           final ArtifactCoordinate coordinate )
    {
        Artifact aetherArtifact = toArtifact( coordinate );
        return buildingRequest.getRepositorySession()
            .getLocalRepositoryManager().getPathForLocalArtifact( aetherArtifact );
    }

    @Override
    public String getPathForLocalMetadata( final ProjectBuildingRequest buildingRequest,
                                           final ArtifactMetadata metadata )
    {
        Metadata aetherMetadata =
            new DefaultMetadata( metadata.getGroupId(),
                metadata.storedInGroupDirectory() ? null : metadata.getArtifactId(),
                metadata.storedInArtifactVersionDirectory() ? metadata.getBaseVersion() : null,
                "maven-metadata.xml", Nature.RELEASE_OR_SNAPSHOT );

        return buildingRequest.getRepositorySession()
            .getLocalRepositoryManager().getPathForLocalMetadata( aetherMetadata );
    }

    @Override
    public ProjectBuildingRequest setLocalRepositoryBasedir( final ProjectBuildingRequest request, final File basedir )
    {
        ProjectBuildingRequest newRequest = new DefaultProjectBuildingRequest( request );

        RepositorySystemSession session = request.getRepositorySession();

        // "clone" session and replace localRepository
        DefaultRepositorySystemSession newSession = new DefaultRepositorySystemSession( session );

        // Clear cache, since we're using a new local repository
        newSession.setCache( new DefaultRepositoryCache() );

        // keep same repositoryType
        String repositoryType = resolveRepositoryType( session.getLocalRepository() );

        LocalRepositoryManager localRepositoryManager =
            repositorySystem.newLocalRepositoryManager( newSession, new LocalRepository( basedir, repositoryType ) );

        newSession.setLocalRepositoryManager( localRepositoryManager );

        newRequest.setRepositorySession( newSession );

        return newRequest;
    }

    @Override
    public File getLocalRepositoryBasedir(final ProjectBuildingRequest request)
    {
        return request.getRepositorySession().getLocalRepository().getBasedir();
    }

    /**
     * @param localRepository {@link LocalRepository}
     * @return the resolved type.
     */
    private String resolveRepositoryType( LocalRepository localRepository )
    {
        String repositoryType;
        if ( "enhanced".equals( localRepository.getContentType() ) )
        {
            repositoryType = "default";
        }
        else
        {
            // this should be "simple"
            repositoryType = localRepository.getContentType();
        }
        return repositoryType;
    }

    /**
     * @param coordinate {@link ArtifactCoordinate}
     * @return {@link Artifact}
     */
    private Artifact toArtifact( ArtifactCoordinate coordinate )
    {
        if ( coordinate == null )
        {
            return null;
        }

        Artifact result =
            new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(),
                                 coordinate.getExtension(), coordinate.getVersion() );

        return result;
    }
}
