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
package org.apache.maven.shared.transfer.dependencies.resolve.internal;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import org.junit.Before;
import org.junit.Test;

public class Maven30DependencyResolverTest
{
    private Maven30DependencyResolver dr;

    @Before
    public void setUp()
    {
      RepositorySystem system = mock( RepositorySystem.class );
      ArtifactHandlerManager manager = mock( ArtifactHandlerManager.class );
      RepositorySystemSession session = mock( RepositorySystemSession.class );
      ArrayList<RemoteRepository> repositories = new ArrayList<>();
      dr = new Maven30DependencyResolver( system, manager, session, repositories );
    }

    @Test
    public void resolveDependenciesWithEmptyDependenciesShouldNotThrow()
        throws DependencyResolverException
    {
        TransformableFilter filter = mock( TransformableFilter.class );

        dr.resolveDependencies( new ArrayList<Dependency>(), new ArrayList<Dependency>(), filter );
    }

    @Test
    public void resolveDependenciesWithNullMavenDependenciesShouldNotThrow()
        throws DependencyResolverException
    {
        TransformableFilter filter = mock( TransformableFilter.class );

        dr.resolveDependencies( null, new ArrayList<Dependency>(), filter );
    }

}


