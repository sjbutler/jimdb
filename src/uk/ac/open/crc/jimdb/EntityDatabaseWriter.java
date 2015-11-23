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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;
import uk.ac.open.crc.idtk.TypeName;

/**
 * Provides an implementation for writing to the database.
 *
 */

class EntityDatabaseWriter {
    private static final Logger LOGGER = 
            LoggerFactory.getLogger( EntityDatabaseWriter.class );

    private final Connection connection;
    private final IdentifierNameCache identifierNameCache;
    private final TokenCache tokenCache;
    private final SpeciesCache speciesCache;
    private final TypeNameCache typeNameCache;
    private final MethodSignatureCache methodSignatureCache;
    private final PackageCache packageCache;
    private final PackageNameCache packageNameCache;
    private final ModifierCache modifierCache;
    private final FileNameCache fileNameCache;

    private Integer projectKey;

    /**
     * Constructor.
     */
    EntityDatabaseWriter() {
        this.identifierNameCache = IdentifierNameCache.getInstance();
        this.tokenCache = TokenCache.getInstance();

        this.speciesCache = SpeciesCache.getInstance();
        this.typeNameCache = TypeNameCache.getInstance();
        this.methodSignatureCache = MethodSignatureCache.getInstance();
        this.packageCache = PackageCache.getInstance();
        this.packageNameCache = PackageNameCache.getInstance();
        
        this.modifierCache = ModifierCache.getInstance();
        
        // NB this store is not primed prior to a datarun as it 
        // only makes sense in the context of a single datarun
        // (unless multiple versions are being investigated)
        this.fileNameCache = FileNameCache.getInstance();
        
        this.connection = EntityDatabaseManager.getConnection();
    }

    /**
     * Stores a program entity in the database.
     * @param programEntity the entity to store
     */
    void store(RawProgramEntity programEntity) {
        this.projectKey = JimDbConfiguration.getInstance().getProjectKey();
        
        // REVIEW THIS - there must be a better way of doing this!
        // make sure we're not the first in and set the project key
        if (this.projectKey == null) {
            this.projectKey = storeProject(
                    JimDbConfiguration.getInstance().getProjectName(), 
                    JimDbConfiguration.getInstance().getProjectVersion());
        }

      
        String fileName = programEntity.getFileName();
        Integer fileNameKey = this.fileNameCache.get( fileName );
        if ( fileNameKey == null ) {
            fileNameKey = storeFileName( fileName );
            this.fileNameCache.put( fileNameKey, fileName ); // & cache it
        }
   
        String entityDigest = programEntity.getEntityUid();
        String parentDigest = programEntity.getContainerUid();
        String packageName = programEntity.getPackageName();
        
        Species species = programEntity.getSpecies();
        Integer speciesNameKey = this.speciesCache.get( species.description() );

        // so, let's recover some keys
        // first the package name
        Integer packageNameKey = this.packageNameCache.get( packageName );
        if ( packageNameKey == null ) {
            packageNameKey = addPackageName( packageName );
            this.packageNameCache.put( packageNameKey, packageName );
        }

        // now we need to recover the package key 
        // - this is a key for the named package in this project
        Integer packageKey = this.packageCache.get( packageName );
        if ( packageKey == null ) {
            packageKey = addPackage( packageNameKey );
            this.packageCache.put( packageKey, packageName );
        } 
        
        String methodSignature = programEntity.getMethodSignature();
        Integer methodSignatureKey;
        if ( methodSignature == null ) { // signature will be null, "()" or "(...)"
            // i.e. we are not in a method
            methodSignatureKey = 0;
        }
        else {
            methodSignatureKey = this.methodSignatureCache.get( methodSignature );
            if (methodSignatureKey == null) {
                methodSignatureKey = storeMethodSignature( methodSignature );
            }
        }

        // single store single cache
        // name is fqn, or identifier name if not
        // may need to be changed to local name at later date to increase accuracy
        TypeName typeName = programEntity.getTypeName();
        String typeNameString = typeName.fqn();
        if ( typeNameString == null 
                || typeNameString.isEmpty() ) {
            typeNameString = typeName.identifierName();
        }
        
        Integer typeNameKey = this.typeNameCache.get( typeNameString );
        if (typeNameKey == null) {
            typeNameKey = storeTypeName(typeName);
        }
        
        // Need to store the identifier name
        String identifierName = programEntity.getIdentifierName();
        Integer identifierNameKey = this.identifierNameCache.get( identifierName );
        if ( identifierNameKey == null ) {
            identifierNameKey = storeIdentifierName( identifierName );
            this.identifierNameCache.put( identifierNameKey, identifierName );            
        }

        // store the ProgramEntity data to the database
        // to get the program entity key and 
        // then build the cross references for modifiers &c
        Integer programEntityKey = storeProgramEntity(
                this.projectKey,
                packageKey,
                identifierNameKey,
                parentDigest,
                entityDigest,
                speciesNameKey,
                typeNameKey,
                methodSignatureKey,
                identifierName.equals( ProgramEntity.ANONYMOUS ),
                fileNameKey,
                programEntity.isArrayDeclaration(),
                programEntity.isLoopControlVariable(),
                programEntity.getBeginLineNumber(),
                programEntity.getBeginColumn(),
                programEntity.getEndLineNumber(),
                programEntity.getEndColumn());
        
     
        // store the modifiers
        ArrayList<Modifier> modifierList = programEntity.getModifiers();
        if ( modifierList != null
                && ! modifierList.isEmpty() ) {
            storeModifierList(programEntityKey, modifierList);
        }
        
        // now the inheritance trees
        if ( species.isClass() || species.isInterface() ) {
            
            programEntity.getSuperClassList().stream().forEach((superClass) -> {
                storeSuperClass( superClass, programEntityKey);
            });
            
            programEntity.getSuperTypeList().stream().forEach((superType) -> {
                storeSuperType( superType, programEntityKey);
            });
        }
    }

    /**
     * Stores the entity in the database.
     * @param projectKey project key in database
     * @param packageKey package key in database
     * @param identifierNameKey name key in database
     * @param parentDigest hash identifying containing node
     * @param entityDigest hash identifying this node
     * @param speciesNameKey key of species in database
     * @param typeNameKey key of type name in database
     * @param methodSignatureKey key of method signature in database, of 0 if not a method
     * @param isAnonymous is the entity unnamed
     * @param fileNameKey key in database for the file name
     * @param isArrayDeclaration array declaration?
     * @param isLoopControlVariable loop control variable?
     * @param beginLineNumber physical location of entity in file
     * @param beginColumn physical location of entity in file
     * @param endLineNumber physical location of entity in file
     * @param endColumn physical location of entity in file
     * @return the program entity key
     */
    Integer storeProgramEntity(
                Integer projectKey,
                Integer packageKey,
                Integer identifierNameKey,
                String parentDigest,
                String entityDigest,
                Integer speciesNameKey,
                Integer typeNameKey,
                Integer methodSignatureKey,
                boolean isAnonymous,
                Integer fileNameKey,
                boolean isArrayDeclaration,
                boolean isLoopControlVariable,
                int beginLineNumber,
                int beginColumn,
                int endLineNumber,
                int endColumn) {
        Integer programEntityKey = null;
    
        try {
            PreparedStatement sqlProgramEntityInsert = EntityDatabaseManager.sqlProgramEntityInsert;
            sqlProgramEntityInsert.setInt(1, projectKey);
            sqlProgramEntityInsert.setInt(2, packageKey);
            sqlProgramEntityInsert.setInt(3, identifierNameKey);
            sqlProgramEntityInsert.setString(4, parentDigest);
            sqlProgramEntityInsert.setString(5, entityDigest);
            sqlProgramEntityInsert.setInt(6, speciesNameKey);
            sqlProgramEntityInsert.setInt(7, typeNameKey);
            sqlProgramEntityInsert.setInt(8, methodSignatureKey);
            sqlProgramEntityInsert.setBoolean(9, isAnonymous);
            sqlProgramEntityInsert.setInt(10, fileNameKey);
            sqlProgramEntityInsert.setBoolean(11, isArrayDeclaration);
            sqlProgramEntityInsert.setBoolean(12, isLoopControlVariable);
            sqlProgramEntityInsert.setInt(13, beginLineNumber);
            sqlProgramEntityInsert.setInt(14, beginColumn);
            sqlProgramEntityInsert.setInt(15, endLineNumber);
            sqlProgramEntityInsert.setInt(16, endColumn);
            sqlProgramEntityInsert.execute();
            ResultSet resultSet = sqlProgramEntityInsert.getGeneratedKeys();
            resultSet.next();
            programEntityKey = resultSet.getInt(1);
            this.connection.commit();
        }
        catch (SQLException sqlEx) {
            LOGGER.error(
                    "Insert in to program entity table failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
        
        return programEntityKey;
    }
    
    /**
     * Records a package name for this project. Forces the link between 
     * program entity and project through package. The relationship is 
     * only likely to be really important during data recovery phase.
     * 
     * @param packageNameKey the package name key
     * @return the project name key
     */
    private Integer addPackage(Integer packageNameKey) {
        Integer packageKey = null;
        
        try {
            PreparedStatement sqlPackageInsert = EntityDatabaseManager.sqlPackageInsert;
            sqlPackageInsert.setInt(1, this.projectKey);
            sqlPackageInsert.setInt(2, packageNameKey);
            sqlPackageInsert.execute();
            ResultSet resultSet = sqlPackageInsert.getGeneratedKeys();
            resultSet.next();
            packageKey = resultSet.getInt(1);
            this.connection.commit();
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Insert in to package table failed: {}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode() );
        }
        
        return packageKey;
    }
    
    /**
     * Records the project as a name and version pair, and sets the project 
     * key in the configuration class.
     * @param name project name
     * @param version project version
     * @return the project key in the database.
     */
    private Integer storeProject(String name, String version) {
        Integer localProjectKey = null;
        try {
            PreparedStatement sqlProjectInsert = EntityDatabaseManager.sqlProjectInsert;
            sqlProjectInsert.setString(1, name);
            sqlProjectInsert.setString(2, version);
            sqlProjectInsert.execute();
            try (ResultSet resultSet = sqlProjectInsert.getGeneratedKeys()) {
                resultSet.next();
                localProjectKey = resultSet.getInt(1);
                this.connection.commit();
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Failed to store project in container table: {}\n"
                            + "SQL state: {}\nError code: {}", 
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        JimDbConfiguration.getInstance().setProjectKey( localProjectKey );

        return localProjectKey;
    }


    // file names and keys are only being cached during the current data run
    // so need to check prior to store that the file name has not 
    // been previously recorded
    private Integer storeFileName( String fileName ) {
        Integer fileNameKey = null;
        
        try {
            PreparedStatement sqlFileNameQuery = 
                    EntityDatabaseManager.sqlFileNameQuery;
            sqlFileNameQuery.setString(1, fileName);
            ResultSet resultSet = sqlFileNameQuery.executeQuery();
            if ( resultSet.next() == true ) {
                fileNameKey = resultSet.getInt("file_name_key");
            }
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Problem encountered during file name query: {}\n"
                            + "SQL state: {}\nError code: {}", 
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
        
        // The file name hasn't been stored previously, so we now try to store it
        if ( fileNameKey == null ) {
            try {
                PreparedStatement sqlFileNameInsert = 
                        EntityDatabaseManager.sqlFileNameInsert;
                sqlFileNameInsert.setString(1, fileName);
                sqlFileNameInsert.executeUpdate();

                ResultSet resultSet = sqlFileNameInsert.getGeneratedKeys();
                resultSet.next();
                fileNameKey = resultSet.getInt(1);
                this.connection.commit();
            }
            catch (SQLException sqlEx) {
                LOGGER.warn(
                        "Failed to store file name: {}\n"
                                + "SQL state: {}\nError code: {}", 
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        
        return fileNameKey;
    }
    
    

    // add package
    // NB package names should be unique - but if multiple versions of the
    // same project are stored that rule no longer holds, hence the 
    // association with the project name and version pair.
    private Integer addPackageName(String packageName) {
        Integer packageNameKey = this.packageNameCache.get( packageName );

        if (packageNameKey == null) {
            try {
                PreparedStatement sqlPackageNameInsert = 
                        EntityDatabaseManager.sqlPackageNameInsert;
                sqlPackageNameInsert.setString(1, packageName);
                sqlPackageNameInsert.execute();
                try (ResultSet packageResults = 
                        sqlPackageNameInsert.getGeneratedKeys()) {
                    packageResults.next();
                    packageNameKey = packageResults.getInt(1);
                    
                    this.connection.commit();
                    packageResults.close();
                }
            }
            catch (SQLException sqlEx) {
                 LOGGER.warn(
                            "Failed to store package in package table: {}\n"
                                    + "SQL state: {}\nError code: {}",
                            sqlEx.getMessage(), 
                            sqlEx.getSQLState(), 
                            sqlEx.getErrorCode() );
            }
        }

        return packageNameKey;
    }



    /**
     * Stores an identifier name to the database. Takes care of identifier 
     * name tokenisation and the storage of tokens.
     * 
     * @param identifierName a name
     * @return the key for the name in the database
     */
    private Integer storeIdentifierName(String identifierName) {
        Integer identifierNameKey = null;

        try {
            PreparedStatement sqlIdentifierNameInsert = 
                    EntityDatabaseManager.sqlIdentifierNameInsert;
            sqlIdentifierNameInsert.setString(1, identifierName);
            sqlIdentifierNameInsert.executeUpdate();
            try ( ResultSet nameResults = sqlIdentifierNameInsert.getGeneratedKeys() ) {
                nameResults.next();
                identifierNameKey = nameResults.getInt(1);
                
                this.connection.commit();
            }

            if ( ! identifierName.startsWith( "#" ) ) { // trap out the non names
                List<String> tokens = 
                        InttSingleton.getInstance().tokenise( identifierName );
                storeTokens( tokens, identifierNameKey );
            }
        }
        catch (SQLException sqlEx) {
             LOGGER.warn(
                        "Failed to store identifier name in identifier name table: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
        }

        return identifierNameKey;
    }


    private Integer storeTypeName( TypeName typeName ) {
        Integer typeNameKey = null;

        // recover the identifier name key
        Integer identifierNameKey = 
                this.identifierNameCache.get( typeName.identifierName() );
        if ( identifierNameKey == null ) {
            identifierNameKey = storeIdentifierName( typeName.identifierName() );
            this.identifierNameCache.put( identifierNameKey, typeName.identifierName() );
        }
        
        String name = typeName.fqn();
        if ( name == null || name.isEmpty() ) {
            name = typeName.identifierName(); 
        }
        
        try {
            PreparedStatement sqlTypeNameInsert = 
                    EntityDatabaseManager.sqlTypeNameInsert;
            sqlTypeNameInsert.setString( 1, name );
            sqlTypeNameInsert.setInt( 2, identifierNameKey);
            sqlTypeNameInsert.execute();
            try ( ResultSet typeNameResults = sqlTypeNameInsert.getGeneratedKeys() ) {
                typeNameResults.next();
                typeNameKey = typeNameResults.getInt( 1 );
                
                this.connection.commit();
            }
        }
        catch (SQLException sqlEx) {
             LOGGER.warn(
                        "Failed to store type name in identifier name table: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
        }

        this.typeNameCache.put( typeNameKey, name );

        return typeNameKey;
    }


    private void storeTokens( List<String> tokens, Integer identifierNameKey ) {
        PreparedStatement sqlComponentWordInsert = 
                EntityDatabaseManager.sqlComponentWordInsert;
        Integer tokenKey;
        for ( int i = 0; i < tokens.size(); i++ ) {
            String token = tokens.get( i ).toLowerCase();
            tokenKey = this.tokenCache.get( token );
            if ( tokenKey == null ) {
                // unrecognised word, so save it
                try {
                    sqlComponentWordInsert.setString( 1, token );
                    sqlComponentWordInsert.execute();
                    // now retrieve the key
                    try ( ResultSet resultSet = sqlComponentWordInsert.getGeneratedKeys() ) {
                        resultSet.next();
                        tokenKey = resultSet.getInt(1);
                        this.connection.commit();
                    }
                }
                catch (SQLException sqlEx) {
                    LOGGER.warn(
                            "Insert into hard words table failed: {}\n"
                                    + "SQL state: {}\nError code: {}",
                            sqlEx.getMessage(), 
                            sqlEx.getSQLState(), 
                            sqlEx.getErrorCode() );
                    return; // fail here - is there need for a graceful recovery mechanism?
                }

                // and cache the token key for later
                this.tokenCache.put( tokenKey, token );
            }

            // now save the cross reference
            // this is a straghtforward insert
            try {
                PreparedStatement sqlComponentWordXrefInsert = 
                        EntityDatabaseManager.sqlComponentWordXrefInsert;
                sqlComponentWordXrefInsert.setInt( 1, tokenKey );
                sqlComponentWordXrefInsert.setInt( 2, identifierNameKey );
                sqlComponentWordXrefInsert.setInt( 3, i + 1 );  // the nth position in the identifier
                sqlComponentWordXrefInsert.executeUpdate();
                this.connection.commit();
            }
            catch (SQLException sqlEx) {
                LOGGER.warn(
                        "Insert into hard words xref table failed: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
    }


    private Integer storeMethodSignature( String methodSignature ) {
        // defensive
        Integer methodSignatureKey = 
                this.methodSignatureCache.get( methodSignature );

        if ( methodSignatureKey == null ) {
            try {
                PreparedStatement sqlMethodSignatureInsert = 
                        EntityDatabaseManager.sqlMethodSignatureInsert;
                sqlMethodSignatureInsert.setString( 1, methodSignature );
                sqlMethodSignatureInsert.execute();
                ResultSet resultSet = sqlMethodSignatureInsert.getGeneratedKeys();
                resultSet.next();
                methodSignatureKey = resultSet.getInt(1);
            }
            catch (SQLException sqlEx) {
                    LOGGER.warn(
                            "Insert into method signatures table failed: {}\n"
                                    + "SQL state: {}\nError code: {}",
                            sqlEx.getMessage(), 
                            sqlEx.getSQLState(), 
                            sqlEx.getErrorCode() );
                    return methodSignatureKey; // fail here - is there need for a graceful recovery mechanism?
            }
        }
        
        this.methodSignatureCache.put( methodSignatureKey, methodSignature );
        
        return methodSignatureKey;
    }


    private void storeModifierList(
            Integer programEntityKey, 
            ArrayList<Modifier> modifiers ) {
        modifiers.stream()
                .map( (modifier) -> this.modifierCache.get( modifier.description() ) )
                .forEach( (modifierKey) -> {
            storeModifierXref(modifierKey, programEntityKey);
        } );
    }

    
    private void storeSuperClass(TypeName superClassName, int programEntityKey) {
        // make sure the type is already stored, then xref
        Integer typeNameKey = this.storeTypeName( superClassName );
        
        try {
            PreparedStatement sqlSuperClassInsert = 
                    EntityDatabaseManager.sqlSuperClassInsert;
            sqlSuperClassInsert.setInt( 1, programEntityKey );
            sqlSuperClassInsert.setInt( 2, typeNameKey );
            sqlSuperClassInsert.executeUpdate();
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Insert into super class xref table failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
    }
    
    private void storeSuperType( TypeName superTypeName, int programEntityKey ) {
        Integer typeNameKey = this.storeTypeName( superTypeName );
        
        try {
            PreparedStatement sqlSuperTypeInsert =
                    EntityDatabaseManager.sqlSuperTypeInsert;
            sqlSuperTypeInsert.setInt( 1, programEntityKey );
            sqlSuperTypeInsert.setInt( 2, typeNameKey );
            sqlSuperTypeInsert.executeUpdate();
        }
        catch (SQLException sqlEx) {
            LOGGER.warn(
                    "Insert into super type xref table failed: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
    }
    
    private void storeModifierXref( Integer modifierKey, Integer programEntityKey ) {
        try {
            PreparedStatement sqlModifierXrefInsert = 
                    EntityDatabaseManager.sqlModifierXrefInsert;
            sqlModifierXrefInsert.setInt(1, modifierKey);
            sqlModifierXrefInsert.setInt(2, programEntityKey);
            
            sqlModifierXrefInsert.execute();
            
            this.connection.commit();
        }
        catch (SQLException sqlEx) {
             LOGGER.warn(
                        "Failed to store modifier cross reference: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
        }
    }
    
}
