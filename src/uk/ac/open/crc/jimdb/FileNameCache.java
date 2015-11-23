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

/**
 * Caches database key &ndash; file name pairs.
 */
class FileNameCache extends DatabaseKeyCache {
    
    private static FileNameCache instance = null;
    
    /**
     * Retrieves the instance of this class.
     * @return the instance of this class
     */
    synchronized static FileNameCache getInstance() {
        if ( instance == null ) {
            instance = new FileNameCache();
        }
        
        return instance;
    }
    
    
    /// -------------------------
    private FileNameCache() {
        super();
    }
    
}
