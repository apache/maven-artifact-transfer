package org.apache.maven.plugin.artifact.deployer;

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
 * 3.5.2, 3.5.3. This is done by using the test plugin <code>maven-artifact-deployer-plugin</code> which uses the
 * ArtifactInstaller as component. By using this way we get a real runtime environment which supports all Maven
 * versions.
 * 
 * @author Karl Heinz Marbaise
 */
@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.0.5", "3.1.1", "3.2.5", "3.3.9", "3.5.4", "3.6.3" } )
public class ArtifactDeployerTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public ArtifactDeployerTest( MavenRuntimeBuilder builder )
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
                .withCliOption( "-e" )
                .execute( "clean", "verify" );
        //@formatter:on

        result.assertErrorFreeLog();
        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-artifact-deployer-plugin:1.0.0:artifact-deployer (id-artifact-deployer) @ maven-artifact-deployer-plugin-it ---" );

        String mvnVersion = mavenRuntime.getMavenVersion();
        // The "." will be replaced by "/" in the running of the artifact-installer-plugin so I need to do the same
        // here.
        // Maybe there is a more elegant way to do that?
        mvnVersion = mvnVersion.replaceAll( "\\.", "/" );

        String mavenRepoLocal = System.getProperty( "maven.repo.local" );
        File localRepo = new File( mavenRepoLocal );

        System.out.println( "localRepo='" + localRepo.getAbsolutePath() + "'" );
        System.out.println( "mvnVersion='" + mvnVersion + "'" );

        File baseDirectoy = new File( localRepo, "ARTIFACT-DEPLOYER-GROUPID-" + mvnVersion + "/ARTIFACTID/VERSION/" );

        checkForArtifactFile( baseDirectoy );
        checkForArtifactGroupMetaFile( baseDirectoy );
        checkForArtifactVersionMetaFile( baseDirectoy );
        checkForArtifactClassifierFile( baseDirectoy );

        assertTrue( new File( localRepo, "ARTIFACT-DEPLOYER-GROUPID-" + mvnVersion
            + "/ARTIFACTID/maven-metadata-local.xml" ).exists() ); // ??

    }

    private void checkForArtifactClassifierFile( File baseDirectoy )
    {
        File artifactClassifierFile = new File( baseDirectoy, "ARTIFACTID-VERSION-CLASSIFIER.EXTENSION" );
        assertTrue( "artifactClassifierFile '" + artifactClassifierFile.getAbsolutePath() + "'",
                    artifactClassifierFile.exists() );
        assertTrue( "artifactClassifierFile md5 not found.",
                    new File( artifactClassifierFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "artifactClassifierFile sha1 not found.",
                    new File( artifactClassifierFile.getAbsolutePath() + ".sha1" ).exists() );
    }

    private void checkForArtifactFile( File baseDirectoy )
    {
        File artifactFile = new File( baseDirectoy, "ARTIFACTID-VERSION.EXTENSION" );
        assertTrue( "artifactFile '" + artifactFile.getAbsolutePath() + "'", artifactFile.exists() );
        assertTrue( "artifactFile md5 not found.", new File( artifactFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "artifactFile sha1 not found.", new File( artifactFile.getAbsolutePath() + ".sha1" ).exists() );
    }
    
    private void checkForArtifactGroupMetaFile( File baseDirectoy )
    {
        File localFile = new File( baseDirectoy.getParentFile(), "ARTIFACTID-VERSION-local.camG" );
        File baseFile = new File( baseDirectoy.getParentFile(), "ARTIFACTID-VERSION.camG" );
        assertTrue( "localFile '" + localFile.getAbsolutePath() + "'", localFile.exists() );
        assertTrue( "artifactFile md5 not found.", new File( baseFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "artifactFile sha1 not found.", new File( baseFile.getAbsolutePath() + ".sha1" ).exists() );
    }
    
    private void checkForArtifactVersionMetaFile( File baseDirectoy )
    {
        File localFile = new File( baseDirectoy, "ARTIFACTID-VERSION-local.camV" );
        File baseFile = new File( baseDirectoy, "ARTIFACTID-VERSION.camV" );
        assertTrue( "localFile '" + localFile.getAbsolutePath() + "'", localFile.exists() );
        assertTrue( "artifactFile md5 not found.", new File( baseFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "artifactFile sha1 not found.", new File( baseFile.getAbsolutePath() + ".sha1" ).exists() );
    }
}