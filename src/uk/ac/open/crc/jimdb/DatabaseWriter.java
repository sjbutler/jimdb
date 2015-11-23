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
 * Provides an API for writing to the database.
 *
 */
public class DatabaseWriter {

    // package private to control the means of instantiation
    DatabaseWriter() {
    }

    /**
     * Stores the declaration recorded in the {@linkplain RawProgramEntity} to 
     * the database.
     * @param programEntity a declaration
     */
    public void store( RawProgramEntity programEntity ) {
        EntityDatabaseWriter writer = new EntityDatabaseWriter();
        writer.store( programEntity );
    }
}
