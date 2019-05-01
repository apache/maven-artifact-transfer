package org.apache.maven.plugin.project.install;

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
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.install.ProjectInstaller;
import org.apache.maven.shared.transfer.project.install.ProjectInstallerRequest;
import org.apache.maven.shared.transfer.repository.RepositoryManager;

/**
 * This mojo is implemented to test the {@link ProjectInstaller} part of the maven-artifact-transfer shared component.
 */
@Mojo( name = "project-installer", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true )
public class ProjectInstallerMojo
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
    private ProjectInstaller installer;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Hello from project-installer plugin" );
        installProject( session.getProjectBuildingRequest() );
        getLog().info( "Bye bye from project-installer plugin" );
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

            File tmpFile = File.createTempFile( "project-install", ".jar", artifactsDirectory );
            createFileContent( tmpFile );

            File tmpFileClassifier = File.createTempFile( "project-install-classifier", ".jar", artifactsDirectory );
            createFileContent( tmpFileClassifier );

            projectHelper.attachArtifact( session.getCurrentProject(), "jar", "classifier", tmpFileClassifier );
            session.getCurrentProject().getArtifact().setFile( tmpFile );
            
            ProjectInstallerRequest pir = new ProjectInstallerRequest();
            pir.setProject( session.getCurrentProject());
            TransferUtils.importAetherLibrary( pluginDescriptor );
            installer.install( pbr, pir );
        }
        catch ( ArtifactInstallerException e )
        {
            throw new MojoExecutionException( "ArtifactInstallerException", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "IOException", e );
        }
        catch ( NoFileAssignedException e )
        {
            throw new MojoExecutionException( "NoFileAssignedException", e );
        }

    }

}
