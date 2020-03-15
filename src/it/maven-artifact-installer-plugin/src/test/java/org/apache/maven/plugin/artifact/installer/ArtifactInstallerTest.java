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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

/**
 * This will check if the ArtifactInstaller works for all Maven versions 3.0.5, 3.1.1, 3.2.5, 3.3.1, 3.3.9, 3.5.0,
 * 3.5.2, 3.5.3. This is done by using the test plugin <code>maven-artifact-installer-plugin</code> which uses the
 * ArtifactInstaller as component. By using this way we get a real runtime environment which supports all Maven
 * versions.
 * 
 * @author Karl Heinz Marbaise
 */
@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.0.5", "3.1.1", "3.2.5", "3.3.9", "3.5.4", "3.6.3" } )
public class ArtifactInstallerTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public ArtifactInstallerTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.mavenRuntime = builder.build();
    }

    @Test
    public void buildExample()
        throws Exception
    {
        File basedir = resources.getBasedir( "example" );
        //@formatter:off
        MavenExecutionResult result =
            mavenRuntime
                .forProject( basedir )
                .withCliOption( "-DmvnVersion=" + mavenRuntime.getMavenVersion() ) // Might be superfluous
                .withCliOption( "-B" )
                .withCliOption( "-V" )
                .execute( "clean", "verify" );
        //@formatter:on

        result.assertErrorFreeLog();
        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-artifact-installer-plugin:1.0.0:artifact-installer (id-artifact-installer) @ maven-artifact-installer-plugin-it ---" );

        String mvnVersion = mavenRuntime.getMavenVersion() + "/";
        // The "." will be replaced by "/" in the running of the artifact-installer-plugin so I need to do the same
        // here.
        // Maybe there is a more elegant way to do that?
        mvnVersion = mvnVersion.replaceAll( "\\.", "/" );

        String mavenRepoLocal = System.getProperty( "maven.repo.local" );
        File localRepo = new File( mavenRepoLocal );

        System.out.println( "localRepo='" + localRepo.getAbsolutePath() + "'" );
        System.out.println( "mvnVersion='" + mvnVersion + "'" );

        File baseDirectoy = new File( localRepo, "ARTIFACT-INSTALLER-GROUPID-" + mvnVersion + "/ARTIFACTID/VERSION/" );

        // We don't have a pom file.
        checkForNonExistingPomFile( baseDirectoy );
        checkForArtifact( baseDirectoy );
        checkForArtifactGroupMetaFile( baseDirectoy );
        checkForArtifactVersionMetaFile( baseDirectoy );
        checkForArtifactClassifier( baseDirectoy );
    }

    private void checkForArtifactClassifier( File baseDirectoy )
    {
        File jarArtifactClassifierFile = new File( baseDirectoy, "ARTIFACTID-VERSION-CLASSIFIER.EXTENSION" );
        assertTrue( "jarClassifierFile '" + jarArtifactClassifierFile.getAbsolutePath() + "'",
                    jarArtifactClassifierFile.exists() );
        assertFalse( "jarClassifier md5 not found.",
                     new File( jarArtifactClassifierFile.getAbsolutePath() + ".md5" ).exists() );
        assertFalse( "jarClassifier sha1 not found.",
                     new File( jarArtifactClassifierFile.getAbsolutePath() + ".sha1" ).exists() );
    }

    private void checkForArtifact( File baseDirectoy )
    {
        File artifactFile = new File( baseDirectoy, "ARTIFACTID-VERSION.EXTENSION" );
        assertTrue( "artifactFile '" + artifactFile.getAbsolutePath() + "'", artifactFile.exists() );
        assertFalse( "artifactFile md5 not found.", new File( artifactFile.getAbsolutePath() + ".md5" ).exists() );
        assertFalse( "artifactFile sha1 not found.", new File( artifactFile.getAbsolutePath() + ".sha1" ).exists() );
    }
    
    private void checkForArtifactGroupMetaFile( File baseDirectoy )
    {
        File localFile = new File( baseDirectoy.getParentFile(), "ARTIFACTID-VERSION-local.camG" );
        File baseFile = new File( baseDirectoy.getParentFile(), "ARTIFACTID-VERSION.camG" );
        assertTrue( "localFile '" + localFile.getAbsolutePath() + "'", localFile.exists() );
    }
    
    private void checkForArtifactVersionMetaFile( File baseDirectoy )
    {
        File localFile = new File( baseDirectoy, "ARTIFACTID-VERSION-local.camV" );
        File baseFile = new File( baseDirectoy, "ARTIFACTID-VERSION.camV" );
        assertTrue( "localFile '" + localFile.getAbsolutePath() + "'", localFile.exists() );
    }

    private void checkForNonExistingPomFile( File baseDirectoy )
    {
        File pomFile = new File( baseDirectoy, "ARTIFACTID-VERSION.EXTENSION.pom" );
        assertFalse( "pomFile '" + pomFile.getAbsolutePath() + "'", pomFile.exists() );
        assertFalse( "pom md5 not found.", new File( pomFile.getAbsolutePath() + ".md5" ).exists() );
        assertFalse( "pom sha1 not found.", new File( pomFile.getAbsolutePath() + ".sha1" ).exists() );
    }
}