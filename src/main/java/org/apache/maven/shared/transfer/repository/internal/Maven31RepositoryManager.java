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

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
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

import java.io.File;

/**
 * 
 */
class Maven31RepositoryManager
    implements MavenRepositoryManager
{
    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession session;

    Maven31RepositoryManager( RepositorySystem repositorySystem, RepositorySystemSession session )
    {
        this.repositorySystem = repositorySystem;
        this.session = session;
    }

    @Override
    public String getPathForLocalArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
    {
        return session.getLocalRepositoryManager()
                .getPathForLocalArtifact( RepositoryUtils.toArtifact( mavenArtifact ) );
    }

    @Override
    public String getPathForLocalArtifact( ArtifactCoordinate coordinate )
    {
        return session.getLocalRepositoryManager().getPathForLocalArtifact( toArtifact( coordinate ) );
    }
    
    @Override
    public String getPathForLocalMetadata( ArtifactMetadata metadata )
    {
        Metadata aetherMetadata =
            new DefaultMetadata( metadata.getGroupId(),
                                 metadata.storedInGroupDirectory() ? null : metadata.getArtifactId(),
                                 metadata.storedInArtifactVersionDirectory() ? metadata.getBaseVersion() : null,
                                 "maven-metadata.xml", Nature.RELEASE_OR_SNAPSHOT );

        return session.getLocalRepositoryManager().getPathForLocalMetadata( aetherMetadata );
    }

    @Override
    public ProjectBuildingRequest setLocalRepositoryBasedir( ProjectBuildingRequest buildingRequest, File basedir )
    {
        ProjectBuildingRequest newRequest = new DefaultProjectBuildingRequest( buildingRequest );

        RepositorySystemSession session = buildingRequest.getRepositorySession();

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
    public File getLocalRepositoryBasedir()
    {
        return session.getLocalRepository().getBasedir();
    }

    /**
     * @param localRepository {@link LocalRepository}
     * @return the resolved type.
     */
    protected String resolveRepositoryType( LocalRepository localRepository )
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
    protected Artifact toArtifact( ArtifactCoordinate coordinate )
    {
        if ( coordinate == null )
        {
            return null;
        }

        return new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(),
                             coordinate.getExtension(), coordinate.getVersion() );
    }
}
