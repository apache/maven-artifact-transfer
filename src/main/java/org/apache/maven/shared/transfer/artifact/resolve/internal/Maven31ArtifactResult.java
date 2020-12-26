package org.apache.maven.shared.transfer.artifact.resolve.internal;

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

import org.apache.maven.RepositoryUtils;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * {@link org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult} wrapper for {@link ArtifactResult}
 * 
 * @author Robert Scholte
 * @since 3.0
 */
class Maven31ArtifactResult
    implements org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult
{
    private final ArtifactResult artifactResult;

    /**
     * @param artifactResult {@link ArtifactResult}
     */
    Maven31ArtifactResult( ArtifactResult artifactResult )
    {
        this.artifactResult = artifactResult;
    }

    @Override
    public org.apache.maven.artifact.Artifact getArtifact()
    {
        return RepositoryUtils.toArtifact( artifactResult.getArtifact() );
    }
}
