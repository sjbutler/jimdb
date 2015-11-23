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
 * Interface for {@code KeyStore}s. This interface and its implementors
 * are gradually being deprecated.
 *
 */
interface DatabaseKeyStore {

    /**
     * Stores a string and a database key.
     * @param key a string
     * @param value a database key
     * @return {@code null} unless the key was previously associated another value
     */
    public Integer put(String key, Integer value);

    /**
     * Retrieved the database key used for the string.
     * @param key a string
     * @return a database key associated with the string, or {@code null} if 
     * there string is unknown
     */
    public Integer get(String key);
}
