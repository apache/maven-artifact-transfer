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
import org.apache.maven.shared.transfer.support.DelegateSupport;
import org.apache.maven.shared.transfer.support.Selector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.util.DefaultRepositoryCache;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;

import java.io.File;
import java.util.Objects;

/**
 *
 */
@Component( role = RepositoryManagerDelegate.class, hint = Selector.MAVEN_3_0_X )
public class Maven30RepositoryManager
        extends DelegateSupport
        implements RepositoryManagerDelegate
{
    @Requirement
    private RepositorySystem repositorySystem;

    public Maven30RepositoryManager()
    {
    }

    public Maven30RepositoryManager( RepositorySystem repositorySystem )
    {
        this.repositorySystem = Objects.requireNonNull( repositorySystem );
    }

    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest,
                                           org.apache.maven.artifact.Artifact mavenArtifact )
    {
        Artifact aetherArtifact = RepositoryUtils.toArtifact( mavenArtifact );

        return buildingRequest.getRepositorySession().getLocalRepositoryManager().getPathForLocalArtifact(
                aetherArtifact );
    }

    @Override
    public String getPathForLocalArtifact( ProjectBuildingRequest buildingRequest,
                                           ArtifactCoordinate coordinate )
    {
        Artifact aetherArtifact = toArtifact( coordinate );

        // LRM.getPathForLocalArtifact() won't throw an Exception, so translate reflection error to RuntimeException

        return buildingRequest.getRepositorySession().getLocalRepositoryManager().getPathForLocalArtifact(
                aetherArtifact );
    }

    @Override
    public String getPathForLocalMetadata( ProjectBuildingRequest buildingRequest,
                                           ArtifactMetadata metadata )
    {
        Metadata aetherMetadata = new DefaultMetadata( metadata.getGroupId(),
                metadata.storedInGroupDirectory() ? null : metadata.getArtifactId(),
                metadata.storedInArtifactVersionDirectory() ? metadata.getBaseVersion() : null, "maven-metadata.xml",
                Nature.RELEASE_OR_SNAPSHOT );

        return buildingRequest.getRepositorySession().getLocalRepositoryManager().getPathForLocalMetadata(
                aetherMetadata );
    }

    @Override
    public ProjectBuildingRequest setLocalRepositoryBasedir( ProjectBuildingRequest buildingRequest,
                                                             File basedir )
    {
        ProjectBuildingRequest newRequest = new DefaultProjectBuildingRequest( buildingRequest );

        RepositorySystemSession session = buildingRequest.getRepositorySession();

        // "clone" session and replace localRepository
        DefaultRepositorySystemSession newSession = new DefaultRepositorySystemSession( session );

        // Clear cache, since we're using a new local repository
        newSession.setCache( new DefaultRepositoryCache() );

        // keep same repositoryType
        String repositoryType = resolveRepositoryType( session.getLocalRepository() );

        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(
                new LocalRepository( basedir, repositoryType ) );

        newSession.setLocalRepositoryManager( localRepositoryManager );

        newRequest.setRepositorySession( newSession );

        return newRequest;
    }

    @Override
    public File getLocalRepositoryBasedir( ProjectBuildingRequest buildingRequest )
    {
        return buildingRequest.getRepositorySession().getLocalRepository().getBasedir();
    }

    /**
     * Aether-1.9+ (i.e. M3.0.2+) expects "default", not "enhanced" as repositoryType
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

        Artifact result = new DefaultArtifact( coordinate.getGroupId(), coordinate.getArtifactId(),
                coordinate.getClassifier(), coordinate.getExtension(), coordinate.getVersion() );

        return result;
    }
}
