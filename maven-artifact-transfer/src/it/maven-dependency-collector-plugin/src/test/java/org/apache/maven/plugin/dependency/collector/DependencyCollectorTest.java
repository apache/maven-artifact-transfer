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
 * This will check if the DependencyCollector works for all Maven versions 3.0.5, 3.1.1, 3.2.5, 3.3.1, 3.3.9, 3.5.0,
 * 3.5.4, 3.6.3. This is done by using the test plugin <code>maven-artifact-installer-plugin</code> which uses the
 * ArtifactInstaller as component. By using this way we get a real runtime environment which supports all Maven
 * versions.
 * 
 * @author Karl Heinz Marbaise
 */
@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.0.5", "3.1.1", "3.2.5", "3.3.9", "3.5.4", "3.6.3" } )
public class DependencyCollectorTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public DependencyCollectorTest( MavenRuntimeBuilder builder )
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
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        System.out.println( "mavenVersion='" + mavenRuntime.getMavenVersion() + "'" );
    }

}