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

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.sonatype.aether.graph.DependencyNode;

/**
 * This class replace the internal data Map lacking inside DependencyNode on earlier Sonatype Aether versions.
 * 
 * @author Gabriel Belingueres
 */
public class Maven30NodeData
{

    private IdentityHashMap<DependencyNode, Map<Object, Object>> nodeMap =
        new IdentityHashMap<DependencyNode, Map<Object, Object>>();

    public void putData( DependencyNode node, Object key, Object value )
    {
        Map<Object, Object> dataMap = nodeMap.get( node );
        if ( dataMap == null )
        {
            dataMap = Collections.emptyMap();
        }
        dataMap = setData( dataMap, key, value );
        nodeMap.put( node, dataMap );
    }

    public Map<Object, Object> getData( DependencyNode node )
    {
        Map<Object, Object> dataMap = nodeMap.get( node );
        if ( dataMap == null )
        {
            dataMap = Collections.emptyMap();
        }
        return dataMap;
    }

    private Map<Object, Object> setData( Map<Object, Object> data, Object key, Object value )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "key must not be null" );
        }

        if ( value == null )
        {
            if ( !data.isEmpty() )
            {
                data.remove( key );

                if ( data.isEmpty() )
                {
                    data = Collections.emptyMap();
                }
            }
        }
        else
        {
            if ( data.isEmpty() )
            {
                data = new HashMap<Object, Object>();
            }
            data.put( key, value );
        }

        return data;
    }
}
