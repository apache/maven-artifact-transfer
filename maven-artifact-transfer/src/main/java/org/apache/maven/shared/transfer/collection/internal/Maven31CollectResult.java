package org.apache.maven.shared.transfer.collection.internal;

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

import org.apache.maven.shared.transfer.collection.CollectResult;
import org.apache.maven.shared.transfer.graph.DependencyNode;

/**
 * CollectResult wrapper around {@link CollectResult}
 * 
 * @author Pim Moerenhout
 *
 */
class Maven31CollectResult implements CollectResult
{
    private final org.eclipse.aether.collection.CollectResult collectResult;
    
    /**
     * @param collectResult {@link CollectResult}
     */
    Maven31CollectResult( org.eclipse.aether.collection.CollectResult collectResult )
    {
        this.collectResult = collectResult;
    }

    /**
     * Gets the root node of the dependency graph.
     *
     * @return The root node of the dependency graph or {@code null} if none.
     */
    @Override
    public DependencyNode getRoot()
    {
        return new Maven31DependencyNodeAdapter( collectResult.getRoot() );
    }

    @Override
    public List<Exception> getExceptions()
    {
        return collectResult.getExceptions();
    }

}
