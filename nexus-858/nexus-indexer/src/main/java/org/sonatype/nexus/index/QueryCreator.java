/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

/**
 * A component that creates Query objects from Strings, that can be later combined into one Query.
 * 
 * @author cstamas
 */
public interface QueryCreator
{
    String ROLE = QueryCreator.class.getName();

    Query constructQuery( String field, String query );
}
