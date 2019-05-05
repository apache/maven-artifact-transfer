package org.apache.maven.shared.artifact.transfer.noclassworlds;

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

import org.apache.maven.shared.transfer.project.MavenAetherUtils;

/**
 * Test that if there is no dependecy with Classworlds artifact, the import of the library will fallback to doing
 * nothing without runtime errors.
 *
 * @since 0.11.1
 */
public class App
{
    private void test()
    {
        try
        {
            MavenAetherUtils.importAetherLibrary();
            System.out.println( "OK" );
        }
        catch ( Exception e )
        {
            System.out.println( "ERROR" );
        }
    }

    public static void main( String[] args )
    {
        App app = new App();
        app.test();
    }
}
