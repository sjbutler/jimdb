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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;

/**
 * Implements the read functionality for the EntityDatabase.
 *
 */
class EntityDatabaseReader {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger( EntityDatabaseReader.class );
    private static final int QUARTER_OF_A_MILLION = 250_000;
    private final Connection connection;
    private final TokenCache tokenCache;
    private final FileNameCache fileNameCache;
    private final IdentifierNameCache identifierNameCache;
    private final MethodSignatureCache methodSignatureCache;
    private final ModifierCache modifierCache;
    private final PackageNameCache packageNameCache;
    private final ProjectKeyStore projectKeyStore;
    private final SpeciesCache speciesCache;
    private final TypeNameCache typeNameCache;

    EntityDatabaseReader() {
        this.connection = EntityDatabaseManager.getConnection();

        // get the necessary caches
        this.tokenCache = TokenCache.getInstance();
        this.fileNameCache = FileNameCache.getInstance();
        this.identifierNameCache = IdentifierNameCache.getInstance();
        this.methodSignatureCache = MethodSignatureCache.getInstance();
        this.modifierCache = ModifierCache.getInstance();
        this.packageNameCache = PackageNameCache.getInstance();
        this.projectKeyStore = ProjectKeyStore.getInstance();
        this.speciesCache = SpeciesCache.getInstance();
        this.typeNameCache = TypeNameCache.getInstance();
    }

    ArrayList<String> getProjectsList() {
        return new ArrayList<>( this.projectKeyStore.getProjectNames() );
    }

    ArrayList<String> tokensFor( String identifierName ) {
        ArrayList<String> componentWords = null;

        Integer identifierNameKey = identifierNameCache.get( identifierName );

        if ( identifierNameKey == null ) {
            LOGGER.warn(
                    "No component words found for identifier name: \"{}\"",
                    identifierName );
        }
        else {
            try {
                PreparedStatement componentWordsXrefQuery = 
                        EntityDatabaseManager.sqlComponentWordsXrefQuery;
                componentWordsXrefQuery.setInt( 1, identifierNameKey );

                ResultSet resultSet = componentWordsXrefQuery.executeQuery();
                HashMap<Integer, Integer> components = new HashMap<>();
                while ( resultSet.next() ) {
                    components.put(
                            resultSet.getInt( "position" ) - 1,
                            resultSet.getInt( "component_word_key_fk" ) );
                }

                String[] words = new String[components.size()];

                for ( int index = 0; index < components.size(); index++ ) {
                    words[index] = this.tokenCache.get( components.get( index ) );
                }

                componentWords = new ArrayList<>( Arrays.asList( words ) );
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover component words for \"{}\": "
                                + "{}\nSQL state: {}\nError code: {}",
                        identifierName, 
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        return componentWords;
    }

    ArrayList<String> getPackageNamesForProject( String projectName ) {
        ArrayList<String> packageNames = new ArrayList<>();

        if ( projectName != null ) {
            try {
                PreparedStatement sqlPackageNameKeysQuery = 
                        EntityDatabaseManager.sqlPackageNameKeysForProjectQuery;
                Integer projectKey = this.projectKeyStore.get( projectName );
                if ( projectKey != null ) {
                    sqlPackageNameKeysQuery.setInt( 1, projectKey );
                    ResultSet resultSet = sqlPackageNameKeysQuery.executeQuery();
                    while ( resultSet.next() ) {
                        int packageNameKey = resultSet.getInt( "package_name_key_fk" );
                        packageNames.add( this.packageNameCache.get( packageNameKey ) );
                    }
                }
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover package names for project \"{}\": "
                                + "{}\nSQL state: {}\nError code: {}",
                        projectName, 
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        
        return packageNames;
    }

    ArrayList<String> getClassNamesForPackage( String projectName, String packageName ) {
        ArrayList<String> classNames = new ArrayList<>();

        Integer packageNameKey = this.packageNameCache.get( packageName );
        Integer projectKey = this.projectKeyStore.get( projectName );

        if ( packageNameKey != null && projectKey != null ) {
            Integer packageKey = null;
            try {
                PreparedStatement sqlNamedPackageKeyQuery = 
                        EntityDatabaseManager.sqlNamedPackageKeyQuery;
                sqlNamedPackageKeyQuery.setInt( 1, projectKey );
                sqlNamedPackageKeyQuery.setInt( 2, packageNameKey );
                ResultSet resultSet = sqlNamedPackageKeyQuery.executeQuery();
                resultSet.next();  // there should be only one result
                packageKey = resultSet.getInt( "package_key_fk" );
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover key for package \"{}\" in project \"{}\": "
                                + "{}\nSQL state: {}\nError code: {}",
                        packageName, 
                        projectName, 
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }

            try {
                PreparedStatement sqlClassNameKeysForPackageInProject = 
                        EntityDatabaseManager.sqlClassNameKeysForPackageInProjectQuery;
                sqlClassNameKeysForPackageInProject.setInt( 1, projectKey );
                sqlClassNameKeysForPackageInProject.setInt( 2, packageKey );
                sqlClassNameKeysForPackageInProject.setInt( 3, this.speciesCache.get( "class" ) );
                ResultSet resultSet = sqlClassNameKeysForPackageInProject.executeQuery();
                while ( resultSet.next() ) {
                    int identifierNameKey = resultSet.getInt( "identifier_name_key_fk" );
                    classNames.add( this.identifierNameCache.get( identifierNameKey ) );
                }
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover class names in package \"{}\" "
                                + "for project \"{}\": {}\nSQL state: {}\nError code: {}",
                        packageName, 
                        projectName, 
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }

        return classNames;
    }

    
    
    // NB This only extracts top-level classes.
    ArrayList<InheritableProgramEntity> getAllClassNamesFor( String projectNameAndVersion ) {
        ArrayList<InheritableProgramEntity> classNameData =
                new ArrayList<>();

        int projectKey = this.projectKeyStore.get( projectNameAndVersion );

        try {
            PreparedStatement sqlAllClassDataQuery = 
                    EntityDatabaseManager.sqlAllClassDataQuery;
            sqlAllClassDataQuery.setInt( 1, this.speciesCache.get( "class" ) );
            sqlAllClassDataQuery.setInt( 2, projectKey );

            ResultSet resultSet = sqlAllClassDataQuery.executeQuery();
            while ( resultSet.next() ) {
                int programEntityKey = resultSet.getInt( "program_entity_key" );
                int identifierNameKey = 
                        resultSet.getInt( "identifier_name_key_fk" );
                int packageKey = resultSet.getInt( "package_key_fk" );
                String identifierName = null;
                // not sure this guard is required, as it should always have a 
                // value
                if ( identifierNameKey != 0 ) {
                    identifierName = 
                            this.identifierNameCache.get( identifierNameKey );
                }

                String packageName = getPackageNameFor( packageKey );

                // recover the tokens
                ArrayList<String> tokens = 
                        tokensFor( identifierName );

                // get the modifiers
                ArrayList<Modifier> modifierList = 
                        getModifierList( programEntityKey );

                String containerUid = resultSet.getString( "container_uid" );
                String entityUid = resultSet.getString( "entity_uid" );
                int typeNameKey = resultSet.getInt( "type_name_key_fk" );
                String type = this.typeNameCache.get( typeNameKey );
                String resolveableType = null;  // for the moment
                boolean isArrayDeclaration = resultSet.getBoolean( "is_array" );
                boolean isLoopControlVar = resultSet.getBoolean( "is_loop_control_var" );
                // get text position
                int fileNameKey = resultSet.getInt( "file_name_key_fk" );
                String fileName = this.fileNameCache.get( fileNameKey );
                int startLineNumber = resultSet.getInt( "start_line_number" );
                int startColumn = resultSet.getInt( "start_column" );
                int endLineNumber = resultSet.getInt( "end_line_number" );
                int endColumn = resultSet.getInt( "end_column" );
                
                // recover project name and version 
                ProjectDetails projectDetails = getProjectDetails( projectKey );
                
                // get the parents and their tokens
                HashMap<String, ArrayList<String>> superClasses = 
                        buildSuperClassesMap( programEntityKey );
                HashMap<String, ArrayList<String>> superTypes = 
                        buildSuperTypesMap( programEntityKey );

                // create a class name data object and add it to the list
                classNameData.add(new InheritableProgramEntity(
                        projectDetails.name(),
                        projectDetails.version(),
                        identifierName,
                        packageName,
                        tokens,
                        modifierList,
                        Species.CLASS,
                        containerUid,
                        entityUid,
                        type,
                        resolveableType,
                        isArrayDeclaration,
                        isLoopControlVar,
                        fileName,
                        startLineNumber,
                        startColumn,
                        endLineNumber,
                        endColumn,
                        superClasses,
                        superTypes ) );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover class name data for project \"{}\": "
                            + "{}\nSQL state: {}\nError code: {}",
                    projectNameAndVersion, 
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return classNameData;
    }

    public ArrayList<ProgramEntity> getAllFieldNamesFor( String projectName ) {
        return getEntitiesForProjectBySpecies(projectName, Species.FIELD );
    }

    public ArrayList<ProgramEntity> getAllFormalArgumentNamesFor( String projectName ) {
        return getEntitiesForProjectBySpecies(projectName, Species.FORMAL_ARGUMENT );
    }

    public ArrayList<ProgramEntity> getAllLocalVariableNamesFor( String projectName ) {
        return getEntitiesForProjectBySpecies(projectName, Species.LOCAL_VARIABLE );
    }

    
    /**
     * Creates a list of classes and interfaces found in a project. The list 
     * includes inner classes and member classes.
     * 
     * @param projectName a project name and version separated by a space
     * @return a list of program entities
     */
    public ArrayList<ProgramEntity> getAllClassesAndInterfacesFor( String projectName ) {
        ArrayList<ProgramEntity> classesAndInterfaces = new ArrayList<>();
        
        for ( Species species : Species.values() ) {
            if ( species.isClassOrInterface() ) {
                classesAndInterfaces.addAll( 
                        getEntitiesForProjectBySpecies( projectName, species) );
            }
        }
        
        return classesAndInterfaces;
    }
    

    private ArrayList<ProgramEntity> getEntitiesForProjectBySpecies(
            final String projectName,
            final Species species ) {
        ArrayList<ProgramEntity> programEntityList = new ArrayList<>();

        int projectKey = this.projectKeyStore.get( projectName );
        int speciesKey = this.speciesCache.get( species.description() );

        try {
            PreparedStatement sqlEntitiesBySpeciesQuery = 
                    EntityDatabaseManager.sqlProgramEntitiesBySpeciesQuery;
            sqlEntitiesBySpeciesQuery.setInt( 1, projectKey );
            sqlEntitiesBySpeciesQuery.setInt( 2, speciesKey );

            ResultSet resultSet = sqlEntitiesBySpeciesQuery.executeQuery();

            while ( resultSet.next() ) {
                int programEntityKey = resultSet.getInt( "program_entity_key" );
                // gather the data
                String identifierName = 
                        this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
                String packageName = 
                        this.getPackageNameFor( resultSet.getInt( "package_key_fk" ) );
                ArrayList<String> componentWords = this.tokensFor( identifierName );
                ArrayList<Modifier> accessModifiers = 
                        this.getModifierList( programEntityKey );
                String containerUid = resultSet.getString( "container_uid" );
                String entityUid = resultSet.getString( "entity_uid" );
                int typeNameKey = resultSet.getInt( "type_name_key_fk" );
                String type = this.typeNameCache.get( typeNameKey );
                String resolvableType = null;  // for the moment
                ProjectDetails projectDetails = getProjectDetails( projectKey );
                boolean isArrayDeclaration = resultSet.getBoolean( "is_array" );
                boolean isLoopControlVariable = 
                        resultSet.getBoolean( "is_loop_control_var" );
                
                // get text position
                int fileNameKey = resultSet.getInt( "file_name_key_fk" );
                String fileName = this.fileNameCache.get( fileNameKey );
                int startLineNumber = resultSet.getInt( "start_line_number" );
                int startColumn = resultSet.getInt( "start_column" );
                int endLineNumber = resultSet.getInt( "end_line_number" );
                int endColumn = resultSet.getInt( "end_column" );
  
                
                String methodSignature = null;
                HashMap<String,ArrayList<String>> superClasses = null;
                HashMap<String,ArrayList<String>> superTypes = null;
                if ( species.isMethod() || species.isConstructor() ) {
                    methodSignature = 
                            this.methodSignatureCache.get( 
                                    resultSet.getInt( "method_signature_key_fk" ) );
                }
                else if ( species.isClassOrInterface() ) {
                    superClasses = buildSuperClassesMap( programEntityKey );
                    superTypes = buildSuperTypesMap( programEntityKey );
                }
                
                // now instantiate a ProgramEntity and add it to the list
                ProgramEntity programEntityData =
                        ProgramEntityFactory.create( 
                        projectDetails.name(),
                        projectDetails.version(),
                        identifierName,
                        packageName,
                        componentWords,
                        accessModifiers,
                        species,
                        containerUid,
                        entityUid,
                        type,
                        resolvableType,
                        isArrayDeclaration,
                        isLoopControlVariable,
                        fileName,
                        startLineNumber,
                        startColumn,
                        endLineNumber,
                        endColumn,
                        methodSignature,
                        superClasses,
                        superTypes);
                programEntityList.add( programEntityData );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Encountered problem recovering program entity data: {}"
                            + "\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return programEntityList;
    }

    public ArrayList<Modifier> getModifierList( int programEntityKey ) {
        ArrayList<Modifier> modifiers = new ArrayList<>();

        try {
            PreparedStatement sqlModifiersXrefQuery = 
                    EntityDatabaseManager.sqlModifiersXrefQuery;
            sqlModifiersXrefQuery.setInt( 1, programEntityKey );
            ResultSet resultSet = sqlModifiersXrefQuery.executeQuery();
            while ( resultSet.next() ) {
                modifiers.add( Modifier.getModifierFor( 
                        this.modifierCache.get( resultSet.getInt( "modifier_key_fk" ) ) ) );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover modifiers for program entity: "
                            + "{}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return modifiers;
    }

    String getPackageNameFor( int packageKey ) {
        String packageName = null;

        try {
            PreparedStatement sqlPackageNameKeyQuery = 
                    EntityDatabaseManager.sqlPackageNameKeyQuery;
            sqlPackageNameKeyQuery.setInt( 1, packageKey );
            ResultSet resultSet = sqlPackageNameKeyQuery.executeQuery();
            resultSet.next();
            int packageNameKey = resultSet.getInt( "package_name_key_fk" );
            packageName = this.packageNameCache.get( packageNameKey );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover package name for program entity: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return packageName;
    }

    /**
     * Retrieves a list of super classes for the given program entity. Where the
     * program entity is not a class an empty list is returned.
     * <p>
     * At the moment this returns only the RH component of the full path as this
     * is (a) the lowest common denominator, and (b) all that is needed for most
     * analysis.
     * </p>
     * 
     * @param programEntityKey a database key for a program entity
     * @param a list of modifiers
     */
    public ArrayList<String> getSuperClassNameList( int programEntityKey ) {
        ArrayList<String> superClassList = new ArrayList<>();

        try {
            PreparedStatement sqlSuperClassQuery = 
                    EntityDatabaseManager.sqlSuperClassQuery;
            sqlSuperClassQuery.setInt( 1, programEntityKey );
            ResultSet resultSet = sqlSuperClassQuery.executeQuery();
            while ( resultSet.next() ) {
                int superClassNameKey = 
                        resultSet.getInt( "super_class_name_key_fk" );
                int identifierNameKey = 
                        getIdentifierNameKeyForTypeName( superClassNameKey );
                superClassList.add( this.identifierNameCache.get( identifierNameKey ) );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover super class list for program entity: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return superClassList;
    }

    public ArrayList<String> getSuperTypeNameList( int programEntityKey ) {
        ArrayList<String> superTypeList = new ArrayList<>();

        try {
            PreparedStatement sqlSuperTypeQuery = 
                    EntityDatabaseManager.sqlSuperTypeQuery;
            sqlSuperTypeQuery.setInt( 1, programEntityKey );
            ResultSet resultSet = sqlSuperTypeQuery.executeQuery();
            while ( resultSet.next() ) {
                int superTypeNameKey = 
                        resultSet.getInt( "super_type_name_key_fk" );
                int identifierNameKey = 
                        getIdentifierNameKeyForTypeName( superTypeNameKey );
                superTypeList.add( this.identifierNameCache.get( identifierNameKey ) );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover super class list for program entity: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return superTypeList;
    }

    /**
     * Retrieves all the recorded program entities for a specific project.
     * 
     * @param projectName The name of the project
     * @param projectVersion The version of interest
     * @return A {@code List} of the program entities recorded for the project.
     */
    public List<ProgramEntity> getEntitiesFor( 
            final String projectName, 
            final String projectVersion ) {
        return getEntitiesFor( projectName + " " + projectVersion );
    }
    
    
    List<ProgramEntity> getEntitiesFor( final String projectNameAndVersion ) {
        int projectKey = this.projectKeyStore.get( projectNameAndVersion );
        
        ArrayList<ProgramEntity> programEntityList = 
                new ArrayList<>( QUARTER_OF_A_MILLION );
        
        try {
            PreparedStatement sqlEntitiesByProjectQuery = 
                    EntityDatabaseManager.sqlAllEntitiesByProjectQuery;
            sqlEntitiesByProjectQuery.setInt( 1, projectKey );

            ResultSet resultSet = sqlEntitiesByProjectQuery.executeQuery();

            while ( resultSet.next() ) {
                int programEntityKey = resultSet.getInt( "program_entity_key" );
                // gather the data
                String identifierName = 
                        this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
                String packageName = 
                        this.getPackageNameFor( resultSet.getInt( "package_key_fk" ) );
                ArrayList<String> componentWords = 
                        this.tokensFor( identifierName );
                ArrayList<Modifier> accessModifiers = 
                        this.getModifierList( programEntityKey );
                String containerUid = resultSet.getString( "container_uid" );
                String entityUid = resultSet.getString( "entity_uid" );
                int typeNameKey = resultSet.getInt( "type_name_key_fk" );
                String type = this.typeNameCache.get( typeNameKey );
                String resolvableType = null;  // for the moment
                boolean isArrayDeclaration = resultSet.getBoolean( "is_array" );
                boolean isLoopControlVariable = 
                        resultSet.getBoolean( "is_loop_control_var" );
                ProjectDetails projectDetails = getProjectDetails( projectKey );
                // get text position
                int fileNameKey = resultSet.getInt( "file_name_key_fk" );
                String fileName = this.fileNameCache.get( fileNameKey );
                int startLineNumber = resultSet.getInt( "start_line_number" );
                int startColumn = resultSet.getInt( "start_column" );
                int endLineNumber = resultSet.getInt( "end_line_number" );
                int endColumn = resultSet.getInt( "end_column" );
  
                String speciesName = 
                        this.speciesCache.get( resultSet.getInt( "species_name_key_fk" ) );
                Species species = Species.getSpeciesFor( speciesName );
                
                String methodSignature = null;
                HashMap<String,ArrayList<String>> superClasses = null;
                HashMap<String,ArrayList<String>> superTypes = null;
                if ( species.isMethod() || species.isConstructor() ) {
                    methodSignature = 
                            this.methodSignatureCache.get( 
                                    resultSet.getInt( "method_signature_key_fk" ) );
                }
                else if ( species.isClassOrInterface() ) {
                    superClasses = buildSuperClassesMap( programEntityKey );
                    superTypes = buildSuperTypesMap( programEntityKey );
                }
                
                
                // now instantiate a ProgramEntity and add it to the list
                ProgramEntity programEntityData = ProgramEntityFactory.create( 
                        projectDetails.name(),
                        projectDetails.version(),
                        identifierName,
                        packageName,
                        componentWords,
                        accessModifiers,
                        species,
                        containerUid,
                        entityUid,
                        type,
                        resolvableType,
                        isArrayDeclaration,
                        isLoopControlVariable,
                        fileName,
                        startLineNumber,
                        startColumn,
                        endLineNumber,
                        endColumn,
                        methodSignature,
                        superClasses,
                        superTypes);
                programEntityList.add( programEntityData );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Encountered problem recovering program entity data: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return programEntityList;
    }
    
    private int getIdentifierNameKeyForTypeName( int typeNameKey ) {
        Integer identifierNameKey = null;

        try {
            PreparedStatement typeNameIdentifierQuery = 
                    EntityDatabaseManager.sqlTypeNameIdentifierQuery;
            typeNameIdentifierQuery.setInt( 1, typeNameKey );
            ResultSet resultSet = typeNameIdentifierQuery.executeQuery();
            resultSet.next();
            identifierNameKey = resultSet.getInt( "identifier_name_key_fk" );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover identifier name key from type name: {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return identifierNameKey;
    }

    
    ArrayList<String> getIdentifierNamesFor( 
            String projectName, 
            Species species ) {
        ArrayList<String> identifierNames = new ArrayList<>();

        if ( projectName != null ) {
            try {
                Integer projectKey = this.projectKeyStore.get( projectName );
                Integer speciesKey = this.speciesCache.get( species.description() );

                PreparedStatement sqlSpeciesForProjectQuery = 
                        EntityDatabaseManager.sqlAllNamesForSpeciesByProjectQuery;
                sqlSpeciesForProjectQuery.setInt( 1, speciesKey );
                sqlSpeciesForProjectQuery.setInt( 2, projectKey );

                ResultSet resultSet = sqlSpeciesForProjectQuery.executeQuery();
                while ( resultSet.next() ) {
                    identifierNames.add( 
                            this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) ) );
                }
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover identifier names for species: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        
        return identifierNames;
    }

    ArrayList<String> getIdentifierNamesFor( String projectName ) {
        ArrayList<String> identifierNames = new ArrayList<>();

        if ( projectName != null ) {
            try {
                Integer projectKey = this.projectKeyStore.get( projectName );

                if ( projectKey != null ) {
                    PreparedStatement sqlSpeciesForProjectQuery = 
                            EntityDatabaseManager.sqlAllNamesForProjectQuery;
                    sqlSpeciesForProjectQuery.setInt( 1, projectKey );

                    ResultSet resultSet = sqlSpeciesForProjectQuery.executeQuery();
                    while ( resultSet.next() ) {
                        identifierNames.add( 
                                this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) ) );
                    }
                }
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover identifier names for project: {}\n"
                                + "SQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        
        return identifierNames;
    }

    ArrayList<String> getIdentifierNamesFor( 
            String projectName, Species species, Modifier modifier ) {
        ArrayList<String> identifierNames = new ArrayList<>();

        if ( projectName != null ) {
            try {
                Integer modifierKey = this.modifierCache.get( modifier.name() );
                Integer projectKey = this.projectKeyStore.get( projectName );
                Integer speciesKey = this.speciesCache.get( species.description() );

                if ( projectKey != null ) {
                    PreparedStatement sqlSpeciesForProjectAndModifierQuery = 
                            EntityDatabaseManager.sqlAllNamesForSpeciesByProjectQuery;
                    sqlSpeciesForProjectAndModifierQuery.setInt( 1, speciesKey );
                    sqlSpeciesForProjectAndModifierQuery.setInt( 2, projectKey );
                    sqlSpeciesForProjectAndModifierQuery.setInt( 3, modifierKey );

                    ResultSet resultSet = sqlSpeciesForProjectAndModifierQuery.executeQuery();
                    while ( resultSet.next() ) {
                        identifierNames.add( 
                                this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) ) );
                    }
                }
            }
            catch ( SQLException sqlEx ) {
                LOGGER.error(
                        "Could not recover identifier names for species constrained "
                                + "by modifier: {}\nSQL state: {}\nError code: {}",
                        sqlEx.getMessage(), 
                        sqlEx.getSQLState(), 
                        sqlEx.getErrorCode() );
            }
        }
        
        return identifierNames;
    }

    
    ArrayList<String> getNameSetFor(
            Species species,
            Integer count,
            Integer minimumLength ) {
        // recover the set of names according to the recipe submitted
        HashSet<String> startingNameSet = new HashSet<>();
        try {
            Integer speciesKey = this.speciesCache.get( species.description() );
            PreparedStatement sqlAllNamesForSpecies = 
                    EntityDatabaseManager.sqlAllNamesForSpeciesQuery;
            sqlAllNamesForSpecies.setInt( 1, speciesKey );

            ResultSet resultSet = sqlAllNamesForSpecies.executeQuery();
            while ( resultSet.next() ) {
                String identifierName = 
                        this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
                if ( identifierName.length() >= minimumLength ) {
                    startingNameSet.add( identifierName );
                }
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover identifier name set for species : "
                            + "{}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        ArrayList<String> nameList = new ArrayList<>();
        // now select the requested population size randomly
        HashSet<String> nameSet = new HashSet<>();
        if ( count > 0 && startingNameSet.size() > count ) {
            ArrayList<String> startingNameList = new ArrayList<>( startingNameSet );
            int maximumIndex = startingNameList.size() - 1;
            Random numberGenerator = new Random( System.currentTimeMillis() );
            do {
                int index = (int) (numberGenerator.nextDouble() * maximumIndex);
                String name = startingNameList.get( index );
                if ( name.length() >= minimumLength ) {
                    nameSet.add( name );
                }
            } while ( nameSet.size() < count );

            nameList.addAll( nameSet );
        }
        else {
            // zero value for count means every unique name
            // or there are fewer names than requested
            nameList.addAll( startingNameSet );
        }

        Collections.sort( nameList );
        return nameList;
    }
    
    ArrayList<String> getTokenisedNameSetFor(
            Species species,
            Integer count,
            Integer minimumLength ) {
        // recover the set of names according to the recipe submitted
        
        ArrayList<String> nameList = 
                getNameSetFor( species, count, minimumLength );
        
        ArrayList<String> tokenisedNameList = new ArrayList<>();
        // replace with a join lambda
        for ( String name : nameList ) {
            ArrayList<String> tokens = tokensFor( name );
            StringBuilder tokenisedName = new StringBuilder();
            for ( int i = 0; i < tokens.size(); i++ ) {
                tokenisedName.append( tokens.get( i ) );
                if ( i < (tokens.size() - 1) ) {
                    tokenisedName.append( " " );
                }
            }
            tokenisedNameList.add( tokenisedName.toString() );
        }
        
        Collections.sort( tokenisedNameList );
        return tokenisedNameList;
    }
    

    ArrayList<String> getNameSetFor(
            String projectName,
            Species species,
            Integer count,
            Integer minimumLength ) {
        // recover the set of names according to the recipe  submitted
        HashSet<String> startingNameSet = new HashSet<>();
        try {
            Integer speciesKey = this.speciesCache.get( species.description() );
            Integer projectKey = this.projectKeyStore.get( projectName );
            if ( projectKey != null ) {
                PreparedStatement sqlAllNamesForSpeciesByProject = 
                        EntityDatabaseManager.sqlAllNamesForSpeciesByProjectQuery;
                sqlAllNamesForSpeciesByProject.setInt( 1, speciesKey );
                sqlAllNamesForSpeciesByProject.setInt( 2, projectKey );

                ResultSet resultSet = sqlAllNamesForSpeciesByProject.executeQuery();
                while ( resultSet.next() ) {
                    String identifierName = 
                            this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
                    if ( identifierName.length() >= minimumLength ) {
                        startingNameSet.add( identifierName );
                    }
                }
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover identifier name set for species : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        ArrayList<String> nameList = new ArrayList<>();
        // now select the requested population size randomly
        HashSet<String> nameSet = new HashSet<>();
        if ( count > 0 && startingNameSet.size() > count ) {
            ArrayList<String> startingNameList = new ArrayList<>( startingNameSet );
            int maximumIndex = startingNameList.size() - 1;
            Random numberGenerator = new Random( System.currentTimeMillis() );
            do {
                int index = (int) (numberGenerator.nextDouble() * maximumIndex);
                String name = startingNameList.get( index );
                if ( name.length() >= minimumLength ) {
                    nameSet.add( name );
                }
            } while ( nameSet.size() < count );

            nameList.addAll( nameSet );
        }
        else {
            // zero value for count means every unique name
            // and take the whole list if fewer than count members
            nameList.addAll( startingNameSet );
        }

        Collections.sort( nameList );
        return nameList;
    }

    ArrayList<String> getTokenisedNameSetFor(
            String projectName,
            Species species,
            Integer count,
            Integer minimumLength ) {
        ArrayList<String> nameList = 
                getNameSetFor( projectName, species, count, minimumLength );
        
        ArrayList<String> tokenisedNameList = new ArrayList<>();
        // replace with a join lambda
        for ( String name : nameList ) {
            ArrayList<String> tokens = tokensFor( name );
            StringBuilder tokenisedName = new StringBuilder();
            for ( int i = 0; i < tokens.size(); i++ ) {
                tokenisedName.append( tokens.get( i ) );
                if ( i < (tokens.size() - 1) ) {
                    tokenisedName.append( " " );
                }
            }
            tokenisedNameList.add( tokenisedName.toString() );
        }
        
        Collections.sort( tokenisedNameList );
        return tokenisedNameList; 
    }
    
    InheritableProgramEntity getClassOrInterfaceFor( String projectName, String fqn ) {
        InheritableProgramEntity entityData = null;

        // split the fqn into package and class/interface name
        int lastDotIndex = fqn.lastIndexOf( "." );
        String packageName = fqn.substring( 0, lastDotIndex );
        String entityName = fqn.substring( lastDotIndex + 1 );

        int projectKey = this.projectKeyStore.get( projectName );
        int packageKey = this.packageNameCache.get( packageName );
        int identifierNameKey = this.identifierNameCache.get( entityName );

        try {

            PreparedStatement sqlClassOrInterfaceForFqnQuery = 
                    EntityDatabaseManager.sqlClassOrInterfaceForFqnQuery;
            sqlClassOrInterfaceForFqnQuery.setInt( 1, projectKey );
            sqlClassOrInterfaceForFqnQuery.setInt( 2, packageKey );
            sqlClassOrInterfaceForFqnQuery.setInt( 3, identifierNameKey );

            ResultSet resultSet = sqlClassOrInterfaceForFqnQuery.executeQuery();

            int programEntityKey = resultSet.getInt( "program_entity_key" );
            // recover the component words
            ArrayList<String> componentWords = tokensFor( entityName );

            // get the modifiers
            ArrayList<Modifier> modifierList = getModifierList( programEntityKey );
            String containerUid = resultSet.getString( "container_uid" );
            String entityUid = resultSet.getString( "entity_uid" );
            int typeNameKey = resultSet.getInt( "type_name_key_fk" );
            String type = this.typeNameCache.get( typeNameKey );
            String resolveableType = null;  // for the moment
            int speciesNameKey = resultSet.getInt( "species_name_key_fk" );
            Species species = 
                    Species.getSpeciesFor( this.speciesCache.get( speciesNameKey ) );
            boolean isArrayDeclaration = resultSet.getBoolean( "is_array" );
            boolean isLoopControlVariable = 
                    resultSet.getBoolean( "is_loop_control_var" );
            ProjectDetails projectDetails = getProjectDetails( projectKey );
            // get text position
            int fileNameKey = resultSet.getInt( "file_name_key_fk" );
            String fileName = this.fileNameCache.get( fileNameKey );
            int startLineNumber = resultSet.getInt( "start_line_number" );
            int startColumn = resultSet.getInt( "start_column" );
            int endLineNumber = resultSet.getInt( "end_line_number" );
            int endColumn = resultSet.getInt( "end_column" );

            // get the parents and their tokens
            HashMap<String, ArrayList<String>> superClasses = 
                    buildSuperClassesMap( programEntityKey );
            HashMap<String, ArrayList<String>> superTypes = 
                    buildSuperTypesMap( programEntityKey );

            // create a class name data object and add it to the list
            entityData = new InheritableProgramEntity(
                    projectDetails.name(),
                    projectDetails.version(),
                    entityName,
                    packageName,
                    componentWords,
                    modifierList,
                    species,
                    containerUid,
                    entityUid,
                    type,
                    resolveableType,
                    isArrayDeclaration,
                    isLoopControlVariable,
                    fileName,
                    startLineNumber,
                    startColumn,
                    endLineNumber,
                    endColumn,
                    superClasses,
                    superTypes );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover class or interface for given FQN : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return entityData;
    }

    ArrayList<InheritableProgramEntity> getEntityCandidatesFor( 
            String className, 
            Species targetSpecies ) {
        // guard against inadvertant calls 
        if ( targetSpecies != Species.CLASS && targetSpecies != Species.INTERFACE ) {
            throw new IllegalArgumentException( "target must be class or interface." );
        }
        
        ArrayList<InheritableProgramEntity> candidateClasses = new ArrayList<>();

        // run the query
        int identifierNameKey = this.identifierNameCache.get( className );
        try {
            PreparedStatement sqlClassesForNameQuery = 
                    EntityDatabaseManager.sqlEntityCandidatesForNameQuery;
            sqlClassesForNameQuery.setInt( 1, identifierNameKey );
            sqlClassesForNameQuery.setInt( 2, this.speciesCache.get( targetSpecies.description() ) );
            ResultSet resultSet = sqlClassesForNameQuery.executeQuery();
            while ( resultSet.next() ) {
                int programEntityKey = resultSet.getInt( "program_entity_key" );
                // recover the component words
                ArrayList<String> tokens = tokensFor( className );

                String packageName = getPackageNameFor( resultSet.getInt( "package_key_fk" ));
                
                // get the modifiers
                ArrayList<Modifier> modifierList = getModifierList( programEntityKey );
                String containerUid = resultSet.getString( "container_uid" );
                String entityUid = resultSet.getString( "entity_uid" );
                int typeNameKey = resultSet.getInt( "type_name_key_fk" );
                String type = this.typeNameCache.get( typeNameKey );
                String resolveableType = null;  // for the moment
                int speciesNameKey = resultSet.getInt( "species_name_key_fk" );
                Species species = Species.getSpeciesFor( this.speciesCache.get( speciesNameKey ) );
                
                int projectKey = resultSet.getInt( "project_key_fk" );
                ProjectDetails projectDetails = getProjectDetails( projectKey );
                // get text position
                int fileNameKey = resultSet.getInt( "file_name_key_fk" );
                String fileName = this.fileNameCache.get( fileNameKey );
                int startLineNumber = resultSet.getInt( "start_line_number" );
                int startColumn = resultSet.getInt( "start_column" );
                int endLineNumber = resultSet.getInt( "end_line_number" );
                int endColumn = resultSet.getInt( "end_column" );
  
                // get the parents and their tokens
                HashMap<String, ArrayList<String>> superClasses = 
                        buildSuperClassesMap( programEntityKey );
                HashMap<String, ArrayList<String>> superTypes = 
                        buildSuperTypesMap( programEntityKey );

                // create a class name data object and add it to the list
                InheritableProgramEntity entityData = new InheritableProgramEntity(
                        projectDetails.name(),
                        projectDetails.version(),
                        className,
                        packageName,
                        tokens,
                        modifierList,
                        species,
                        containerUid,
                        entityUid,
                        type,
                        resolveableType,
                        false,                  // can't be an array
                        false,                  // cannot be loop control
                        fileName,
                        startLineNumber,
                        startColumn,
                        endLineNumber,
                        endColumn,
                        superClasses,
                        superTypes );
                
                candidateClasses.add( entityData );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover candidate list for given class or "
                            + "interface name : {}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }

        return candidateClasses;
    }

    ArrayList<InheritableProgramEntity> getSubclassesFor( String className ) {
        ArrayList<InheritableProgramEntity> candidateSubclasses = new ArrayList<>();
        int typeNameKey = 0; // initialise to something
        // recover the type name key
        int typeIdentifierNameKey = this.identifierNameCache.get( className );
        try {
            PreparedStatement typeNameQuery = 
                    EntityDatabaseManager.sqlTypeNameKeyByIdentifierNameKeyQuery;
            typeNameQuery.setInt( 1, typeIdentifierNameKey );
            ResultSet resultSet = typeNameQuery.executeQuery();
            typeNameKey = resultSet.getInt( "type_name_key" );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover type name key : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            // is this fatal? or should we return here?
        }
        
        // now query the super class xref table to recover possible subclasses
        try {
            PreparedStatement subClassKeyQuery = 
                    EntityDatabaseManager.sqlSubClassKeyQuery;
            subClassKeyQuery.setInt( 1, typeNameKey);
            ResultSet resultSet = subClassKeyQuery.executeQuery();
            while ( resultSet.next() ) {
                candidateSubclasses.add( getInheritableEntityFor( 
                        resultSet.getInt( "sub_class_entity_key_fk" ) ));
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover subclass entity key : {}\nSQL state: {}"
                            + "\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
        
        return candidateSubclasses;
    }
   
    
    ArrayList<InheritableProgramEntity> getSubTypesFor( String interfaceName ) {
        ArrayList<InheritableProgramEntity> candidateSubTypes = new ArrayList<>();
        int typeNameKey = 0; // initialise to something
        // recover the type name key
        int typeIdentifierNameKey = this.identifierNameCache.get( interfaceName );
        try {
            PreparedStatement typeNameQuery = 
                    EntityDatabaseManager.sqlTypeNameKeyByIdentifierNameKeyQuery;
            typeNameQuery.setInt( 1, typeIdentifierNameKey );
            ResultSet resultSet = typeNameQuery.executeQuery();
            typeNameKey = resultSet.getInt( "type_name_key" );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover type name key : {}\nSQL state: {}\nError code: {}",
                    sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode() );
            // is this fatal, or should we return here?
        }
        
        // now query the super type xref table to recover possible 
        // implementing classes and extending interfaces
        try {
            PreparedStatement subTypeKeyQuery = 
                    EntityDatabaseManager.sqlSubTypeKeyQuery;
            subTypeKeyQuery.setInt( 1, typeNameKey);
            ResultSet resultSet = subTypeKeyQuery.executeQuery();
            while ( resultSet.next() ) {
                candidateSubTypes.add( getInheritableEntityFor( 
                        resultSet.getInt( "sub_type_entity_key_fk" ) ));
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover subtype entity key : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
        
        return candidateSubTypes;
    }
    
    // rename this to something more descriptive
    // review the implementation -- post hoc truncation must be wasteful
    HashSet<ProgramEntity> getEntitySetWhere( 
            Species species, 
            int maxCount, 
            TypeGroup typeGroup ) {
        HashSet<ProgramEntity> outputSet = new HashSet<>();
        
        // recover species with unique names
        // -- recover a list of entities
        ArrayList<ProgramEntity> programEntities = getEntitiesBySpecies( species );
        
        // filter by TypeGroup
        // -- copy those entities with the correct TypeGroup to the output set
        // -- make sure set members are unique by name 
        HashSet<String> nameSet = new HashSet<>();
        for ( ProgramEntity programEntity : programEntities ) {
            if ( TypeGroup.classifyFromString( programEntity.getType() ) == typeGroup ) {
                String identifierName = programEntity.getIdentifierName();
                if ( ! nameSet.contains( identifierName ) ) {
                    outputSet.add( programEntity );
                    nameSet.add( identifierName );
                }
            }
        }

        // cap set size if necessary
        if ( outputSet.size() > maxCount ) {
            outputSet = truncateSet( outputSet, maxCount );
        }
        
        return outputSet;
    }
    
    ArrayList<ProgramEntity> getEntitiesBySpecies( Species species ) {
        ArrayList<ProgramEntity> programEntities = new ArrayList<>();
        
        try {
            int speciesKey = this.speciesCache.get( species.description() );
            
            PreparedStatement entityBySpeciesQuery = 
                    EntityDatabaseManager.sqlAllEntitiesBySpeciesQuery;
            entityBySpeciesQuery.setInt( 1, speciesKey );
            ResultSet resultSet = entityBySpeciesQuery.executeQuery();
            
            // now build the program entities
            // this is awful and repetetive. May however, signpost way to refactoring.
            while ( resultSet.next() ) {
                ProgramEntity entityData;
                
                String entityName = 
                        this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
                ArrayList<String> tokens = tokensFor( entityName );

                String packageName = 
                        getPackageNameFor( resultSet.getInt( "package_key_fk" ));

                // get the modifiers
                int programEntityKey = resultSet.getInt( "program_entity_key" );
                ArrayList<Modifier> modifierList = getModifierList( programEntityKey );
                String containerUid = resultSet.getString( "container_uid" );
                String entityUid = resultSet.getString( "entity_uid" );
                int typeNameKey = resultSet.getInt( "type_name_key_fk" );
                String type = this.typeNameCache.get( typeNameKey );
                String resolveableType = null;  // for the moment
                boolean isArrayDeclaration = resultSet.getBoolean( "is_array" );
                boolean isLoopControlVariable = 
                        resultSet.getBoolean( "is_loop_control_var" );

                int projectKey = resultSet.getInt( "project_key_fk" );
                ProjectDetails projectDetails = getProjectDetails( projectKey );
                // get text position
                int fileNameKey = resultSet.getInt( "file_name_key_fk" );
                String fileName = this.fileNameCache.get( fileNameKey );
                int startLineNumber = resultSet.getInt( "start_line_number" );
                int startColumn = resultSet.getInt( "start_column" );
                int endLineNumber = resultSet.getInt( "end_line_number" );
                int endColumn = resultSet.getInt( "end_column" );

                HashMap<String, ArrayList<String>> superClasses = null;
                HashMap<String, ArrayList<String>> superTypes = null;
                if ( species.isClassOrInterface() ) {
                    superClasses = buildSuperClassesMap( programEntityKey );
                    superTypes = buildSuperTypesMap( programEntityKey );
                }
                
                String methodSignature = null;
                if ( species.isMethod() || species.isConstructor() ) {
                    int methodSignatureKey = 
                            resultSet.getInt( "method_signature_key_fk" );
                    methodSignature = 
                            this.methodSignatureCache.get( methodSignatureKey );
                }
                    
                entityData = ProgramEntityFactory.create(
                            projectDetails.name(),
                            projectDetails.version(),
                            entityName,
                            packageName,
                            tokens,
                            modifierList,
                            species,
                            containerUid,
                            entityUid,
                            type,
                            resolveableType,
                            isArrayDeclaration,
                            isLoopControlVariable,
                            fileName,
                            startLineNumber,
                            startColumn,
                            endLineNumber,
                            endColumn,
                            methodSignature,
                            superClasses,
                            superTypes );
                
                programEntities.add( entityData );
            }
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover entities for species : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
        }
        
        return programEntities;
    }
    
    // truncates a set to a given size
    private HashSet<ProgramEntity> truncateSet( HashSet<ProgramEntity> set, int maxSize ) {
        // early out when no work is required
        if ( set.size() <= maxSize ) {
            return set;
        }
        
        HashSet<ProgramEntity> outputSet = new HashSet<>();
        
        for ( ProgramEntity entity : set ) {
            outputSet.add( entity );
            if ( outputSet.size() == maxSize ) {
                break;
            } 
        }
        
        return outputSet;
    }
    
    // recovers and inheritable program entity from a program entity key
    // currently used for retrieving subclasses and subtypes
    //
    // The majority of this method is replicated in a number of places
    // in this class and is a prime candidate for refactoring (possible). 
    private InheritableProgramEntity getInheritableEntityFor( int programEntityKey ) {
        InheritableProgramEntity entityData = null;
        
        try {
            PreparedStatement sqlInheritableProgramEntityQuery = 
                    EntityDatabaseManager.sqlInheritableProgramEntityQuery;
            sqlInheritableProgramEntityQuery.setInt( 1, programEntityKey);
            ResultSet resultSet = sqlInheritableProgramEntityQuery.executeQuery();
            resultSet.next();

            String entityName = 
                    this.identifierNameCache.get( resultSet.getInt( "identifier_name_key_fk" ) );
            ArrayList<String> componentWords = tokensFor( entityName );

            String packageName = getPackageNameFor( resultSet.getInt( "package_key_fk" ));

            // get the modifiers
            ArrayList<Modifier> modifierList = getModifierList( programEntityKey );
            String containerUid = resultSet.getString( "container_uid" );
            String entityUid = resultSet.getString( "entity_uid" );
            int typeNameKey = resultSet.getInt( "type_name_key_fk" );
            String type = this.typeNameCache.get( typeNameKey );
            String resolveableType = null;  // for the moment
            int speciesNameKey = resultSet.getInt( "species_name_key_fk" );
            Species species = Species.getSpeciesFor( this.speciesCache.get( speciesNameKey ) );

            int projectKey = resultSet.getInt( "project_key_fk" );
            ProjectDetails projectDetails = getProjectDetails( projectKey );
            // get text position
            int fileNameKey = resultSet.getInt( "file_name_key_fk" );
            String fileName = this.fileNameCache.get( fileNameKey );
            int startLineNumber = resultSet.getInt( "start_line_number" );
            int startColumn = resultSet.getInt( "start_column" );
            int endLineNumber = resultSet.getInt( "end_line_number" );
            int endColumn = resultSet.getInt( "end_column" );
  
            // get the parents and their tokens
            HashMap<String, ArrayList<String>> superClasses = 
                    buildSuperClassesMap( programEntityKey );
            HashMap<String, ArrayList<String>> superTypes = 
                    buildSuperTypesMap( programEntityKey );

            // create a class name data object and add it to the list
            entityData = new InheritableProgramEntity(
                    projectDetails.name(),
                    projectDetails.version(),
                    entityName,
                    packageName,
                    componentWords,
                    modifierList,
                    species,
                    containerUid,
                    entityUid,
                    type,
                    resolveableType,
                    false,              // not an array declaration
                    false,              // not loop control variable
                    fileName,
                    startLineNumber,
                    startColumn,
                    endLineNumber,
                    endColumn,
                    superClasses,
                    superTypes );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover subclass entity key : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            // is this fatal, or should we return here?
        }
        
        return entityData;
    }
    
    private HashMap<String, ArrayList<String>> buildSuperClassesMap( int programEntityKey ) {
        ArrayList<String> superClassList = getSuperClassNameList( programEntityKey );
        HashMap<String, ArrayList<String>> superClasses = new HashMap<>();
        superClassList.stream().forEach((superClassName) -> {
            ArrayList<String> componentWordList = tokensFor( superClassName );
            superClasses.put( superClassName, componentWordList );
        } );

        return superClasses;
    }

    private HashMap<String, ArrayList<String>> buildSuperTypesMap( int programEntityKey ) {
        ArrayList<String> superTypeList = getSuperTypeNameList( programEntityKey );
        HashMap<String, ArrayList<String>> superTypes = new HashMap<>();
        superTypeList.stream().forEach((superTypeName) -> {
            ArrayList<String> componentWordList = tokensFor( superTypeName );
            superTypes.put( superTypeName, componentWordList );
        } );

        return superTypes;
    }
    
    private ProjectDetails getProjectDetails( int projectKey ) {
        ProjectDetails projectDetails;
        PreparedStatement sqlProjectDetailsQuery = 
                EntityDatabaseManager.sqlProjectDetailsQuery;
        
        try {
            sqlProjectDetailsQuery.setInt( 1, projectKey);
            ResultSet resultSet = sqlProjectDetailsQuery.executeQuery();
            resultSet.next();
            projectDetails = new ProjectDetails( 
                    resultSet.getString( "project_name" ), 
                    resultSet.getString( "project_version" ) );
        }
        catch ( SQLException sqlEx ) {
            LOGGER.error(
                    "Could not recover project details : {}\n"
                            + "SQL state: {}\nError code: {}",
                    sqlEx.getMessage(), 
                    sqlEx.getSQLState(), 
                    sqlEx.getErrorCode() );
            return null;
        }
        
        return projectDetails;
    }
    
    
    
    /**
     * Class to encapsulate data.
     *
     */
    class ProjectDetails {
        private final String name;
        private final String version;
    
        ProjectDetails( String name, String version ) {
            this.name = name;
            this.version = version;
        }
    
        String name() {
            return this.name;
        }
    
        String version() {
            return this.version;
        }
    
    }
}
