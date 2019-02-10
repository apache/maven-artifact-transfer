package org.apache.maven.shared.transfer.dependencies.resolve.internal;

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

import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.apache.maven.shared.transfer.dependencies.resolve.internal.DefaultDependencyResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Check the parameter contracts which have been made based on the interface {@link DependencyResolver}.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmabaise@apache.org</a>
 */
public class DefaultDependencyResolverTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DependencyResolver dr;

    @Before
    public void setUp()
    {
        dr = new DefaultDependencyResolver();
    }

    @Test
    public void resolveDependenciesWithDependableCoordinatShouldFailWithIAEWhenParameterBuildingRequestIsNull()
        throws DependencyResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dr.resolveDependencies( null, (DependableCoordinate) null, null );
    }

    @Test
    public void resolveDependenciesWithDependableCoordinatShouldFailWithIAEWhenParameterCoordinateIsNull()
        throws DependencyResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter coordinate is not allowed to be null." );

        ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
        dr.resolveDependencies( request, (DependableCoordinate) null, null );
    }

    @Test
    public void resolveDependenciesWithModelShouldFailWithIAEWhenParameterBuildingRequestIsNull()
        throws DependencyResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

        dr.resolveDependencies( null, (Model) null, null );
    }

    @Test
    public void resolveDependenciesWithModelShouldFailWithIAEWhenParameterModelIsNull()
        throws DependencyResolverException
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "The parameter model is not allowed to be null." );

        ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
        dr.resolveDependencies( request, (Model) null, null );
    }

}
