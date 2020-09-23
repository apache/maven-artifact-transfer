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

import static org.mockito.Mockito.mock;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Check the parameter contracts which have been made based on the interface {@link ArtifactResolver}.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmabaise@apache.org</a>
 */
public class DefaultArtifactResolverTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ArtifactResolver dap;

    @Before
    public void setUp()
    {
        dap = new DefaultArtifactResolver();
    }

    @Test
    public void resolveArtifactWithArtifactShouldFaileWithIAEWhenParameterBuildingRequestIsNull()
        throws ArtifactResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dap.resolveArtifact( null, (Artifact) null );
    }

    @Test
    public void resolveArtifactWithArtifactShouldFaileWithIAEWhenArtifactIsNull()
        throws ArtifactResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter mavenArtifact is not allowed to be null." );

        ProjectBuildingRequest pbr = mock( ProjectBuildingRequest.class );

        dap.resolveArtifact( pbr, (Artifact) null );
    }

    @Test
    public void resolveArtifactWithCoordinateShouldFaileWithIAEWhenParameterBuildingRequestIsNull()
        throws ArtifactResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dap.resolveArtifact( null, (ArtifactCoordinate) null );
    }

    @Test
    public void resolveArtifactWithCoordinateShouldFaileWithIAEWhenArtifactIsNull()
        throws ArtifactResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter coordinate is not allowed to be null." );

        ProjectBuildingRequest pbr = mock( ProjectBuildingRequest.class );

        dap.resolveArtifact( pbr, (ArtifactCoordinate) null );
    }

}
