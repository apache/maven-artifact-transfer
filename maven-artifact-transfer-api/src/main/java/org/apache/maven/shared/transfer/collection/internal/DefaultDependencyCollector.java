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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.collection.CollectResult;
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;
import org.apache.maven.shared.transfer.collection.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.support.DelegatorSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
@Singleton
@Named
public class DefaultDependencyCollector
        extends DelegatorSupport<DependencyCollectorDelegate>
        implements DependencyCollector
{
    @Inject
    public DefaultDependencyCollector( Map<String, DependencyCollectorDelegate> delegates )
    {
        super( delegates );
    }

    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              Dependency root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              DependableCoordinate root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              Model root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, DependableCoordinate root )
    {
        validateBuildingRequestAndRoot( buildingRequest, root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Dependency root )
    {
        validateBuildingRequestAndRoot( buildingRequest, root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Model root )
    {
        validateBuildingRequestAndRoot( buildingRequest, root );
    }

    private void validateBuildingRequestAndRoot( ProjectBuildingRequest buildingRequest, Object root )
    {
        validateBuildingRequest( buildingRequest );
        validateRoot( root );
    }

    private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
    {
        Objects.requireNonNull( buildingRequest, "The parameter buildingRequest is not allowed to be null." );
    }

    private void validateRoot( Object root )
    {
        Objects.requireNonNull( root, "The parameter root is not allowed to be null." );
    }
}
