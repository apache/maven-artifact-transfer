package org.apache.maven.shared.transfer.collection.internal;

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
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;
import org.apache.maven.shared.transfer.collection.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultDependencyCollectorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private DependencyCollector dc;

  @Before
  public void setUp() {
    dc = new org.apache.maven.shared.transfer.collection.internal.DefaultDependencyCollector();
  }

  @Test
  public void collectDependenciesWithDependencyShouldFailWithNPEWhenParameterBuildingRequestIsNull()
      throws DependencyCollectionException {
    thrown.expect( NullPointerException.class );
    thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

    dc.collectDependencies(null, (org.apache.maven.model.Dependency) null);
  }

  @Test
  public void collectDependenciesWithDependencyShouldFailWithNPEWhenParameterRootIsNull()
      throws DependencyCollectionException {
    thrown.expect( NullPointerException.class );
    thrown.expectMessage("The parameter root is not allowed to be null.");

    ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
    dc.collectDependencies(request, (org.apache.maven.model.Dependency) null);
  }

  @Test
  public void collectDependenciesWithDependableCoordinateShouldFailWithNPEWhenParameterBuildingRequestIsNull()
      throws DependencyCollectionException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("The parameter buildingRequest is not allowed to be null.");

    dc.collectDependencies(null, (DependableCoordinate) null);
  }

  @Test
  public void collectDependenciesWithDependableCoordinateShouldFailWithNPEWhenParameterRootIsNull()
      throws DependencyCollectionException {
    thrown.expect( NullPointerException.class );
    thrown.expectMessage( "The parameter root is not allowed to be null." );

    ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
    dc.collectDependencies(request, (DependableCoordinate) null);
  }

  @Test
  public void collectDependenciesWithModelShouldFailWithNPEWhenParameterBuildingRequestIsNull()
      throws DependencyCollectionException {
    thrown.expect( NullPointerException.class );
    thrown.expectMessage( "The parameter buildingRequest is not allowed to be null." );

    dc.collectDependencies(null, (org.apache.maven.model.Model) null);
  }

  @Test
  public void collectDependenciesWithModelShouldFailWithNPEWhenParameterRootIsNull()
      throws DependencyCollectionException {
    thrown.expect( NullPointerException.class );
    thrown.expectMessage( "The parameter root is not allowed to be null." );

    ProjectBuildingRequest request = mock( ProjectBuildingRequest.class );
    dc.collectDependencies(request, (org.apache.maven.model.Model) null);
  }
}
