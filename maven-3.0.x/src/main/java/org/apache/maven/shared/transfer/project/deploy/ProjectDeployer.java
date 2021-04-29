package org.apache.maven.shared.transfer.project.deploy;

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;

/**
 * This defines the interface to deploy a single Maven Project.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
public interface ProjectDeployer
{
    /**
     * This will deploy a single project which may contain several artifacts. Those artifacts will be deployed into the
     * appropriate remote repository.
     * 
     * <pre class="java">
     *  &#64;Parameter( defaultValue = "${session}", required = true, readonly = true )
     *  MavenSession session;
     *  &#64;Parameter( defaultValue = "${project}", required = true, readonly = true )
     *  MavenProject project;
     *  ..
     *  &#64;Component
     *  ProjectDeployer deployer;
     *  ...
     *  public void execute()
     *  {
     *    ProjectDeployerRequest pdr = 
     *      new ProjectDeployerRequest()
     *        .setProject( project );
     *  
     *    deployer.deploy( session.getProjectBuildingRequest(), pdr, artifactRepository );
     *  }
     * </pre>
     * 
     * @param buildingRequest {@link ProjectBuildingRequest}
     * @param request {@link ProjectDeployerRequest}
     * @param artifactRepository {@link ArtifactRepository}
     * @throws NoFileAssignedException In case of missing file which has not been assigned to project.
     * @throws ArtifactDeployerException in case of artifact could not correctly deployed.
     * @throws IllegalArgumentException in case <code>buildingRequest</code> is <code>null</code>, <code>request</code>
     *             is <code>null</code> or <code>artifactRepository</code> is <code>null</code>.
     */
    void deploy( ProjectBuildingRequest buildingRequest, ProjectDeployerRequest request,
                 ArtifactRepository artifactRepository )
        throws NoFileAssignedException, ArtifactDeployerException;

}
