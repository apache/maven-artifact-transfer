package org.apache.maven.plugin.artifact.installer;

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


import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.collection.DependencyCollector;
import org.apache.maven.shared.transfer.collection.DependencyCollectionException;

/**
 * This mojo is implemented to test the DependencyCollector part of the maven-artifact-transfer shared component.
 */
@Mojo( name = "dependency-collector", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true )
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

    @Component
    private DependencyCollector collector;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Hello from dependency-collector plugin" );
        collectDependencies( session.getProjectBuildingRequest(), session.getCurrentProject().getModel() );
        getLog().info( "Bye bye from dependency-collector plugin" );
    }

    private void collectDependencies( ProjectBuildingRequest projectBuildingRequest, Model model )
        throws MojoFailureException, MojoExecutionException
    {
        try
        {
            collector.collectDependencies( projectBuildingRequest, model );
        }
        catch ( DependencyCollectionException e )
        {
            throw new MojoExecutionException( "DependencyCollectionException", e );
        }
    }

}
