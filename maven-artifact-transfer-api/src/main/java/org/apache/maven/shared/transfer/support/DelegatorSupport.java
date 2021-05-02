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
 * Support class for delegators.
 *
 * @param <D> the delegator type.
 */
public abstract class DelegatorSupport<D> extends ComponentSupport
{
    protected final D delegate;

    protected DelegatorSupport( final Map<String, D> delegates )
    {
        Objects.requireNonNull( delegates, "Null delegates for " + getClass() );
        if ( delegates.isEmpty() )
        {
            throw new IllegalStateException( "No delegates found for " + getClass() );
        }
        this.delegate = Objects.requireNonNull( Selector.selectDelegate( delegates ),
                "Could not select delegate keyed as " + Selector.RUNTIME + " from delegates " + delegates );
    }
}