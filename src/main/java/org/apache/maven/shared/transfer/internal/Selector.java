package org.apache.maven.shared.transfer.internal;

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

/**
 * Maven version selector helper.
 */
public final class Selector
{
    public static final String MAVEN_3_0 = "maven-3.0";

    public static final String MAVEN_3_1 = "maven-3.1";

    public static final String MAVEN_4_0 = "maven-4.0";

    public static String selectedMaven()
    {
        return MAVEN_3_1; // TODO: add logic here. Maven3.0 should fail with meaningful error as "not supported"
    }

    /**
     * Returns {@code true} if the current Maven version is Maven 3.1.
     */
    private static boolean isMaven31()
    {
        try
        {
            // Maven 3.1 specific
            Thread.currentThread().getContextClassLoader().loadClass( "org.eclipse.aether.artifact.Artifact" );
            return true;
        }
        catch ( ClassNotFoundException e )
        {
            return false;
        }
    }
}
