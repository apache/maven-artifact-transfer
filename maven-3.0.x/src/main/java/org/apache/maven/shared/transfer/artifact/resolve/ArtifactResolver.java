package org.apache.maven.shared.transfer.artifact.resolve;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;

/**
 * Resolves the artifact, i.e download the file when required and attach it to the artifact
 */
public interface ArtifactResolver
{

    /**
     * @param buildingRequest {@link ProjectBuildingRequest}
     * @param mavenArtifact {@link Artifact}
     * @return {@link ArtifactResult}
     * @throws ArtifactResolverException in case of an error.
     * @throws IllegalArgumentException in case of parameter <code>buildingRequest</code> is <code>null</code> or
     *             parameter <code>mavenArtifact</code> is <code>null</code>.
     */
    ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, Artifact mavenArtifact )
        throws ArtifactResolverException, IllegalArgumentException;

    /**
     * @param buildingRequest {@link ProjectBuildingRequest}
     * @param coordinate {@link ArtifactCoordinate}
     * @return {@link ArtifactResult}
     * @throws ArtifactResolverException in case of an error.
     * @throws IllegalArgumentException in case of parameter <code>buildingRequest</code> is <code>null</code> or
     *             parameter <code>coordinate</code> is <code>null</code>.
     */
    ArtifactResult resolveArtifact( ProjectBuildingRequest buildingRequest, ArtifactCoordinate coordinate )
        throws ArtifactResolverException, IllegalArgumentException;

}
