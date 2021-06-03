package org.apache.maven.shared.transfer.collection;

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

/**
 * Will only download the pom files when not available, never the artifact. 
 * 
 * @author Robert Scholte
 *
 */
public interface DependencyCollector
{

    /**
     * Collects the transitive dependencies of some artifacts and builds a dependency graph. Note that this operation is
     * only concerned about determining the coordinates of the transitive dependencies and does not actually resolve the
     * artifact files. The supplied session carries various hooks to customize the dependency graph that must be invoked
     * throughout the operation.
     *
     * @param buildingRequest The Maven project buildingrequest, must not be {@code null}.
     * @param root The Maven Dependency, must not be {@code null}.
     * @return The collection result, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     */
    CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, Dependency root )
        throws DependencyCollectionException;

    /**
     * Collects the transitive dependencies of some artifacts and builds a dependency graph. Note that this operation is
     * only concerned about determining the coordinates of the transitive dependencies and does not actually resolve the
     * artifact files. The supplied session carries various hooks to customize the dependency graph that must be invoked
     * throughout the operation.
     *
     * @param buildingRequest The Maven project buildingrequest, must not be {@code null}.
     * @param root The Maven DependableCoordinate, must not be {@code null}.
     * @return The collection result, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     */
    CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, DependableCoordinate root )
        throws DependencyCollectionException;

    /**
     * Collects the transitive dependencies of some artifacts and builds a dependency graph. Note that this operation is
     * only concerned about determining the coordinates of the transitive dependencies and does not actually resolve the
     * artifact files. The supplied session carries various hooks to customize the dependency graph that must be invoked
     * throughout the operation.
     *
     * @param buildingRequest The Maven project buildingrequest, must not be {@code null}.
     * @param root The Maven model, must not be {@code null}.
     * @return The collection result, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     */
    CollectResult collectDependencies( ProjectBuildingRequest buildingRequest, Model root )
        throws DependencyCollectionException;

}
