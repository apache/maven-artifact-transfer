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

import java.util.AbstractList;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * This class is a copy of the Stack class in a more recent version of Sonatype Aether, since 1.7 does not have it.
 * 
 * @param <E> the type of the elements of the stack.
 * @author Gabriel Belingueres
 */
public class Maven30Stack<E>
    extends AbstractList<E>
    implements RandomAccess
{

    private Object[] elements = new Object[64];

    private int size;

    public void push( E element )
    {
        if ( size >= elements.length )
        {
            Object[] tmp = new Object[size + 64];
            System.arraycopy( elements, 0, tmp, 0, elements.length );
            elements = tmp;
        }
        elements[size++] = element;
    }

    @SuppressWarnings( "unchecked" )
    public E pop()
    {
        if ( size <= 0 )
        {
            throw new NoSuchElementException();
        }
        return (E) elements[--size];
    }

    @SuppressWarnings( "unchecked" )
    public E peek()
    {
        if ( size <= 0 )
        {
            return null;
        }
        return (E) elements[size - 1];
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public E get( int index )
    {
        if ( index < 0 || index >= size )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
        }
        return (E) elements[size - index - 1];
    }

    @Override
    public int size()
    {
        return size;
    }

}
