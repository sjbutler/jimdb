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

import java.sql.SQLException;

/**
 * Manages the instantiation and configuration of the database.
 *
 */
public class DatabaseManager {
    // TODO
    // add some safety nets such as ensuring a database is not opened
    // twice and that only one database is open at any one time.
    // Though Derby does police that.
    /**
     * Initialises the database at the given location creating a new database
     * if it does not already exist. 
     * 
     * @param databaseLocation a {@code String} containing a database path.
     * @throws java.sql.SQLException if the database library fails to initialise
     *  the database
     */
    public static void initialiseAndCreate( String databaseLocation ) 
            throws SQLException {
        EntityDatabaseManager.openDatabaseWithCreation(databaseLocation);
    }
    
    /**
     * Initialises the database at the given location. The database is 
     * initialised with read only access. 
     * 
     * @param databaseLocation The location of a database. This should be a path name.
     * @throws java.sql.SQLException if the database library fails to initialise
     *   the database
     */
    public static void initialise(String databaseLocation) throws SQLException {
        EntityDatabaseManager.openDatabase(databaseLocation);
    }
        
    /**
     * Set the name of the current project when writing to the database.
     * @param projectName a project name
     */
    public static void setProjectName(String projectName) {
        JimDbConfiguration.getInstance().setProjectName(projectName);
    }
    
    /**
     * Set the version of the current project when writing to the database.
     * @param projectVersion a project version
     */
    public static void setProjectVersion(String projectVersion) {
        JimDbConfiguration.getInstance().setProjectVersion(projectVersion);
    }
    
    /**
     * Sets the tokeniser to a more aggressive mode.
     */
    public static void setInttRecursiveSplitOn() {
        InttConfiguration.getInstance().setResursiveSplitOn();
    }
    
    /**
     * Sets to the tokeniser to expand contractions of negated modal verbs.
     * For example, "cant" is expanded to "can not", which helps part of
     * speech tagging.
     */
    public static void setInttModalExpansionOn() {
        InttConfiguration.getInstance().setModalExpansionOn();
    }

    // Review the need for this later -- deeper understanding of slf4j 
    // will probably help
    public static void setLoggingLevel( String level ) {
        JimDbConfiguration.getInstance().setLoggingLevel( level );
        InttConfiguration.getInstance().setLoggingLevel( level );
    }
    
    /**
     * Shut the database down allowing any queued write operations to be 
     * completed first.
     * @throws SQLException if the database shutdown fails
     */
    public static void shutdown() throws SQLException {
        EntityDatabaseManager.shutdown();
    }
}
