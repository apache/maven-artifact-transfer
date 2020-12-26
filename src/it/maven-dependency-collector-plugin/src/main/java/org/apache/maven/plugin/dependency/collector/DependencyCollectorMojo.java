package org.apache.maven.plugin.dependency.collector;

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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor;
import org.apache.maven.shared.transfer.dependencies.collect.CollectorResult;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollector;
import org.apache.maven.shared.transfer.dependencies.collect.DependencyCollectorException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;
import org.apache.maven.shared.transfer.project.install.ProjectInstaller;

/**
 * This mojo is implemented to test the {@link DependencyCollector} part of the maven-artifact-transfer shared component.
 *
 * @author Gabriel Belingueres
 */
@Mojo( name = "dependency-collector", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true )
public class DependencyCollectorMojo
    extends AbstractMojo
{

    /**
     * Parameter to have different locations for each Maven version we are testing with.
     */
    @Parameter
    private String mvnVersion;

    @Parameter( defaultValue = "${session}", required = true, readonly = true )
    protected MavenSession session;

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    protected MavenProject project;

    @Component
    private DependencyCollector dependencyCollector;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Hello from dependency-collector plugin" );

        ProjectBuildingRequest buildingRequest =
            new DefaultProjectBuildingRequest( session.getProjectBuildingRequest() );
        buildingRequest.setProject( project );

        collectDependencies( buildingRequest );
        getLog().info( "Bye bye from dependency-collector plugin" );
    }

    private void collectDependencies( ProjectBuildingRequest buildingRequest ) throws MojoExecutionException
    {
        try
        {
            CollectorResult result = dependencyCollector.collectDependenciesGraph( buildingRequest, project.getModel() );
            DependencyNode root = result.getDependencyGraphRoot();

            StringWriter writer = new StringWriter();
            SerializingDependencyNodeVisitor visitor = new SerializingDependencyNodeVisitor( writer );
            root.accept( visitor );

            getLog().info( writer.toString() );
        }
        catch ( DependencyCollectorException e )
        {
            throw new MojoExecutionException( "DependencyCollectorException", e );
        }
    }

}
