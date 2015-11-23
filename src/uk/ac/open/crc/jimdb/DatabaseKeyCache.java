/*
 Copyright (C) 2010-2015 The Open University

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package uk.ac.open.crc.jimdb;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * Generic implementation of a cache for database keys and string values.
 */
class DatabaseKeyCache {

    private final DualHashBidiMap<Integer,String> cache;
    
    /**
     * Constructor.
     */
    DatabaseKeyCache() {
        this.cache = new DualHashBidiMap<>();
    }
    
    /**
     * Caches a database key &ndash; string value pair
     * @param key a database key
     * @param value an associated value
     * @return {@code null} unless the key has been previously associated with 
     * a value in which case the previous value is returned
     */
    String put( Integer key, String value ) {
        return this.cache.put( key, value );
    }
    
    /**
     * Retrieves the value associated with a key.
     * @param key a database key
     * @return the value associate with the key
     */
    String get( Integer key ) {
        return this.cache.get( key );
    }
    
    /**
     * Retrieves the database key associated with the given value.
     * @param value a string
     * @return a database key associated with the value or {@code null} if 
     * the value string is unrecognised.
     */
    Integer get( String value ) {
        return this.cache.getKey( value );
    }
    
    /** 
     * Recovers the number of entries in the cache.
     * @return the size of the cache
     */
    int size() {
        return this.cache.size();
    }
}
