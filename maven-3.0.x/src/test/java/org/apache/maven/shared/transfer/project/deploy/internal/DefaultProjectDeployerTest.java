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

import static org.mockito.Mockito.mock;

import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;
import org.apache.maven.shared.transfer.project.deploy.internal.DefaultProjectDeployer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Check the parameter contracts which have been made based on the interface {@link ProjectDeployer}.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmabaise@apache.org</a>
 */
public class DefaultProjectDeployerTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void deployShouldFailWithIAEWhileBuildingRequestIsNull()
        throws IllegalArgumentException, NoFileAssignedException, ArtifactDeployerException
    {
        ProjectDeployer dpi = new DefaultProjectDeployer();

        expectedException.expect( IllegalArgumentException.class );
        expectedException.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dpi.deploy( null, null, null );
    }

    @Test
    public void deployShouldFailWithIAEWhileProjectDeployerRequestIsNull()
        throws IllegalArgumentException, NoFileAssignedException, ArtifactDeployerException
    {
        ProjectDeployer dpi = new DefaultProjectDeployer();

        expectedException.expect( IllegalArgumentException.class );
        expectedException.expectMessage( "The parameter projectDeployerRequest is not allowed to be null." );

        ProjectBuildingRequest pbr = mock( ProjectBuildingRequest.class );
        dpi.deploy( pbr, null, null );
    }

    @Test
    public void deployShouldFailWithIAEWhileArtifactRepositoryIsNull()
        throws IllegalArgumentException, NoFileAssignedException, ArtifactDeployerException
    {
        ProjectDeployer dpi = new DefaultProjectDeployer();

        expectedException.expect( IllegalArgumentException.class );
        expectedException.expectMessage( "The parameter artifactRepository is not allowed to be null." );

        ProjectBuildingRequest pbr = mock( ProjectBuildingRequest.class );
        ProjectDeployerRequest pdr = mock( ProjectDeployerRequest.class );
        dpi.deploy( pbr, pdr, null );
    }

}
