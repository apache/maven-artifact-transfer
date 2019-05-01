package org.apache.maven.plugin.project.deploy;

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
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.TransferUtils;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;
import org.apache.maven.shared.transfer.project.install.ProjectInstaller;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

/**
 * This mojo is implemented to test the {@link ProjectInstaller} part of the maven-artifact-transfer shared component.
 */
@Mojo( name = "project-deployer", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true )
public class ProjectDeployerMojo
    extends AbstractMojo
{

    /**
     * Parameter to have different locations for each Maven version we are testing with.
     */
    @Parameter
    private String mvnVersion;

    @Component
    protected RepositoryManager repositoryManager;

    @Parameter( defaultValue = "${session}", required = true, readonly = true )
    protected MavenSession session;

    @Parameter( defaultValue = "${plugin}", required = true, readonly = true )
    protected PluginDescriptor pluginDescriptor;

    @Component
    private ProjectDeployer deployer;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Hello from project-deployer plugin" );
        installProject( session.getProjectBuildingRequest() );
        getLog().info( "Bye bye from project-deployer plugin" );
    }

    private void createFileContent( File outputFile )
        throws IOException
    {
        Path file = outputFile.toPath();
        List<String> asList = Arrays.asList( "Line 1", "Line 2" );
        Files.write( file, asList, Charset.forName( "UTF-8" ) );
    }

    private void installProject( ProjectBuildingRequest pbr )
        throws MojoFailureException, MojoExecutionException
    {
        try
        {

            File artifactsDirectory =
                new File( session.getCurrentProject().getBuild().getDirectory(), "tests/artifacts" );
            artifactsDirectory.mkdirs();

            getLog().info( "Directory: '" + artifactsDirectory.getAbsolutePath() + "'" );

            File tmpFile = File.createTempFile( "project-deploy", ".jar", artifactsDirectory );
            createFileContent( tmpFile );

            File tmpFileClassifier = File.createTempFile( "project-deploy-classifier", ".jar", artifactsDirectory );
            createFileContent( tmpFileClassifier );

            projectHelper.attachArtifact( session.getCurrentProject(), "jar", "classifier", tmpFileClassifier );
            session.getCurrentProject().getArtifact().setFile( tmpFile );
            
            ProjectDeployerRequest pdr = new ProjectDeployerRequest();
            pdr.setProject( session.getCurrentProject());
            
            ArtifactRepository repo = session.getCurrentProject().getDistributionManagementArtifactRepository();
            
            TransferUtils.importAetherLibrary( pluginDescriptor );
            deployer.deploy( session.getProjectBuildingRequest(), pdr, repo );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "IOException", e );
        }
        catch ( NoFileAssignedException e )
        {
            throw new MojoExecutionException( "NoFileAssignedException", e );
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoExecutionException( "IllegalArgumentException", e );
        }
        catch ( ArtifactDeployerException e )
        {
            throw new MojoExecutionException( "ArtifactDeployerException", e );
        }

    }

}
