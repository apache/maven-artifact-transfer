package org.apache.maven.shared.transfer.project.install;

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

import java.io.IOException;

import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;

/**
 * This defines the interface to install a single Maven Project.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
public interface ProjectInstaller
{
    /**
     * This will install a single project which may contain several artifacts. Those artifacts will be installed into
     * the appropriate repository.
     * 
     * <pre class="java">
     *  &#64;Parameter( defaultValue = "${session}", required = true, readonly = true )
     *  private MavenSession session;
     *  &#64;Parameter( defaultValue = "${project}", required = true, readonly = true )
     *  private MavenProject project;
     *  ..
     *  &#64;Component
     *  private ProjectInstaller installer;
     *  ...
     *  public void execute()
     *  {
     *    ProjectInstallerRequest pir =
     *      new ProjectInstallerRequest()
     *         .setProject( mavenProject );
     * 
     *    installer.install( session.getProjectBuildingRequest(), pir );
     *  }
     * </pre>
     * 
     * To set a different local repository than the current one in the Maven session, you can inject an instance of the
     * <code>RepositoryManager</code> and set the path to the local repository, called <code>localRepositoryPath</code>,
     * as such:
     * 
     * <pre class="java">
     * &#64;Component
     * private RepositoryManager repositoryManager;
     * 
     * buildingRequest = repositoryManager.setLocalRepositoryBasedir( buildingRequest, localRepositoryPath );
     * </pre>
     * 
     * @param projectBuildingRequest {@link ProjectBuildingRequest}
     * @param projectInstallerRequest {@link ProjectInstallerRequest}
     * @throws IOException In case of problems related to checksums.
     * @throws ArtifactInstallerException In case of problems to install artifacts.
     * @throws NoFileAssignedException If no file has been assigned to the project.
     * @throws IllegalArgumentException in case of parameter <code>projectBuildingRequest</code> is <code>null</code> or
     *             parameter <code>projectInstallerRequest</code> is <code>null</code>.
     */
    void install( ProjectBuildingRequest projectBuildingRequest, ProjectInstallerRequest projectInstallerRequest )
        throws IOException, ArtifactInstallerException, NoFileAssignedException;

}
