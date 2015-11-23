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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Caches projects and their database keys.
 */
class ProjectKeyStore implements DatabaseKeyStore {
    private static ProjectKeyStore instance = null;
    
    /**
     * Recovers the cache instance.
     * @return the instance of the cache
     */
    static synchronized ProjectKeyStore getInstance() {
        if (instance == null) {
            instance = new ProjectKeyStore();
        }
        
        return instance;
    }
    
    /// --------------------------------
    
    private final HashMap<String,Integer> keyStore;
    
    private ProjectKeyStore() {
        this.keyStore = new HashMap<>();
    }
    
    /**
     * Recovers a database key for a project.
     * @param key a project
     * @return the database key associated with the project
     */
    @Override
    public synchronized Integer get(String key) {
        return this.keyStore.get(key);
    }
    
    /**
     * Caches a project and its database key.
     * @param key a project
     * @param value a database key
     * @return {@code null} or the key previously associated with the project.
     */
    @Override
    public synchronized Integer put(String key, Integer value) {
        return this.keyStore.put(key, value);
    }
    
    /**
     * Recovers the size of the cache.
     * @return the size of the cache
     */
    synchronized Integer size() {
        return this.keyStore.size();
    }
    
    @Deprecated
    synchronized Boolean isEmpty() {
        return this.keyStore.isEmpty();
    }
    
    // used in one place.
    // replace by revising where it is used, or by allowing 
    // DatabaseKeyCache to provide the list of value
    synchronized ArrayList<String> getProjectNames() {
        return new ArrayList<>(Arrays.asList(this.keyStore.keySet().toArray(new String[0])));
    }
}
