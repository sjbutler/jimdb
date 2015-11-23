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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.derby.jdbc.BasicEmbeddedDataSource40;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;

/**
 * Underlying database manager.
 *
 */
class EntityDatabaseManager {
     private static final Logger LOGGER = 
             LoggerFactory.getLogger( EntityDatabaseManager.class );

    // the following constants are available within the package for use
    // by the DatabaseWriter class
     
    // schema
    static final String SCHEMA = "SVM";

    // table names
    static final String IDENTIFIER_NAMES_TABLE = "IDENTIFIER_NAMES";
    static final String COMPONENT_WORDS_TABLE = "COMPONENT_WORDS";
    static final String COMPONENT_WORDS_XREF_TABLE = "COMPONENT_WORDS_XREFS";
    static final String PROGRAM_ENTITIES_TABLE = "PROGRAM_ENTITIES";
    static final String PROJECT_TABLE = "PROJECTS";
    static final String PACKAGES_TABLE = "PACKAGES";
    static final String PACKAGE_NAMES_TABLE = "PACKAGE_NAMES";
    static final String SPECIES_TABLE = "SPECIES";
    static final String TYPE_NAMES_TABLE = "TYPE_NAMES";
//    static final String TYPE_NAMES_HARD_WORDS_XREF_TABLE = "TYPE_NAMES_HARD_WORDS_XREF";
    static final String METHOD_SIGNATURES_TABLE = "METHOD_SIGNATURES";
    static final String MODIFIERS_TABLE = "MODIFIERS";
    static final String MODIFIERS_XREF_TABLE = "MODIFIERS_XREF";
    static final String SUPER_CLASS_XREF_TABLE = "SUPER_CLASS_XREF";
    static final String SUPER_TYPE_XREF_TABLE = "SUPER_TYPE_XREF";
    static final String TYPE_ARGUMENT_XREF = "TYPE_ARGUMENT_XREF";
    
//    static final String EXTENDS_INTERFACE_TABLE = "EXTENDS_INTERFACE";
//    static final String IMPLEMENTS_INTERFACE_TABLE = "IMPLEMENTS_INTERFACE";
//    static final String EXTENDS_CLASS_TABLE = "EXTENDS_CLASS";

    static final String FILE_NAMES_TABLE = "FILES";
    
    private static final String SQL_CREATE_IDENTIFIER_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE
            + "("
            + "identifier_name VARCHAR(255) NOT NULL, "   // need longer?
            + "identifier_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY "
            + ")";

    private static final String SQL_CREATE_TYPE_NAMES_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + TYPE_NAMES_TABLE
            + "("
            + "type_name VARCHAR(255) NOT NULL, "
            + "type_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "identifier_name_key_fk INT REFERENCES " 
              + SCHEMA + "."  + IDENTIFIER_NAMES_TABLE + "(identifier_name_key) "
            + ")";

    private static final String SQL_FILE_NAMES_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + FILE_NAMES_TABLE
            + "("
            + "file_name VARCHAR(255) NOT NULL, "
            + "file_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY "
            + ")";

    private static final String SQL_CREATE_COMPONENT_WORDS_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + COMPONENT_WORDS_TABLE
            + "("
            + "component_word VARCHAR(255), " // accommodates long single case components
            + "component_word_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY "
            + ")";

    private static final String SQL_CREATE_COMPONENT_WORDS_XREF_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + COMPONENT_WORDS_XREF_TABLE
            + "("
            + "component_word_key_fk INT REFERENCES " + SCHEMA + "." 
              + COMPONENT_WORDS_TABLE + "(component_word_key), "
            + "identifier_name_key_fk INT REFERENCES " + SCHEMA  + "." 
              + IDENTIFIER_NAMES_TABLE + "(identifier_name_key), "
            + "position INT NOT NULL "     // i.e. which token is it
            + ")";

    private static final String SQL_CREATE_PROGRAM_ENTITIES_TABLE = 
            "CREATE TABLE "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + "("
            + "program_entity_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "project_key_fk INT REFERENCES " 
              + SCHEMA + "." + PROJECT_TABLE + "(project_key), "
            + "package_key_fk INT REFERENCES " 
              + SCHEMA + "." + PACKAGES_TABLE + "(package_key), "  // may be too short
            + "identifier_name_key_fk INT REFERENCES " 
              + SCHEMA + "." + IDENTIFIER_NAMES_TABLE + "(identifier_name_key), "
            + "container_uid VARCHAR(255), "        // allows the use of sha512 (and more)
            + "entity_uid VARCHAR(255), "
            + "species_name_key_fk INT REFERENCES " 
              + SCHEMA + "." + SPECIES_TABLE + "(species_name_key), "
            + "type_name_key_fk INT REFERENCES " 
              + SCHEMA + "." + TYPE_NAMES_TABLE + "(type_name_key), "
            + "method_signature_key_fk INT, " // this does reference the method signatures table, but can also be zero REFERENCES " + SCHEMA + "." + METHOD_SIGNATURES_TABLE + "(method_signature_key), "
            + "is_anonymous BOOLEAN NOT NULL, "
            + "file_name_key_fk INT REFERENCES " 
              + SCHEMA + "." + FILE_NAMES_TABLE + "(file_name_key), " 
            + "is_array BOOLEAN NOT NULL, "
            + "is_loop_control_var BOOLEAN NOT NULL, "
            + "start_line_number INT, "
            + "start_column INT, "
            + "end_line_number INT, "
            + "end_column INT"
            + ")";
        
    // REVIEW this solution
    // package tables
    private static final String SQL_CREATE_PACKAGE_NAMES_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + PACKAGE_NAMES_TABLE
            + "("
            + "package_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "package_name VARCHAR(255)"
            + ")";

    private static final String SQL_CREATE_PACKAGE_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + PACKAGES_TABLE
            + "("
            + "package_name_key_fk INT REFERENCES " 
              + SCHEMA + "." + PACKAGE_NAMES_TABLE +"(package_name_key), "
            + "package_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "project_key_fk INT REFERENCES " 
              + SCHEMA + "." + PROJECT_TABLE + "(project_key)"
            + ")";


    // project table
    private static final String SQL_CREATE_PROJECT_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + PROJECT_TABLE
            + "("
            + "project_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "project_name VARCHAR(255), "
            + "project_version VARCHAR(255)"  // both columns are excessively wide
            + ")";

    // method signatures
    private static final String SQL_CREATE_METHOD_SIGNATURES_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + METHOD_SIGNATURES_TABLE
            + "("
            + "method_signature_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "method_signature VARCHAR(2048)" // excessive, but accommodates worst cases observed
            + ")";

    // read only table to reduce redundancy
    private static final String SQL_CREATE_SPECIES_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + SPECIES_TABLE
            + "("
            + "species_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "species_name VARCHAR(20)"
            + ")";

    // SUPER CLASS XREF
    private static final String SQL_CREATE_SUPER_CLASS_XREF_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + SUPER_CLASS_XREF_TABLE
            + "("
            + "sub_class_entity_key_fk INT REFERENCES " 
              + SCHEMA + "." + PROGRAM_ENTITIES_TABLE + "(program_entity_key), "
            + "super_class_name_key_fk INT REFERENCES "
              + SCHEMA + "." + TYPE_NAMES_TABLE + "(type_name_key) "
            + ")";
    
    // SUPER_TYPE_XREF
    private static final String SQL_CREATE_SUPER_TYPE_XREF_TABLE =
            "CREATE TABLE "
            + SCHEMA + "." + SUPER_TYPE_XREF_TABLE
            + "("
            + "sub_type_entity_key_fk INT REFERENCES " 
              + SCHEMA + "." + PROGRAM_ENTITIES_TABLE + "(program_entity_key), "
            + "super_type_name_key_fk INT REFERENCES "
              + SCHEMA + "." + TYPE_NAMES_TABLE + "(type_name_key) "
            + ")";
    
    private static final String SQL_CREATE_FILE_NAMES_TABLE = 
            "CREATE TABLE "
            + SCHEMA + "." + FILE_NAMES_TABLE
            + "("
            + "file_name_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " 
            + "file_name VARCHAR(255)"
            + ")";
        
    private static final String SQL_CREATE_MODIFIERS_TABLE = 
            "CREATE TABLE "
            + SCHEMA + "." + MODIFIERS_TABLE
            + "("
            + "modifier VARCHAR(20), "
            + "modifier_key INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY"
            +")";

    private static final String SQL_CREATE_MODIFIERS_XREF_TABLE = 
            "CREATE TABLE "
            + SCHEMA + "." + MODIFIERS_XREF_TABLE
            + "("
            + "modifier_key_fk INT REFERENCES " 
              + SCHEMA + "." + MODIFIERS_TABLE + "(modifier_key), "
            + "program_entity_key_fk INT REFERENCES " 
              + SCHEMA + "." + PROGRAM_ENTITIES_TABLE + "(program_entity_key)"
            + ")";
    

    private static BasicEmbeddedDataSource40 dataSource = null;

    private static Connection connection = null;


    /// ------- statements and prepared statements -------------

    // write methods
    // this may be entirely redundant
    private static final String IDENTIFIER_NAME_QUERY_STATEMENT =
            "SELECT identifier_name_key FROM "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE
            + " WHERE identifier_name = ?";

    // review and revise
    private static final String IDENTIFIER_NAME_INSERT_STATEMENT =
            "INSERT INTO "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE
            + "(identifier_name) "
            + " VALUES(?)";

    private static final String TYPE_NAME_INSERT_STATEMENT =
            "INSERT INTO "
            + SCHEMA + "." + TYPE_NAMES_TABLE
            + "(type_name, identifier_name_key_fk) "
            + "VALUES(?, ?)";

    private static final String COMPONENT_WORD_QUERY_STATEMENT =
            "SELECT COMPONENT_word_key FROM "
                    + SCHEMA + "." + COMPONENT_WORDS_TABLE
                    + " WHERE COMPONENT_word = ?";

    private static final String COMPONENT_WORD_INSERT_STATEMENT =
            "INSERT INTO " + SCHEMA + "." + COMPONENT_WORDS_TABLE
            + "(component_word) VALUES(?)";

    private static final String COMPONENT_WORD_XREF_INSERT_STATEMENT =
            "INSERT INTO "
            + SCHEMA + "." + COMPONENT_WORDS_XREF_TABLE
            + "(component_word_key_fk, identifier_name_key_fk, position)"
            + " VALUES(?, ?, ?)";

    private static final String PROJECT_INSERT_STATEMENT =
            "INSERT INTO " + SCHEMA + "." + PROJECT_TABLE
            + "(project_name, project_version)"
            +" VALUES(?, ?)";

    private static final String PACKAGE_NAME_INSERT_STATEMENT =
            "INSERT INTO " + SCHEMA + "." + PACKAGE_NAMES_TABLE
            + "(package_name)"
            +" VALUES(?)";
            
    private static final String PACKAGE_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + PACKAGES_TABLE
            + "(project_key_fk, package_name_key_fk)"
            + " VALUES(?, ?)";
    
    // insert super class
    private static final String SUPER_CLASS_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + SUPER_CLASS_XREF_TABLE
            + "(sub_class_entity_key_fk, super_class_name_key_fk)"
            + " VALUES(?,?)";
    
    // insert super type
    private static final String SUPER_TYPE_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + SUPER_TYPE_XREF_TABLE
            + "(sub_type_entity_key_fk, super_type_name_key_fk)"
            + " VALUES(?,?)";
    

    // store method name signature
    private static final String METHOD_SIGNATURE_INSERT_STATEMENT =
            "INSERT INTO " + SCHEMA + "." + METHOD_SIGNATURES_TABLE
            + "(method_signature)"
            + " VALUES(?)";

    private static final String MODIFIER_XREF_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + MODIFIERS_XREF_TABLE
            + "(modifier_key_fk, program_entity_key_fk)"
            + " VALUES(?,?)";
    
    private static final String PROGRAM_ENTITY_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + "(project_key_fk, package_key_fk, identifier_name_key_fk, "
            + "container_uid, entity_uid, species_name_key_fk, "
            + "type_name_key_fk, "
            + "method_signature_key_fk, is_anonymous, file_name_key_fk, "
            + "is_array, is_loop_control_var, "
            + "start_line_number, start_column, end_line_number, end_column)"
            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    private static final String FILE_NAME_INSERT_STATEMENT = 
            "INSERT INTO " + SCHEMA + "." + FILE_NAMES_TABLE
            + "(file_name)"
            + " VALUES(?)";
    
    // read statements
    private static final String PACKAGE_NAME_QUERY =
            "SELECT package_name FROM "
            + SCHEMA + "." + PACKAGE_NAMES_TABLE
            + " WHERE package_name_key=?";

    private static final String IDENTIFIER_NAME_BY_SPECIES_QUERY =
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE species_name_key_fk=?";

    private static final String IDENTIFIER_NAME_QUERY =
            "SELECT identifier_name FROM "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE
            + " WHERE identifier_name_key=?";

    private static final String COMPONENT_WORDS_XREF_QUERY =
            "SELECT component_word_key_fk, position FROM "
            + SCHEMA + "." + COMPONENT_WORDS_XREF_TABLE
            + " WHERE identifier_name_key_fk = ?";

    private static final String COMPONENT_WORD_QUERY =
            "SELECT component_word FROM "
            + SCHEMA + "." + COMPONENT_WORDS_TABLE
            + " WHERE component_word_key = ?";

    private static final String PROJECTS_QUERY_STATEMENT =
            "SELECT * FROM "
            + SCHEMA + "." + PROJECT_TABLE;

    private static final String SUPER_CLASS_QUERY =
            "SELECT super_class_name_key_fk FROM "
            + SCHEMA + "." + SUPER_CLASS_XREF_TABLE
            + " WHERE sub_class_entity_key_fk=?";

    private static final String SUPER_TYPE_QUERY =
            "SELECT super_type_name_key_fk FROM "
            + SCHEMA + "." + SUPER_TYPE_XREF_TABLE
            + " WHERE sub_type_entity_key_fk=?";

    private static final String TYPE_NAME_QUERY =
            "SELECT type_name FROM "
            + SCHEMA + "." + TYPE_NAMES_TABLE
            + " WHERE type_name_key=?";

    private static final String TYPE_NAME_IDENTIFIER_QUERY =
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + TYPE_NAMES_TABLE
            + " WHERE type_name_key=?";
    
    private static final String TYPE_NAME_KEY_BY_IDENTIFIER_NAME_KEY_QUERY = 
            "SELECT type_name_key FROM "
            + SCHEMA + "." + TYPE_NAMES_TABLE
            + " WHERE identifier_name_key_fk=?";
    
    private static final String IDENTIFIER_NAME_KEY_QUERY =
            "SELECT identifier_name_key FROM "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE
            + " WHERE identifier_name=?";

    private static final String COMPONENT_WORD_KEY_QUERY_STATEMENT =
            "SELECT component_word_key FROM "
            + SCHEMA + "." + COMPONENT_WORDS_TABLE
            + " WHERE component_word = ?";

    private static final String FILE_NAME_QUERY = 
            "SELECT file_name_key FROM "
            + SCHEMA + "." + FILE_NAMES_TABLE
            + " WHERE file_name = ?";
    
    private static final String PACKAGE_NAME_KEYS_FOR_PROJECT_QUERY = 
            "SELECT package_name_key_fk FROM "  
            + SCHEMA + "." + PACKAGES_TABLE 
            + " WHERE project_key_fk = ?";
    
    private static final String PACKAGE_NAME_KEY_QUERY = 
            "SELECT package_name_key_fk FROM "  
            + SCHEMA + "." + PACKAGES_TABLE 
            + " WHERE package_key = ?";
    
    private static final String NAMED_PACKAGE_KEY_QUERY = 
            "SELECT package_key FROM "  
            + SCHEMA + "." + PACKAGES_TABLE 
            + " WHERE project_key_fk = ? AND package_name_key_fk = ?";
    
    private static final String PROGRAM_ENTITY_BY_SPECIES_QUERY = 
            "SELECT * FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE species_name_key_fk = ?";
    
    private static final String PROGRAM_ENTITY_BY_PROJECT_QUERY = 
            "SELECT * FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE project_key_fk = ?";
    
    private static final String CLASS_NAME_KEYS_FOR_PACKAGE_QUERY = 
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE project_key_fk = ? AND package_key_fk = ? "
            + "AND species_name_key_fk = ?"; // DO NOT hard code the species key
    
        private static final String MODIFIER_KEYS_QUERY = 
            "SELECT modifier_key_fk FROM "
            + SCHEMA + "." + MODIFIERS_XREF_TABLE   
            + " WHERE program_entity_key_fk = ?";
    
    private static final String ALL_CLASS_DATA_FOR_PROJECT_QUERY = 
            "SELECT * FROM " 
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE species_name_key_fk = ? "
            + " AND project_key_fk = ?";
    
    private static final String ALL_NAMES_FOR_SPECIES_QUERY = 
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE species_name_key_fk = ?";
    
    private static final String ALL_NAMES_FOR_SPECIES_BY_PROJECT_QUERY = 
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE species_name_key_fk = ?" 
            + " AND project_key_fk = ?";
    
    private static final String ALL_NAMES_FOR_PROJECT_QUERY = 
            "SELECT identifier_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE project_key_fk = ?";
    
    private static final String ALL_NAMES_QUERY = 
            "SELECT identifier_name_key FROM "
            + SCHEMA + "." + IDENTIFIER_NAMES_TABLE;

    private static final String ALL_IDENTIFIER_DATA_FOR_PROJECT =
            "SELECT identifier_name_key_fk, species_name_key_fk, type_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE project_key_fk = ?";
    
    private static final String ALL_PROGRAM_ENTITIES_BY_SPECIES_FOR_PROJECT_QUERY = 
            "SELECT * FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE 
            + " WHERE project_key_fk = ? AND species_name_key_fk = ?";

    private static final String PROJECT_DETAILS_QUERY = 
            "SELECT project_name, project_version FROM "
            + SCHEMA + "." + PROJECT_TABLE
            + " WHERE project_key = ?";
    
    // ------------- mwnci queries
    
    // TODO -- this is probably correct, but may require revision
    // the chief concerns are the fields returned.
    // and the specification of the species as CLASS or INTERFACE, which is brittle
    private static final String CLASS_OR_INTERFACE_FOR_FQN_QUERY = 
            "SELECT program_entity_key, container_uid, entity_uid, type_name_key_fk, "
            + "project_key_fk, "
            + "file_name_key_fk, start_line_number, start_column, "
            + "end_line_number, end_column, "
            + "species_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE project_key_fk = ? AND package_key_fk = ? AND identifier_name_key_fk = ?"
            + " AND species_name_key_fk in (3,10)"; // brittle -- may need more generic method for injecting species key value
    
    private static final String ENTITY_CANDIDATES_FOR_NAME_QUERY =
            "SELECT program_entity_key, project_key_fk, package_key_fk, "
            + "container_uid, entity_uid, type_name_key_fk, "
            + "file_name_key_fk, start_line_number, start_column, "
            + "end_line_number, end_column, "
            + "species_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE identifier_name_key_fk = ? and species_name_key_fk = ?"; 
            
            
    private static final String SUB_CLASS_KEY_QUERY = 
            "SELECT sub_class_entity_key_fk FROM "
            + SCHEMA + "." + SUPER_CLASS_XREF_TABLE
            + " WHERE super_class_name_key_fk = ?";
    
    private static final String SUB_TYPE_KEY_QUERY = 
            "SELECT sub_type_entity_key_fk FROM "
            + SCHEMA + "." + SUPER_TYPE_XREF_TABLE
            + " WHERE super_type_name_key_fk = ?";
    
    private static final String INHERITABLE_ENTITY_BY_KEY_QUERY = 
            "SELECT identifier_name_key_fk, project_key_fk, package_key_fk, "
            + "container_uid, entity_uid, type_name_key_fk, "
            + "file_name_key_fk, start_line_number, start_column, "
            + "end_line_number, end_column, "
            + "species_name_key_fk FROM "
            + SCHEMA + "." + PROGRAM_ENTITIES_TABLE
            + " WHERE program_entity_key = ?"; 
            
    // ------------------------------------------
    
    static PreparedStatement sqlFileNameInsert = null;
    static PreparedStatement sqlFileNameQuery = null;
    
    static PreparedStatement sqlProgramEntityInsert = null;
    
    static PreparedStatement sqlIdentifierNameQuery = null;
    static PreparedStatement sqlIdentifierNameInsert = null;
    static PreparedStatement sqlIdentifierMetadataInsert = null;

    static PreparedStatement sqlComponentWordQuery = null;
    static PreparedStatement sqlComponentWordInsert = null;

    static PreparedStatement sqlComponentWordXrefInsert = null;

    static PreparedStatement sqlProjectInsert = null;

    static PreparedStatement sqlPackageInsert = null;
    static PreparedStatement sqlPackageNameInsert = null;
    
    static PreparedStatement sqlTypeNameInsert = null;

    static PreparedStatement sqlUpdateContainer = null;

    static PreparedStatement sqlMethodSignatureInsert = null;
    static PreparedStatement sqlMethodSignatureXrefInsert = null;

    static PreparedStatement sqlSuperClassInsert = null;
    static PreparedStatement sqlSuperTypeInsert = null;
    static PreparedStatement sqlImplementsInterfaceInsert = null;

    static PreparedStatement sqlTypeNameHardWordXrefInsert = null;
    
    static PreparedStatement sqlModifierXrefInsert = null;
    
    
    // Reader query statements
    static PreparedStatement sqlIdentifierNameBySpeciesQuery = null;

    static PreparedStatement sqlComponentWordKeyQuery = null;
    static PreparedStatement sqlComponentWordsXrefQuery = null;

    static PreparedStatement sqlLocationByTypeAndProjectQuery = null;

    static PreparedStatement sqlPackageNameKeysForProjectQuery = null;
    static PreparedStatement sqlPackageNameQuery = null;
    static PreparedStatement sqlPackageNameKeyQuery = null;
    static PreparedStatement sqlNamedPackageKeyQuery = null;
    
    
    static PreparedStatement sqlProjectsQuery = null;

    static PreparedStatement sqlSuperClassQuery = null;
    static PreparedStatement sqlSuperTypeQuery = null;
    static PreparedStatement sqlTypeNameQuery = null;
    static PreparedStatement sqlTypeNameIdentifierQuery = null;
    static PreparedStatement sqlTypeNameHardWordsXrefQuery = null;

    static PreparedStatement sqlModifiersXrefQuery = null;
    static PreparedStatement sqlClassNameKeysForPackageQuery = null;
    static PreparedStatement sqlMethodReturnTypeQuery = null;
    static PreparedStatement sqlMethodModifiersQuery = null;
    
    static PreparedStatement sqlLocalVariablesQuery = null;
    static PreparedStatement sqlMethodsQuery = null;
    static PreparedStatement sqlFieldsQuery = null;

    static PreparedStatement sqlImplementedInterfacesNamesQuery = null;
    static PreparedStatement sqlSuperClassNameQuery = null;
    static PreparedStatement sqlExtendedInterfaceNamesQuery = null;
    
    static PreparedStatement sqlClassNameKeysForPackageInProjectQuery = null;
    
    static PreparedStatement sqlProgramEntitiesBySpeciesQuery = null;
    
    // for extracting corpora
    static PreparedStatement sqlAllNamesForSpeciesQuery = null;
    static PreparedStatement sqlAllNamesForSpeciesByProjectQuery = null;
    static PreparedStatement sqlAllNamesForSpeciesByProjectAndModifierQuery = null;
    static PreparedStatement sqlAllNamesForProjectQuery = null;
    static PreparedStatement sqlAllNamesQuery = null;
    
    // for class analysis
    static PreparedStatement sqlAllClassDataQuery = null;
    
    // for survey
    static PreparedStatement sqlAllIdentifierDataQuery = null;
    
    static PreparedStatement sqlAllEntitiesBySpeciesQuery = null;
    static PreparedStatement sqlAllEntitiesByProjectQuery = null;
    
    // for mwnci
    static PreparedStatement sqlClassOrInterfaceForFqnQuery = null;
    static PreparedStatement sqlEntityCandidatesForNameQuery = null;
    
    static PreparedStatement sqlTypeNameKeyByIdentifierNameKeyQuery = null;
    static PreparedStatement sqlSubClassKeyQuery = null;
    static PreparedStatement sqlSubTypeKeyQuery = null;
    static PreparedStatement sqlInheritableProgramEntityQuery = null;
    
    static PreparedStatement sqlProjectDetailsQuery = null;
    
    // ------- database tuning settings ---------------------------
    private static final String derbySetPageSize =
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY"
            + "('derby.storage.pageSize','32768')";

    // default is 1,000
    private static final String derbySetPageCacheSize =
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY"
            + "('derby.storage.pageCacheSize','40000')";

    /**
     * Obtains a database connection. The database should have
     * been initialised previously.
     *
     * @return The connection to the database,
     * or <code>null</code> if the database is uninitialised.
     */
    synchronized static Connection getConnection() {
        if (dataSource == null) {
            LOGGER.error( "Database not initialised: connection unavailable." );
        }

        return connection;
    }


    /**
     * Initialises the database connection. The exception is used as
     * a diagnostic for the developer/user.
     *
     * @throws SQLException There are many reasons why the exception
     * is thrown - including a mis-identified database location
     * and SQL syntax errors - and there is no reason why the application
     * should try to recover from any of them when the programmer can fix them.
     */
    static synchronized void openDatabase(String databaseLocation) 
            throws SQLException {
        // do nothing if database is already initialised
        if (dataSource == null) {
            LOGGER.info( "Connecting to database at: {}", databaseLocation);
            dataSource = new BasicEmbeddedDataSource40();
            dataSource.setDatabaseName(databaseLocation);

            // NEED TO review and revise following when moving to multi-threaded
            // DataBaseWriters
            connection = dataSource.getConnection();

            // set the derby values for page size and page cache size
            connection.prepareStatement(derbySetPageCacheSize).execute();
            connection.prepareStatement(derbySetPageSize).execute();

            // and switch off the auto-commit
            connection.setAutoCommit(false);

            createReaderPreparedStatements();

            buildCaches();
          
        }
    }
    
    
    /**
     * Initialises the database connection. The exception is used as
     * a diagnostic for the developer/user.
     *
     * @throws SQLException There are many reasons why the exception
     * is thrown - including a mis-identified database location
     * and SQL syntax errors - and there is no reason why the application
     * should try to recover from any of them when the programmer can fix them.
     */
    static synchronized void openDatabaseWithCreation(String databaseLocation) 
            throws SQLException {
        // do nothing if database is already initialised
        if (dataSource == null) {
            LOGGER.info( "Connecting to database at: {}", databaseLocation);
            dataSource = new BasicEmbeddedDataSource40();
            dataSource.setDatabaseName(databaseLocation);
            dataSource.setCreateDatabase("create");

            // NEED TO review and revise following when moving to multi-threaded
            // DataBaseWriters
            connection = dataSource.getConnection();

            // set the derby values for page size and page cache size
            connection.prepareStatement(derbySetPageCacheSize).execute();
            connection.prepareStatement(derbySetPageSize).execute();

            // need to check that the tables exist in the database,
            // and create them if necessary
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet resultSet = metaData.getTables(null,
                    SCHEMA,
                    IDENTIFIER_NAMES_TABLE,
                    null);
            if (resultSet.next() != true) {
                // i.e. no results were returned
                // Is the lack of one key table sufficient to
                // determine that there are no tables present?
                createTables();
            }

            // and switch off the auto-commit
            connection.setAutoCommit(false);
 
            createWriterPreparedStatements();
            
            buildCaches();
        }
    }

    private static void buildCaches() {
            cacheIdentifierNamesAndKeys();
            
            cacheTokensAndKeys();
            
            cacheMethodSignatures();
            
            cacheTypeNames();
            
            cacheSpeciesKeys();
            cachePackageNameKeys();
            cacheModifierKeys();
            
            cacheFileNames();
            
            cacheSpeciesKeys();
            cachePackageNameKeys();
            cacheModifierKeys();
            cacheProjectKeys();
            cacheFileNames();
    }
    
    private static void createWriterPreparedStatements() throws SQLException {

        sqlIdentifierNameQuery =
                connection.prepareStatement(IDENTIFIER_NAME_QUERY_STATEMENT);
        sqlIdentifierNameInsert = connection.prepareStatement(
                IDENTIFIER_NAME_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);

        sqlTypeNameInsert = connection.prepareStatement(
                TYPE_NAME_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);


        sqlComponentWordQuery = connection.prepareStatement(COMPONENT_WORD_QUERY_STATEMENT);
        sqlComponentWordInsert = connection.prepareStatement(
                COMPONENT_WORD_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);

        sqlComponentWordXrefInsert = connection.prepareStatement(
                COMPONENT_WORD_XREF_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);


        sqlProjectInsert = connection.prepareStatement(
                PROJECT_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);

        sqlPackageInsert = connection.prepareStatement(
                PACKAGE_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);

        sqlPackageNameInsert = connection.prepareStatement(
                PACKAGE_NAME_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);

        sqlMethodSignatureInsert = connection.prepareStatement(
                METHOD_SIGNATURE_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);


        sqlFileNameQuery = connection.prepareStatement(FILE_NAME_QUERY);// used while writing to prevent duplicates
        sqlFileNameInsert = connection.prepareStatement(
                FILE_NAME_INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS);
        
        sqlSuperClassInsert = connection.prepareStatement(SUPER_CLASS_INSERT_STATEMENT);
        sqlSuperTypeInsert = connection.prepareStatement(SUPER_TYPE_INSERT_STATEMENT);

        sqlModifierXrefInsert = connection.prepareStatement(
                MODIFIER_XREF_INSERT_STATEMENT, 
                Statement.RETURN_GENERATED_KEYS);
        
        sqlProgramEntityInsert = connection.prepareStatement(
                PROGRAM_ENTITY_INSERT_STATEMENT, 
                Statement.RETURN_GENERATED_KEYS);
   }

    // Only prepared statements needed for the reader
    private static void createReaderPreparedStatements() throws SQLException {
        
            sqlIdentifierNameQuery =
                    connection.prepareStatement(IDENTIFIER_NAME_QUERY_STATEMENT);
            sqlIdentifierNameInsert = connection.prepareStatement(
                    IDENTIFIER_NAME_INSERT_STATEMENT,
                    Statement.RETURN_GENERATED_KEYS);

            sqlIdentifierNameBySpeciesQuery = connection.prepareStatement(IDENTIFIER_NAME_BY_SPECIES_QUERY);


            sqlComponentWordKeyQuery = connection.prepareStatement(COMPONENT_WORD_KEY_QUERY_STATEMENT);
            sqlComponentWordQuery = connection.prepareStatement(COMPONENT_WORD_QUERY);
            sqlComponentWordInsert = connection.prepareStatement(
                    COMPONENT_WORD_INSERT_STATEMENT,
                    Statement.RETURN_GENERATED_KEYS);

            sqlComponentWordXrefInsert = connection.prepareStatement(
                    COMPONENT_WORD_XREF_INSERT_STATEMENT,
                    Statement.RETURN_GENERATED_KEYS);

            sqlComponentWordsXrefQuery = connection.prepareStatement(COMPONENT_WORDS_XREF_QUERY);

            sqlProjectInsert = connection.prepareStatement(
                    PROJECT_INSERT_STATEMENT,
                    Statement.RETURN_GENERATED_KEYS);

            sqlPackageInsert = connection.prepareStatement(
                    PACKAGE_INSERT_STATEMENT,
                    Statement.RETURN_GENERATED_KEYS);

            sqlProjectsQuery = connection.prepareStatement(PROJECTS_QUERY_STATEMENT);
            sqlPackageNameKeysForProjectQuery = connection.prepareStatement(PACKAGE_NAME_KEYS_FOR_PROJECT_QUERY);
            sqlPackageNameQuery = connection.prepareStatement(PACKAGE_NAME_QUERY);
            sqlPackageNameKeyQuery = connection.prepareStatement( PACKAGE_NAME_KEY_QUERY );

            sqlTypeNameQuery = connection.prepareStatement(TYPE_NAME_QUERY);
            sqlTypeNameIdentifierQuery = connection.prepareStatement( TYPE_NAME_IDENTIFIER_QUERY );
            sqlSuperClassQuery = connection.prepareStatement(SUPER_CLASS_QUERY);
            sqlSuperTypeQuery = connection.prepareStatement(SUPER_TYPE_QUERY);

            sqlNamedPackageKeyQuery = connection.prepareStatement( NAMED_PACKAGE_KEY_QUERY );
            
            sqlClassNameKeysForPackageInProjectQuery = connection.prepareStatement( CLASS_NAME_KEYS_FOR_PACKAGE_QUERY );
                    
            sqlModifiersXrefQuery = connection.prepareStatement(MODIFIER_KEYS_QUERY);
                        
            sqlAllClassDataQuery = connection.prepareStatement( ALL_CLASS_DATA_FOR_PROJECT_QUERY );
            
            sqlAllNamesForSpeciesQuery = connection.prepareStatement(ALL_NAMES_FOR_SPECIES_QUERY);
            sqlAllNamesForSpeciesByProjectQuery = connection.prepareStatement(ALL_NAMES_FOR_SPECIES_BY_PROJECT_QUERY);
            sqlAllNamesForProjectQuery = connection.prepareStatement(ALL_NAMES_FOR_PROJECT_QUERY);
            sqlAllNamesQuery = connection.prepareStatement(ALL_NAMES_QUERY);
            
            sqlAllIdentifierDataQuery = connection.prepareStatement(ALL_IDENTIFIER_DATA_FOR_PROJECT);
            
            sqlProgramEntitiesBySpeciesQuery = connection.prepareStatement( ALL_PROGRAM_ENTITIES_BY_SPECIES_FOR_PROJECT_QUERY );
            
            sqlClassOrInterfaceForFqnQuery = connection.prepareStatement( CLASS_OR_INTERFACE_FOR_FQN_QUERY );
            sqlEntityCandidatesForNameQuery = connection.prepareStatement( ENTITY_CANDIDATES_FOR_NAME_QUERY );
            sqlTypeNameKeyByIdentifierNameKeyQuery = connection.prepareStatement( TYPE_NAME_KEY_BY_IDENTIFIER_NAME_KEY_QUERY );
            sqlSubClassKeyQuery = connection.prepareStatement( SUB_CLASS_KEY_QUERY );
            sqlSubTypeKeyQuery = connection.prepareStatement( SUB_TYPE_KEY_QUERY );
            sqlInheritableProgramEntityQuery = connection.prepareStatement( INHERITABLE_ENTITY_BY_KEY_QUERY );
            
            sqlProjectDetailsQuery = connection.prepareStatement( PROJECT_DETAILS_QUERY );
            
            sqlAllEntitiesBySpeciesQuery = connection.prepareStatement( PROGRAM_ENTITY_BY_SPECIES_QUERY );
            sqlAllEntitiesByProjectQuery = connection.prepareStatement( PROGRAM_ENTITY_BY_PROJECT_QUERY );
    }


    // NB - need to ensure that all transactions have completed before
    // this acts.
    static synchronized void shutdown() {
        LOGGER.info("Shutting down database.");
        dataSource.setShutdownDatabase("shutdown");
    }


    // create the data tables and populate the read only tables
    private synchronized static void createTables() throws SQLException {
        LOGGER.info("Creating database tables.");
        // create the identifier name table
        PreparedStatement statement = connection.prepareStatement(SQL_CREATE_IDENTIFIER_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_TYPE_NAMES_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_SPECIES_TABLE);
        statement.execute();
        // populate table
        populateSpeciesTable();

        statement = connection.prepareStatement(SQL_CREATE_METHOD_SIGNATURES_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_PROJECT_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_PACKAGE_NAMES_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_PACKAGE_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_FILE_NAMES_TABLE);
        statement.execute();
        
        statement = connection.prepareStatement(SQL_CREATE_PROGRAM_ENTITIES_TABLE);
        statement.execute();

        
        // inheritance hierarchy tables
        statement = connection.prepareStatement(SQL_CREATE_SUPER_CLASS_XREF_TABLE);
        statement.execute();

        statement = connection.prepareStatement(SQL_CREATE_SUPER_TYPE_XREF_TABLE);
        statement.execute();

        // create the hard words table
        statement = connection.prepareStatement(SQL_CREATE_COMPONENT_WORDS_TABLE);
        statement.execute();

        // create the hard words xref table
        statement = connection.prepareStatement(SQL_CREATE_COMPONENT_WORDS_XREF_TABLE);
        statement.execute();

        // modifiers and xref
        statement = connection.prepareStatement(SQL_CREATE_MODIFIERS_TABLE);
        statement.execute();
        populateModifiersTable();
        statement = connection.prepareStatement(SQL_CREATE_MODIFIERS_XREF_TABLE);
        statement.execute();
        
        statement.close();
    }

    
    private synchronized static void populateSpeciesTable() throws SQLException {
         try (PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO " + SCHEMA + "." + SPECIES_TABLE
                         + " (species_name) VALUES(?)")) {
            for (Species species : Species.values()) {
                 statement.setString(1, species.description());
                 statement.execute();
            }
        }
        connection.commit();
    }

    private synchronized static void populateModifiersTable() throws SQLException {
         try (PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO " + SCHEMA + "." + MODIFIERS_TABLE
                         + " (modifier) VALUES(?)")) {
             for (Modifier modifier : Modifier.values()) {
                 statement.setString(1, modifier.description() );
                 statement.execute();
            }
        }
        connection.commit();
    }
    
    // The following cache* methods are repetitive.
    // It is possible to replace most of them with a single generic method that
    // takes a *Cache instance and a query as an argument.
    // However, it won't work with the Singleton cache implementations
    // as they are.

    private synchronized static void cacheIdentifierNamesAndKeys() {
        // first the identifier names
        LOGGER.info("Caching identifier names and keys");
        IdentifierNameCache cache = IdentifierNameCache.getInstance();
        try {
            PreparedStatement sqlAllIdentifierNamesQuery =
                    connection.prepareStatement(
                            "select identifier_name_key, identifier_name from "
                            + SCHEMA
                            + "."
                            + IDENTIFIER_NAMES_TABLE);
            ResultSet resultSet = sqlAllIdentifierNamesQuery.executeQuery();
            while (resultSet.next() == true) {
                cache.put(
                        resultSet.getInt( "identifier_name_key" ), 
                        resultSet.getString( "identifier_name" ));
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Identifer name query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here 
        }

        LOGGER.info(
                "{} identifier names cached",
                cache.size());
    }

    
    private synchronized static void cacheSpeciesKeys() {
        SpeciesCache cache = SpeciesCache.getInstance();
        try {
            try (PreparedStatement sqlCacheSpecies = connection.prepareStatement(
                    "SELECT species_name, species_name_key FROM " 
                            + SCHEMA + "." + SPECIES_TABLE)) {
                ResultSet speciesResult = sqlCacheSpecies.executeQuery();
                while (speciesResult.next() == true) {
                    cache.put( 
                            speciesResult.getInt( "species_name_key" ), 
                            speciesResult.getString( "species_name" ) );
                }
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Species cache query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode());
            return; // fail here 
        }

        LOGGER.info(
                "{} species cached",
                cache.size());
    }
    
    
    private synchronized static void cachePackageNameKeys() {
        PackageNameCache cache = PackageNameCache.getInstance();
        try {
            try (PreparedStatement sqlCachePackageName = connection.prepareStatement(
                    "SELECT package_name, package_name_key FROM " 
                    + SCHEMA + "." 
                    + PACKAGE_NAMES_TABLE)) {
                ResultSet packageResults = sqlCachePackageName.executeQuery();
                while ( packageResults.next() == true ) {
                    cache.put(
                            packageResults.getInt( "package_name_key" ), 
                            packageResults.getString( "package_name" ) );
                }
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Packages cache query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here 
        }

        LOGGER.info(
                "{} packages cached",
                cache.size());
    }

    private synchronized static void cacheTokensAndKeys() {
        // now the tokens and keys
        LOGGER.info( "Caching tokens and keys" );
        TokenCache cache = TokenCache.getInstance();
        try {
            PreparedStatement sqltokensQuery =
                    connection.prepareStatement(
                            "select component_word_key, component_word from "
                            + SCHEMA
                            + "."
                            + COMPONENT_WORDS_TABLE);
            ResultSet resultSet = sqltokensQuery.executeQuery();
            while (resultSet.next() == true) {
                cache.put(
                        resultSet.getInt( "component_word_key" ),
                        resultSet.getString( "component_word" ) );
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Token query failed: {}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here
        }

        LOGGER.info(
                "{} hard words cached",
                cache.size());
    }

    private synchronized static void cacheMethodSignatures() {
        LOGGER.info("Caching method signatures");
        MethodSignatureCache cache = MethodSignatureCache.getInstance();
        try {
            PreparedStatement sqlMethodSignaturesQuery = connection.prepareStatement(
                    "select method_signature, method_signature_key from "
                    + SCHEMA
                    + "."
                    + METHOD_SIGNATURES_TABLE );
            ResultSet resultSet = sqlMethodSignaturesQuery.executeQuery();
            while (resultSet.next() == true) {
                cache.put(
                        resultSet.getInt( "method_signature_key" ), 
                        resultSet.getString( "method_signature" ) );
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Method signature query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here
        }

        LOGGER.info(
                "{} method signatures cached",
                cache.size() );
    }
    
    private synchronized static void cacheTypeNames() {
        // now cache the type names
        LOGGER.info("Caching type names");
        TypeNameCache cache = TypeNameCache.getInstance();

        try {
            PreparedStatement sqlTypeNamesQuery = connection.prepareStatement(
                    "select type_name, type_name_key from "
                    + SCHEMA
                    + "."
                    + TYPE_NAMES_TABLE );
            ResultSet resultSet = sqlTypeNamesQuery.executeQuery();
            while (resultSet.next() == true) {
                cache.put( 
                        resultSet.getInt( "type_name_key" ), 
                        resultSet.getString( "type_name" ) );
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Type name query failed: {}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here
        }

        LOGGER.info(
                "{} type names cached",
                cache.size());
    }
    
    private synchronized static void cacheModifierKeys() {
        LOGGER.info("Caching modifier keys and modifiers");
        ModifierCache cache = ModifierCache.getInstance();
        
        try {
            PreparedStatement sqlModifiersQuery = connection.prepareStatement(
                    "SELECT modifier_key, modifier FROM "
                    + SCHEMA + "." + MODIFIERS_TABLE);
            ResultSet resultSet = sqlModifiersQuery.executeQuery();
            while (resultSet.next() == true) {
                cache.put(
                        resultSet.getInt( "modifier_key" ),
                        resultSet.getString( "modifier" ) );
            }
            
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Modifier name query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here 
        }

        LOGGER.info(
                "{} modifiers cached",
                cache.size());
    }
    
    
    private synchronized static void cacheProjectKeys() {
        ProjectKeyStore projectKeyStore = ProjectKeyStore.getInstance();
        try {
            try (PreparedStatement sqlCacheProjectName = connection.prepareStatement(
                    "SELECT project_key, project_name, project_version "
                            + "FROM " +SCHEMA + "." + PROJECT_TABLE)) {
                ResultSet projectResults = sqlCacheProjectName.executeQuery();
                while (projectResults.next() == true) {
                    projectKeyStore.put(
                            projectResults.getString( "project_name" )
                                    + " "
                                    + projectResults.getString("project_version"),
                            projectResults.getInt( "project_key" ) );
                }
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Projects cache query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here
        }

        LOGGER.info(
                "{} projects cached",
                projectKeyStore.size());
        
    }
   
    private synchronized static void cacheFileNames() {
        FileNameCache cache = FileNameCache.getInstance();
        try {
            try (PreparedStatement sqlCacheFileNames = connection.prepareStatement( 
                    "SELECT file_name_key, file_name "
                            + " FROM " + SCHEMA + "." + FILE_NAMES_TABLE )) {
                ResultSet resultSet = sqlCacheFileNames.executeQuery();
                while ( resultSet.next() ) {
                    cache.put(
                            resultSet.getInt( "file_name_key" ),
                            resultSet.getString( "file_name" ) );
                }
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "File names cache query failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return; // fail here
        }

        LOGGER.info(
                "{} file names cached",
                cache.size());
    }
}
