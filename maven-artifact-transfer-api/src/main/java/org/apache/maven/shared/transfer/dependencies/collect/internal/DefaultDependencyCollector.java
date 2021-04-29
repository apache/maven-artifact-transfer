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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.collect.CollectorResult;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.apache.maven.shared.transfer.support.DelegatorSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

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

    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                Dependency root ) throws DependencyCollectorException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                DependableCoordinate root ) throws DependencyCollectorException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    public CollectorResult collectDependencies( ProjectBuildingRequest buildingRequest,
                                                Model root ) throws DependencyCollectorException
    {
        validateParameters( buildingRequest, root );
        return delegate.collectDependencies( buildingRequest, root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, DependableCoordinate root )
    {
        validateBuildingRequest( buildingRequest );
        validateRoot( root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Dependency root )
    {
        validateBuildingRequest( buildingRequest );
        validateRoot( root );
    }

    private void validateParameters( ProjectBuildingRequest buildingRequest, Model root )
    {
        validateBuildingRequest( buildingRequest );
        validateRoot( root );
    }

    private void validateBuildingRequest( ProjectBuildingRequest buildingRequest )
    {
        if ( buildingRequest == null )
        {
            throw new IllegalArgumentException( "The parameter buildingRequest is not allowed to be null." );
        }
    }

    private void validateRoot( Object root )
    {
        if ( root == null )
        {
            throw new IllegalArgumentException( "The parameter root is not allowed to be null." );
        }
    }
}
