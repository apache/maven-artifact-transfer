package org.apache.maven.shared.transfer.artifact.resolve.internal;

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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.internal.Delegator;

/**
 *
 */
@Singleton
@Named
public class DefaultArtifactResolver
    extends Delegator<ArtifactResolverDelegate>
    implements ArtifactResolver
{
    private ArtifactResolverDelegate delegate;

    @Inject
    public DefaultArtifactResolver(final Map<String, ArtifactResolverDelegate> delegates)
    {
        super(delegates);
        this.delegate = selectDelegate();
    }

    @Override
    public ArtifactResult resolveArtifact(final ProjectBuildingRequest buildingRequest,
                                          final Artifact mavenArtifact)
        throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, mavenArtifact );
        return delegate.resolveArtifact(buildingRequest, mavenArtifact);
    }

    @Override
    public ArtifactResult resolveArtifact(final ProjectBuildingRequest buildingRequest,
                                          final ArtifactCoordinate coordinate)
        throws ArtifactResolverException, IllegalArgumentException
    {
        validateParameters( buildingRequest, coordinate );
        return delegate.resolveArtifact(buildingRequest, coordinate);
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( mavenArtifact == null )
        {
            throw new IllegalArgumentException( "The parameter mavenArtifact is not allowed to be null." );
        }
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
        if ( coordinate == null )
        {
            throw new IllegalArgumentException( "The parameter coordinate is not allowed to be null." );
        }
    }
}
