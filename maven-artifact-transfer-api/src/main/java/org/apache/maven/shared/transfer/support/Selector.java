package org.apache.maven.shared.transfer.support;

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

import java.util.Map;
import java.util.Objects;

/**
 * Selector that detects "runtime" Maven version and tells which delegate should use.
 */
public final class Selector
{
    public static final String MAVEN_3_0_X = "maven-3.0.x";

    public static final String MAVEN_3_1_X = "maven-3.1.x";

    public static final String RUNTIME = detectRuntime();

    /**
     * Selects delegate from the passed in map, may return {@code null} if no fit delegate not found.
     */
    public static <D> D selectDelegate( final Map<String, D> delegates )
    {
        System.out.println( " ### delegates: " + delegates.keySet() );
        Objects.requireNonNull( delegates, "Null delegates" );
        return delegates.get( RUNTIME );
    }

    /**
     * Detects runtime, or prevents this class to load at all, failing all of components.
     */
    private static String detectRuntime()
    {
        // go from older to newer?
        String runtime = null;
        try
        {
            Trap.MAVEN_3_0_X.check();
            runtime = MAVEN_3_0_X;
        }
        catch ( Trap.TrapException e )
        {
            // skip
        }
        if ( runtime == null )
        {
            try
            {
                Trap.MAVEN_3_1_X.check();
                runtime = MAVEN_3_1_X;
            }
            catch ( Trap.TrapException e )
            {
                // skip
            }
        }

        if ( runtime == null )
        {
            // if here, die
            throw new IllegalStateException( "Could not determine runtime" );
        }
        System.out.println( " ####   RUNTIME " + runtime );
        return runtime;
    }
}