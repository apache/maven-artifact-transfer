package org.apache.maven.shared.transfer.project.install.internal;

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

import java.io.IOException;

import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.install.ProjectInstaller;
import org.apache.maven.shared.transfer.project.install.internal.DefaultProjectInstaller;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Check the parameter contracts which have been made based on the interface {@link ProjectInstaller}.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmabaise@apache.org</a>
 */
public class DefaultProjectInstallerTest
{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void installShouldFailWithIAEWhileBuildingRequestIsNull()
        throws IOException, ArtifactInstallerException, NoFileAssignedException
    {
        ProjectInstaller dpi = new DefaultProjectInstaller();

        expectedException.expect( IllegalArgumentException.class );
        expectedException.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dpi.install( null, null );
    }

    @Test
    public void installShouldFailWithIAEWhileProjectInstallerRequestIsNull()
        throws IOException, ArtifactInstallerException, NoFileAssignedException
    {
        ProjectInstaller dpi = new DefaultProjectInstaller();

        expectedException.expect( IllegalArgumentException.class );
        expectedException.expectMessage( "The parameter installerRequest is not allowed to be null." );
        ProjectBuildingRequest pbr = mock( ProjectBuildingRequest.class );

        dpi.install( pbr, null );
    }

}
