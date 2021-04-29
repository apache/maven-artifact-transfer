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

/**
 * Traps does a check (loadClass attempt) to detect runtime environment.
 */
public abstract class Trap
{
    public abstract void check();

    public static final Trap MAVEN_3_0_X = new Trap()
    {
        @Override
        public void check()
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass( "org.sonatype.aether.artifact.Artifact" );
            }
            catch ( ClassNotFoundException e )
            {
                throw new TrapException( "Environment does not match Maven 3.0.x", e );
            }
        }
    };

    public static final Trap MAVEN_3_1_X = new Trap()
    {
        @Override
        public void check()
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass( "org.eclipse.aether.artifact.Artifact" );
            }
            catch ( ClassNotFoundException e )
            {
                throw new TrapException( "Environment does not match Maven 3.1.x", e );
            }
        }
    };

    /**
     * Trap exception: trap triggered.
     */
    public static class TrapException extends RuntimeException
    {
        public TrapException( String message, Throwable cause )
        {
            super( message, cause );
        }
    }
}