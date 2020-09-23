package org.apache.maven.shared.transfer.dependencies.collect.internal;

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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultDependencyCollectorTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DependencyCollector dc;

    @Before
    public void setUp()
    {
        dc = new DefaultDependencyCollector();
    }

    @Test
    public void collectDependenciesWithDependableCoordinatShouldFailWithIAEWhenParameterBuildingRequestIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dc.collectDependencies( null, (DependableCoordinate) null );
    }

    @Test
    public void collectDependenciesWithDependableCoordinatShouldFailWithIAEWhenParameterRootIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter root is not allowed to be null." );

        ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
        dc.collectDependencies( request, (DependableCoordinate) null );
    }

    @Test
    public void collectDependenciesWithDependencyShouldFailWithIAEWhenParameterBuildingRequestIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dc.collectDependencies( null, (Dependency) null );
    }

    @Test
    public void collectDependenciesWithDependencyShouldFailWithIAEWhenParameterRootIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter root is not allowed to be null." );

        ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
        dc.collectDependencies( request, (Dependency) null );
    }

    @Test
    public void collectDependenciesWithModelShouldFailWithIAEWhenParameterBuildingRequestIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dc.collectDependencies( null, (Model) null );
    }

    @Test
    public void collectDependenciesWithModelShouldFailWithIAEWhenParameterRootIsNull()
        throws DependencyCollectorException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter root is not allowed to be null." );

        ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
        dc.collectDependencies( request, (Model) null );
    }

}
