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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;
import java.util.Objects;

import static org.apache.maven.shared.transfer.support.Selector.selectDelegate;

/**
 *
 */
@Component( role = DependencyCollector.class, hint = "default" )
public class DefaultDependencyCollector
        implements DependencyCollector
{
    @Requirement( role = DependencyCollectorDelegate.class )
    private Map<String, DependencyCollectorDelegate> delegates;

    public DefaultDependencyCollector()
    {
    }

    public DefaultDependencyCollector( Map<String, DependencyCollectorDelegate> delegates )
    {
        this.delegates = delegates;
    }

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              Dependency root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return selectDelegate( delegates ).collectDependencies( buildingRequest, root );
    }

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              DependableCoordinate root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return selectDelegate( delegates ).collectDependencies( buildingRequest, root );
    }

    @Override
    public CollectResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                              Model root )
            throws DependencyCollectionException
    {
        validateParameters( buildingRequest, root );
        return selectDelegate( delegates ).collectDependencies( buildingRequest, root );
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
