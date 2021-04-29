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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.metadata.AbstractArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstaller;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstallerException;
import org.apache.maven.shared.transfer.metadata.ArtifactMetadata;
import org.apache.maven.shared.transfer.repository.RepositoryManager;

/**
 * This mojo is implemented to test the ArtifactInstaller part of the maven-artifact-transfer shared component.
 */
@Mojo( name = "artifact-installer", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true )
public class ArtifactInstallerMojo
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

    @Component
    private ArtifactInstaller installer;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Hello from artifact-installer plugin" );
        installProject( session.getProjectBuildingRequest() );
        getLog().info( "Bye bye from artifact-installer plugin" );
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
            DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler();
            artifactHandler.setExtension( "EXTENSION" );

            File artifactsDirectory =
                new File( session.getCurrentProject().getBuild().getDirectory(), "tests/artifacts" );
            getLog().info( "Directory: '" + artifactsDirectory.getAbsolutePath() + "'" );
            artifactsDirectory.mkdirs();

            File tmpFile = File.createTempFile( "test-install", ".jar", artifactsDirectory );
            createFileContent( tmpFile );

            DefaultArtifact artifact = new DefaultArtifact( "ARTIFACT-INSTALLER-GROUPID-" + mvnVersion, "ARTIFACTID", "VERSION", "compile",
                                                            "jar", null, artifactHandler );
            artifact.setFile( tmpFile );
            DefaultArtifact artifactWithClassifier =
                new DefaultArtifact( "ARTIFACT-INSTALLER-GROUPID-" + mvnVersion, "ARTIFACTID", "VERSION", "compile", "jar", "CLASSIFIER",
                                     artifactHandler );

            File tmpFileClassifier = File.createTempFile( "test-install-classifier", ".jar", artifactsDirectory );
            createFileContent( tmpFileClassifier );
            artifactWithClassifier.setFile( tmpFileClassifier );

            Collection<Artifact> mavenArtifacts = Arrays.<Artifact>asList( artifact, artifactWithClassifier );
            
            for ( Artifact a : mavenArtifacts )
            {
                File camVfile = File.createTempFile( "test-install", ".camV", artifactsDirectory );
                a.addMetadata( new CustomArtifactMetadata( a, camVfile, true ) );
                
                File camGfile = File.createTempFile( "test-install", ".camG", artifactsDirectory );
                a.addMetadata( new CustomArtifactMetadata( a, camGfile, false ) );
            }

            installer.install( session.getProjectBuildingRequest(), mavenArtifacts );
        }
        catch ( ArtifactInstallerException | IOException e )
        {
            throw new MojoExecutionException( e.getClass().getName(), e );
        }

    }

    private class CustomArtifactMetadata extends AbstractArtifactMetadata implements ArtifactMetadata
    {
        private final File file;

        private final boolean storedInArtifactVersionDirectory;
        
        protected CustomArtifactMetadata( Artifact artifact, File file, boolean storedInArtifactVersionDirectory ) 
        {
            super( artifact );   
            this.file = file;
            this.storedInArtifactVersionDirectory = storedInArtifactVersionDirectory;
        }
        
        @Override
        public File getFile() 
        {
            return file;
        }
        
        @Override
        public String getRemoteFilename()
        {
            return artifact.getArtifactId() + '-' + artifact.getVersion() + getDotExtension();
        }
        
        @Override
        public String getLocalFilename( ArtifactRepository repository )
        {
            return artifact.getArtifactId() + '-' + artifact.getVersion() + getDotExtension();
        }
        
        @Override
        public void storeInLocalRepository( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
            throws RepositoryMetadataStoreException
        {
            throw new UnsupportedOperationException("ArtifactDeployerMojo.CustomArtifactMetadata.storeInLocalRepository(ArtifactRepository, ArtifactRepository)");   
        }
        
        @Override
        public boolean storedInArtifactVersionDirectory()
        {
            return storedInArtifactVersionDirectory;
        }
        
        @Override
        public void merge( org.apache.maven.artifact.metadata.ArtifactMetadata metadata )
        {
            throw new UnsupportedOperationException("ArtifactDeployerMojo.CustomArtifactMetadata.merge(ArtifactMetadata)");
        }

        @Override
        public void merge( org.apache.maven.repository.legacy.metadata.ArtifactMetadata metadata )
        {
            throw new UnsupportedOperationException("ArtifactDeployerMojo.CustomArtifactMetadata.merge(ArtifactMetadata)");
        }
        
        @Override
        public String getBaseVersion()
        {
            return artifact.getBaseVersion();
        }

        @Override
        public Object getKey()
        {
            return artifact.getId() + getDotExtension();
        }
        
        private String getDotExtension() 
        {
            return file.getName().substring( file.getName().lastIndexOf( '.' ) );
        }
    }
}
