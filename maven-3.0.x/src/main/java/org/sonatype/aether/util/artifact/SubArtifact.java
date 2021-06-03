package org.sonatype.aether.util.artifact;

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

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.util.Map;

/**
 * An artifact whose identity is derived from another artifact.
 *
 * Note: this is a one-to-one copy of same class from org.sonatype.aether:aether-util:1.13.1 (used in Maven 3.0,5),
 * as Maven 3.0.5 does NOT EXPORT aether util package to plugins.
 * 
 * @author Benjamin Bentmann
 */
public final class SubArtifact
    extends AbstractArtifact
{

    private final Artifact mainArtifact;

    private final String classifier;

    private final String extension;

    private final File file;

    /**
     * Creates a new sub artifact. The classifier and extension specified for this artifact may use the asterisk
     * character "*" to refer to the corresponding property of the main artifact. For instance, the classifier
     * "*-sources" can be used to refer to the source attachment of an artifact. Likewise, the extension "*.asc" can be
     * used to refer to the GPG signature of an artifact.
     * 
     * @param mainArtifact The artifact from which to derive the identity, must not be {@code null}.
     * @param classifier The classifier for this artifact, may be {@code null} if none.
     * @param extension The extension for this artifact, may be {@code null} if none.
     */
    public SubArtifact( Artifact mainArtifact, String classifier, String extension )
    {
        this( mainArtifact, classifier, extension, null );
    }

    /**
     * Creates a new sub artifact. The classifier and extension specified for this artifact may use the asterisk
     * character "*" to refer to the corresponding property of the main artifact. For instance, the classifier
     * "*-sources" can be used to refer to the source attachment of an artifact. Likewise, the extension "*.asc" can be
     * used to refer to the GPG signature of an artifact.
     * 
     * @param mainArtifact The artifact from which to derive the identity, must not be {@code null}.
     * @param classifier The classifier for this artifact, may be {@code null} if none.
     * @param extension The extension for this artifact, may be {@code null} if none.
     * @param file The file for this artifact, may be {@code null} if unresolved.
     */
    public SubArtifact( Artifact mainArtifact, String classifier, String extension, File file )
    {
        if ( mainArtifact == null )
        {
            throw new IllegalArgumentException( "no artifact specified" );
        }
        this.mainArtifact = mainArtifact;
        this.classifier = classifier;
        this.extension = extension;
        this.file = file;
    }

    public String getGroupId()
    {
        return mainArtifact.getGroupId();
    }

    public String getArtifactId()
    {
        return mainArtifact.getArtifactId();
    }

    public String getVersion()
    {
        return mainArtifact.getVersion();
    }

    public Artifact setVersion( String version )
    {
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), version, getFile(),
                                    getProperties() );
    }

    public String getBaseVersion()
    {
        return mainArtifact.getBaseVersion();
    }

    public boolean isSnapshot()
    {
        return mainArtifact.isSnapshot();
    }

    public String getClassifier()
    {
        return expand( classifier, mainArtifact.getClassifier() );
    }

    public String getExtension()
    {
        return expand( extension, mainArtifact.getExtension() );
    }

    public File getFile()
    {
        return file;
    }

    public Artifact setFile( File file )
    {
        if ( ( this.file == null ) ? file == null : this.file.equals( file ) )
        {
            return this;
        }
        return new SubArtifact( mainArtifact, classifier, extension, file );
    }

    public String getProperty( String key, String defaultValue )
    {
        return mainArtifact.getProperty( key, defaultValue );
    }

    public Map<String, String> getProperties()
    {
        return mainArtifact.getProperties();
    }

    private static String expand( String pattern, String replacement )
    {
        String result = "";
        if ( pattern != null )
        {
            result = pattern.replace( "*", replacement );

            if ( replacement.length() <= 0 )
            {
                if ( pattern.startsWith( "*" ) )
                {
                    int i = 0;
                    for ( ; i < result.length(); i++ )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( i );
                }
                if ( pattern.endsWith( "*" ) )
                {
                    int i = result.length() - 1;
                    for ( ; i >= 0; i-- )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( 0, i + 1 );
                }
            }
        }
        return result;
    }

}
