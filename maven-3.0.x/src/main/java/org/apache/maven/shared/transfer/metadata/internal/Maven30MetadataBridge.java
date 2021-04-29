package org.apache.maven.shared.transfer.metadata.internal;

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

import java.io.File;

import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.sonatype.aether.metadata.Metadata;

/**
 * A MetadataBridge for Maven 3.0
 * 
 * @author Robert Scholte
 *
 */
public class Maven30MetadataBridge implements Metadata
{
    private ArtifactMetadata metadata;
    
    private File file;

    public Maven30MetadataBridge( ArtifactMetadata metadata )
    {
        this.metadata = metadata;
    }

    @Override
    public String getGroupId()
    {
        return emptify( metadata.getGroupId() );
    }

    @Override
    public String getArtifactId()
    {
        return metadata.storedInGroupDirectory() ? "" : emptify( metadata.getArtifactId() );
    }

    @Override
    public String getVersion()
    {
        return metadata.storedInArtifactVersionDirectory() ? emptify( metadata.getBaseVersion() ) : "";
    }

    @Override
    public String getType()
    {
        return metadata.getRemoteFilename();
    }

    private String emptify( String string )
    {
        return ( string != null ) ? string : "";
    }

    @Override
    public File getFile()
    {
        return file;
    }
    
    @Override
    public Maven30MetadataBridge setFile( File file )
    {
        this.file = file;
        return this;
    }

    @Override
    public Nature getNature()
    {
        if ( metadata instanceof RepositoryMetadata )
        {
            switch ( ( (RepositoryMetadata) metadata ).getNature() )
            {
                case RepositoryMetadata.RELEASE_OR_SNAPSHOT:
                    return Nature.RELEASE_OR_SNAPSHOT;
                case RepositoryMetadata.SNAPSHOT:
                    return Nature.SNAPSHOT;
                default:
                    return Nature.RELEASE;
            }
        }
        else
        {
            return Nature.RELEASE;
        }
    }
}
