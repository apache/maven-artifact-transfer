package org.apache.maven.shared.transfer.dependencies.collect.internal;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * This class is a copy of their homonymous in the Eclipse Aether library, adapted to work with Sonatype Aether.
 * 
 * @author Gabriel Belingueres
 */
class Maven30PathRecordingDependencyVisitor
    implements DependencyVisitor
{

    private final DependencyFilter filter;

    private final List<List<DependencyNode>> paths;

    private final Maven30Stack<DependencyNode> parents;

    private final boolean excludeChildrenOfMatches;

    /**
     * Creates a new visitor that uses the specified filter to identify terminal nodes of interesting paths. The visitor
     * will not search for paths going beyond an already matched node.
     * 
     * @param filter The filter used to select terminal nodes of paths to record, may be {@code null} to match any node.
     */
    Maven30PathRecordingDependencyVisitor( DependencyFilter filter )
    {
        this( filter, true );
    }

    /**
     * Creates a new visitor that uses the specified filter to identify terminal nodes of interesting paths.
     * 
     * @param filter The filter used to select terminal nodes of paths to record, may be {@code null} to match any node.
     * @param excludeChildrenOfMatches Flag controlling whether children of matched nodes should be excluded from the
     *            traversal, thereby ignoring any potential paths to other matching nodes beneath a matching ancestor
     *            node. If {@code true}, all recorded paths will have only one matching node (namely the terminal node),
     *            if {@code false} a recorded path can consist of multiple matching nodes.
     */
    Maven30PathRecordingDependencyVisitor( DependencyFilter filter, boolean excludeChildrenOfMatches )
    {
        this.filter = filter;
        this.excludeChildrenOfMatches = excludeChildrenOfMatches;
        this.paths = new ArrayList<List<DependencyNode>>();
        this.parents = new Maven30Stack<DependencyNode>();
    }

    /**
     * Gets the filter being used to select terminal nodes.
     * 
     * @return The filter being used or {@code null} if none.
     */
    public DependencyFilter getFilter()
    {
        return filter;
    }

    /**
     * Gets the paths leading to nodes matching the filter that have been recorded during the graph visit. A path is
     * given as a sequence of nodes, starting with the root node of the graph and ending with the node that matched the
     * filter.
     * 
     * @return The recorded paths, never {@code null}.
     */
    public List<List<DependencyNode>> getPaths()
    {
        return paths;
    }

    @Override
    public boolean visitEnter( DependencyNode node )
    {
        boolean accept = filter == null || filter.accept( node, parents );

        parents.push( node );

        if ( accept )
        {
            DependencyNode[] path = new DependencyNode[parents.size()];
            int i = parents.size() - 1;
            for ( DependencyNode n : parents )
            {
                path[i] = n;
                i--;
            }
            paths.add( Arrays.asList( path ) );
        }

        return !( excludeChildrenOfMatches && accept );
    }

    @Override
    public boolean visitLeave( DependencyNode node )
    {
        parents.pop();

        return true;
    }

}
