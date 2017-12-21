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
 * This will check if the ProjectInstaller works for all Maven versions 3.0.5, 3.1.1, 3.2.5, 3.3.1, 3.3.9, 3.5.0, 3.5.2
 * This is done by using the test plugin <code>maven-project-installer-plugin</code> which uses the ProjectInstaller as
 * component. By using this way we get a real runtime environment which supports all Maven versions.
 * 
 * @author Karl Heinz Marbaise
 */
@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.0.5", "3.1.1", "3.2.5", "3.3.1", "3.3.9", "3.5.0", "3.5.2" } )
public class ProjectInstallerTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public ProjectInstallerTest( MavenRuntimeBuilder builder )
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
                // We use verify to prevent running maven-install-plugin.
                .execute( "clean", "verify" );
        //@formatter:on

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-project-installer-plugin:1.0.0:project-installer (id-project-installer) @ maven-project-installer-plugin-it ---" );

        String mvnVersion = mavenRuntime.getMavenVersion() + "/";
        // The "." will be replaced by "/" in the running of the artifact-installer-plugin so I need to do the same
        // here.
        // Maybe there is a more elegant way to do that?
        mvnVersion = mvnVersion.replaceAll( "\\.", "/" );

        String mavenRepoLocal = System.getProperty( "maven.repo.local" );
        File localRepo = new File( mavenRepoLocal );

        System.out.println( "localRepo='" + localRepo.getAbsolutePath() + "'" );
        System.out.println( "mvnVersion='" + mvnVersion + "'" );

        File baseDirectoy =
            new File( localRepo,
                      "PROJECT-INSTALLER-GROUPID-" + mvnVersion + "maven-project-installer-plugin-it/1.0.0-A/" );

        checkForPomFile( baseDirectoy );

        checkForJarFile( baseDirectoy );

        checkForJarClassifierFile( baseDirectoy );

    }

    private void checkForJarClassifierFile( File baseDirectoy )
    {
        File jarClassifierFile = new File( baseDirectoy, "maven-project-installer-plugin-it-1.0.0-A-classifier.jar" );
        assertTrue( "jarClassifierFile '" + jarClassifierFile.getAbsolutePath() + "'", jarClassifierFile.exists() );
        assertTrue( "jarClassifier md5 not found.", new File( jarClassifierFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "jarClassifier sha1 not found.",
                    new File( jarClassifierFile.getAbsolutePath() + ".sha1" ).exists() );
    }

    private void checkForJarFile( File baseDirectoy )
    {
        File jarFile = new File( baseDirectoy, "maven-project-installer-plugin-it-1.0.0-A.jar" );
        assertTrue( "jarFile '" + jarFile.getAbsolutePath() + "'", jarFile.exists() );
        assertTrue( "jar md5 not found.", new File( jarFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "jar sha1 not found.", new File( jarFile.getAbsolutePath() + ".sha1" ).exists() );
    }

    private void checkForPomFile( File baseDirectoy )
    {
        File pomFile = new File( baseDirectoy, "maven-project-installer-plugin-it-1.0.0-A.pom" );
        assertTrue( "pomFile '" + pomFile.getAbsolutePath() + "'", pomFile.exists() );
        assertTrue( "pom md5 not found.", new File( pomFile.getAbsolutePath() + ".md5" ).exists() );
        assertTrue( "pom sha1 not found.", new File( pomFile.getAbsolutePath() + ".sha1" ).exists() );
    }
}