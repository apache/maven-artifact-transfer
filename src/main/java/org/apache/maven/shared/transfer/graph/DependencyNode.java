package org.apache.maven.shared.transfer.graph;

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

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

/**
 * Represents a dependency node within a Maven project's dependency collector.
 *
 * @author Pim Moerenhout
 * @since 0.12
 */
public interface DependencyNode
{
    /**
     * Gets the child nodes of this node.
     *
     * @return the child nodes of this node, never {@code null}
     */
    List<DependencyNode> getChildren();

    /**
     * @return artifact for this DependencyCollectorNode
     */
    Artifact getArtifact();

    /**
     * @return repositories of this DependencyCollectorNode
     */
    List<ArtifactRepository> getRemoteRepositories();

    /**
     * @return true for an optional dependency.
     */
    Boolean getOptional();

    /**
     * @return The scope on the dependency.
     */
    String getScope();

    /**
     * Traverses this node and potentially its children using the specified visitor.
     *
     * @param visitor The visitor to call back, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of this node as well, {@code false} to skip siblings.
     */
    boolean accept( DependencyVisitor visitor );
}
