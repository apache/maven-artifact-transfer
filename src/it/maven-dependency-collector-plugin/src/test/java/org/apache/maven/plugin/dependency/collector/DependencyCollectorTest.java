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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

/**
 * This will check if the DependencyCollector works for all Maven versions 3.1.1, 3.2.5, 3.3.1, 3.3.9, 3.5.0, 3.5.2,
 * 3.5.3, 3.5.4, 3.6.3. This is done by using the test plugin <code>maven-dependency-collector-plugin</code> which uses the
 * DependencyCollector as component. By using this way we get a real runtime environment which supports all Maven versions.
 * 
 * @author Gabriel Belingueres
 */
@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( {
    // (Maven version) <-- uses (Aether version)
    // test ONLY with the most recent Maven versions that make use of an specific Aether version.
    "3.1.1", // <-- Eclipse Aether 0.9.0M2
    
//    "3.2.1", // <-- Eclipse Aether 0.9.0M2
//    "3.2.2", // <-- Eclipse Aether 0.9.0M2
    "3.2.3", // <-- Eclipse Aether 0.9.0M2
    "3.2.5", // <-- Eclipse Aether 1.0.0.v20140518
    
//    "3.3.1", // <-- Eclipse Aether 1.0.2.v20150114 
//    "3.3.3", // <-- Eclipse Aether 1.0.2.v20150114 
    "3.3.9", // <-- Eclipse Aether 1.0.2.v20150114
    
    "3.5.0", // <-- Maven Resolver 1.0.3
    "3.5.2", // <-- Maven Resolver 1.1.0
//    "3.5.3", // <-- Maven Resolver 1.1.1
    "3.5.4", // <-- Maven Resolver 1.1.1
    "3.6.0",  // <-- Maven Resolver 1.3.1
    "3.6.1",  // <-- Maven Resolver 1.3.3
//    "3.6.2"  // <-- Maven Resolver 1.4.1
    "3.6.3"  // <-- Maven Resolver 1.4.1
} )
public class DependencyCollectorTest
{
    private static final String LS = System.lineSeparator();
    
    private static boolean testDependenciesInstalled = false;

    @Rule
    public final TestResources resources = new TestResources();

    /**
     * Relates test method name with the project to test below "projects" directory.
     */
    @Rule
    public final TestName testName = new TestName();
    
    public final MavenRuntime mavenRuntime;

    private File basedir;
    
    private MavenExecution mavenExecution;
    
    public DependencyCollectorTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.mavenRuntime = builder.build();
    }
    
    @Before
    public void setUp()
        throws Exception
    {
        installMockDependencies();
        
        String testMethodName = removeMavenVersion( testName.getMethodName() );
        
        basedir = resources.getBasedir( testMethodName );

        //@formatter:off
        mavenExecution = mavenRuntime
            .forProject( basedir )
            .withCliOption( "-DmvnVersion=" + mavenRuntime.getMavenVersion() ) // Might be superfluous
            .withCliOption( "-B" )
            .withCliOption( "-V" );
        //@formatter:on
    }
    
    /**
     * Test method cames in the form "testMethod>[version]", so remove the method name only.
     * @param methodWithMavenVersion the JUnit test method.
     * @return the method without the maven version.
     */
    private String removeMavenVersion( String methodWithMavenVersion )
    {
        int index = methodWithMavenVersion.indexOf( '[' );
        return methodWithMavenVersion.substring( 0, index );
    }

    /**
     * Install dependencies used for testing.
     * 
     * workaround to install the dependencies used in the tests, since
     * mavenRuntime is not static and it is required to install them and 
     * it needs to execute just once.
     * TODO: improve this or find a solution to use mrm-maven-plugin with
     * takari
     * 
     * @throws Exception if anything goes wrong.
     */
    private void installMockDependencies()
        throws Exception
    {
        if ( testDependenciesInstalled )
            return;

        File basedir = resources.getBasedir( "mockDependencies" );
        //@formatter:off
        MavenExecutionResult result =
            mavenRuntime
                .forProject( basedir )
                .withCliOption( "-DmvnVersion=" + mavenRuntime.getMavenVersion() ) // Might be superfluous
                .withCliOption( "-B" )
                .withCliOption( "-V" )
                // We use verify to prevent running maven-install-plugin.
                .execute( "clean", "install" );
        //@formatter:on

        result.assertErrorFreeLog();

        testDependenciesInstalled = true;
    }

    /**
     * collect dependencies, not informing the test dependencies from transitive dependencies.
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void noTransitiveTestDep()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS + 
            "   org.apache.maven.plugin.dependency.collector.its:a:jar:1.0:compile" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:u:jar:1.0:test" + LS +
            LS;

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect dependencies, but don't inform in the tree those dependencies that are already shown in
     * a a level closer to the root.
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void noRepeatedDeps()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:c:jar:1.0:compile" + LS +
            "      org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:compile" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:compile" + LS +
            "      org.apache.maven.plugin.dependency.collector.its:a:jar:1.0:compile" + LS +
            LS;

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect test dependencies, and its respective compile scope dependencies are informed as "test" scope
     * for the project. 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void transitiveTestDeps()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:t:jar:1.1:test" + LS +
            "      org.apache.maven.plugin.dependency.collector.its:u:jar:1.0:test" + LS +
            LS;

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect dependencies, and inform when dependencyManagement supersedes 
     * the declared (premanaged) version and scope.
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void managedDepsAndScope()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:t:jar:1.1:compile" + LS +
            "      org.apache.maven.plugin.dependency.collector.its:u:jar:1.1:test (version managed from 1.0; scope managed from compile)" + LS +
            LS;

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect dependencies, and inform when a dependency is optional.
     * NOTE: Maven 3.0.x and 3.1+ behave differently. 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void optionalDep()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );

        String expected =
                "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
                "   org.apache.maven.plugin.dependency.collector.its:c:jar:1.0:runtime (optional) " + LS + 
                "      org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:runtime (optional) " + LS + 
                "         org.apache.maven.plugin.dependency.collector.its:a:jar:1.0:runtime (optional) " + LS + 
                LS;
// Maven 3.0.x
//          expected =
//              "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
//              "   org.apache.maven.plugin.dependency.collector.its:c:jar:1.0:runtime (optional) " + LS +
//              "      org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:runtime" + LS +
//              "         org.apache.maven.plugin.dependency.collector.its:a:jar:1.0:runtime" + LS +

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect dependencies, and not inform the transitive optional dependencies. 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void noTransitiveOptionalDep()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:d:jar:1.0:compile" + LS + 
            LS;

        assertTrue( strLog.contains( expected ) );
    }
    
    /**
     * collect dependencies, and inform when a dependency version is selected from a range. 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void versionConstraintDep()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );

        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );
        
        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
            "   org.apache.maven.plugin.dependency.collector.its:t:jar:1.1:compile (version selected from constraint [1.1,))" + LS +
            "      org.apache.maven.plugin.dependency.collector.its:u:jar:1.0:compile" + LS +            
            LS;

        assertTrue( strLog.contains( expected ) );
    }

    /**
     * collect dependencies, and not inform when a dependency is excluded.
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void noExcludedDep()
        throws Exception
    {
        MavenExecutionResult result = mavenExecution.execute( "clean", "validate" );
        
        result.assertErrorFreeLog();

        // Check that the current plugins has been called at least once.
        result.assertLogText( "[INFO] --- maven-dependency-collector-plugin:1.0.0:dependency-collector (id-dependency-collector) @ maven-dependency-collector-plugin-it ---" );

        File logFile = new File(basedir, "log.txt");
        String strLog = FileUtils.fileRead( logFile );

        String expected = 
            "PROJECT-DEPENDENCY-COLLECTOR:maven-dependency-collector-plugin-it:jar:1.0.0-A:" + LS +
             "   org.apache.maven.plugin.dependency.collector.its:c:jar:1.0:compile" + LS + 
             "      org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:compile" + LS + 
             "   org.apache.maven.plugin.dependency.collector.its:b:jar:1.0:compile" + LS + 
           LS;

        assertTrue( strLog.contains( expected ) );
    }
}