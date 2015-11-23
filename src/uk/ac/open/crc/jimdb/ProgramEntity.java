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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;

/**
 * Records the database representation of a program entity for data recovery. 
 * Provides the basic transfer object for returning data from database queries.
 * 
 */
public class ProgramEntity {

    /**
     * Standard string to record for anonymous program entities.
     */
    public static String ANONYMOUS = "#anonymous#";
    
    /**
     * Standard string used to record program entities with no type. Typically,
     * this would be an initialiser.
     */
    public static String NO_TYPE = "#no type#";
    
    private static final Logger LOGGER = LoggerFactory.getLogger( ProgramEntity.class );
    
    /// --------------------- Find another home for this
    private static final HashMap<String,ArrayList<String>> contractionsMap;
    
    static {
        contractionsMap = new HashMap<>();
        InputStream inStream = 
                ProgramEntity.class.getResourceAsStream( "contractions.txt" );
        try ( BufferedReader in = new BufferedReader(new InputStreamReader(inStream)) ) {
            String line;

            // read file
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split(",");
                // sanity check
                if ( tokens.length == 2 ) {
                    String[] modalTokens = tokens[1].split( " " );
                    ArrayList<String> modalPhrase = new ArrayList<>();
                    modalPhrase.add( modalTokens[0] );
                    modalPhrase.add( modalTokens[1] );
                    contractionsMap.put( tokens[0], modalPhrase );
                }
            }
        }
        catch( IOException e ) {
            LOGGER.error(
                    "problem instantiating Modal Expansion component:{}", 
                    e.getMessage() );
            throw new IllegalStateException( 
                    "Could not instantiate Modal Expansion Component. "
                            + "Refer error to developer." );
        }
    }
    /// ----------------------------
    
    private final String projectName;
    private final String projectVersion;
    private final String identifierName;
    private final String packageName;
    private final ArrayList<String> componentWords;
    private final ArrayList<Modifier> modifierList;
    private final Species species;
    private final String containerUid;
    private final String entityUid;
    
    private final String type;
    private final String resolvableType;
    
    private final String fileName;
    private final int startLineNumber;
    private final int startColumn;
    private final int endLineNumber;
    private final int endColumn;
            
    private final boolean isArrayDeclaration;
    private final boolean isLoopControlVariable;
    private final boolean isAnonymous;
    
    private final ArrayList<String> subConcatenatedComponentWords;
    private final ArrayList<String> modalExpandedComponentWords;
    
    /**
     * Creates a representation of a program entity. This class is not to be used 
     * to model methods, classes or interfaces see the subclasses 
     * {@linkplain InheritableProgramEntity}
     * and {@linkplain InvokableProgramEntity}.
     * 
     * @param projectName the project name
     * @param projectVersion the project version
     * @param identifierName Must have a value, use {@linkplain ProgramEntity#ANONYMOUS} for unnamed entities. 
     * @param packageName the package name 
     * @param componentWords Should be null or an empty list when writing to DB.
     * @param modifierList a list of modifiers
     * @param species the species
     * @param containerUid a hash representing the containing entity
     * @param entityUid a hash representing this entity
     * @param type the type name
     * @param resolvableType a resolved type name
     * @param isArrayDeclaration array declaration?
     * @param fileName the file name
     * @param isLoopControlVariable loop control variable declaration?
     * @param startLineNumber physical location in file
     * @param startColumn physical location in file
     * @param endLineNumber physical location in file
     * @param endColumn physical location in file
     */
    public ProgramEntity(
            String projectName,
            String projectVersion,
            String identifierName, 
            String packageName, 
            ArrayList<String> componentWords, 
            ArrayList<Modifier> modifierList,
            Species species,
            String containerUid,
            String entityUid,
            String type,
            String resolvableType,
            boolean isArrayDeclaration,
            boolean isLoopControlVariable,
            String fileName,
            int startLineNumber,
            int startColumn,
            int endLineNumber,
            int endColumn
            ) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.identifierName = identifierName;
        this.packageName = packageName;
        this.componentWords = componentWords;
        this.modifierList = modifierList;
        this.species = species;
        this.containerUid = containerUid;
        this.entityUid = entityUid;
        this.type = type;
        this.resolvableType = resolvableType;
        this.isArrayDeclaration = isArrayDeclaration;
        this.isLoopControlVariable = isLoopControlVariable;
        this.fileName = fileName;
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
        
        this.isAnonymous = ANONYMOUS.equals( this.identifierName );
        
        this.subConcatenatedComponentWords = new ArrayList<>();
        subConcatenateComponentWords(); 
        
        this.modalExpandedComponentWords = new ArrayList<>();
        modalExpandComponentWords();
    }

    /**
     * Retrieves the project name the entity was found in.
     * @return a freeform string containing the project name
     */
    public String getProjectName() {
        return this.projectName;
    }
    
    /**
     * Retrieves the project version the entity was found in.
     * @return a freeform string containing the project version
     */
    public String getProjectVersion() {
        return this.projectVersion;
    }
    
    /**
     * Retrieves the identifier name associated with the entity.
     * @return an identifier name
     */
    public String getIdentifierName() {
        return this.identifierName;
    }

    /**
     * Retrieves the package name where the entity was found.
     * @return a package name
     */
    public String getPackageName() {
        return this.packageName;
    }

    /**
     * Retrieves the tokens found in the identifier name.
     * @return a {@code List} of tokens
     */
    public ArrayList<String> getTokens() {
        return this.componentWords;
    }

    /** 
     * Retrieves the modifiers used in the entity declaration.
     * @return a {@code List} of modifiers
     */
    public ArrayList<Modifier> getModifiers() {
        return this.modifierList;
    }

    /**
     * Retrieves the species of the declaration.
     * @return a species
     */
    public Species getSpecies() {
        return this.species;
    }

    /**
     * Retrieves the UID of the parent container.
     * @return retrieves a UID associated with the containing entity
     */
    public String getContainerUid() {
        return this.containerUid;
    }

    /** 
     * Retrieves the UID of this entity.
     * @return retrieves a UID associated with this entity
     */
    public String getEntityUid() {
        return this.entityUid;
    }

    /**
     * Retrieves the type name of the entity declaration.
     * @return the type name of the declaration or the string 
     * "#no type#" for untyped declarations
     */
    public String getType() {
        return this.type;
    }

    /**
     * Retrieves a longer form of the type name including package name, if the
     * type name has been resolved during the data collection process. NB this
     * value cannot be relied on.
     * @return a FQN if possible
     */
    public String getResolvableType() {
        return this.resolvableType;
    }
    
    /**
     * Indicates whether the entity is an array declaration. NB can only
     * be true for some species.
     * @return {@code true} if the entity was declared as an array type.
     */
    public boolean isArrayDeclaration() {
        return this.isArrayDeclaration;
    }
    
    /** 
     * Indicates if the entity was declared in a loop statement such as 
     * {@code for} or {@code while}. NB only applicable to the local
     * variable species.
     * @return {@code true} if entity declared in loop statement
     */
    public boolean isLoopControlVariable() {
        return this.isLoopControlVariable;
    }
    
    /**
     * Retrieves the name of the file in which the entity was declared.
     * @return a file name
     */
    public String getFileName() {
        return this.fileName;
    }
    
    /**
     * Retrieves the line number on which the entity starts.
     * @return a line number
     */
    public int getStartLineNumber() {
        return this.startLineNumber;
    }
    
    /**
     * Retrieves the column at which the entity begins.
     * @return a column number
     */
    public int getStartColumn() {
        return this.startColumn;
    }
    
    /**
     * Retrieves the line number on which the entity ends.
     * @return a line number
     */
    public int getEndLineNumber() {
        return this.endLineNumber;
    }
    
    /**
     * Retrieves the column number at which the entity ends.
     * @return a column number
     */
    public int getEndColumn() {
        return this.endColumn;
    }
    
    /**
     * This method guesses at the FQN for the recorded entity in the 
     * most simplistic way by concatenating the package name and the 
     * entity name. On many occasions this will be incorrect. This 
     * method should be overridden by implementing classes where a 
     * more precise implementation is possible. 
     * 
     * @return an FQN consisting of the package name a dot and the 
     * identifier name. In most cases this will be incorrect.
     */
    public String getFqn() {
        return this.packageName + "." + this.identifierName;
    }
    
    /**
     * Retrieves a list of component words where the particle 'sub'
     * (if present) has been concatenated with its successor. For example,
     * the identifier name topicSubMenu would be split into {topic, sub, menu},
     * this method would return the list {topic, submenu}. NB this 
     * is experimental and its practical validity has not been established.
     * 
     * @return a {@code List} of component words where any instances of 'sub' have
     * been combined with their successor.
     */
    public List<String> getSubConcatenatedComponentWords() {
        return this.subConcatenatedComponentWords;
    }
    
    // used by constructor to create list of component words with sub concatenated.
    private void subConcatenateComponentWords() {
        for ( int i = 0; i < this.componentWords.size(); i++ ) {
            if ( "sub".equals( this.componentWords.get( i ) ) 
                    && i < this.componentWords.size() - 1 ) {
                this.subConcatenatedComponentWords.add( 
                        this.componentWords.get( i ) 
                        + this.componentWords.get( i + 1 ) );
                i++;
            }
            else {
                this.subConcatenatedComponentWords.add( this.componentWords.get( i ) );
            }
        }
    }
    
    /// Review this functionality -- should it be here or elsewhere
    /**
     * Retrieves a list of component words where any modal contractions
     * have been expanded (eg cant -&gt; can not).
     * @return a {@code List} of component words with any modal contractions expanded.
     */
    public List<String> getModalExpandedComponentWords() {
        return this.modalExpandedComponentWords;
    }
    
    // SHOULD THIS USE THE SUB CONCATENATED AS ITS SOURCE?
    // used by constructor to create list of component words with modal expansion.
    private void modalExpandComponentWords() {
        for ( String word : this.componentWords ) {
            if ( contractionsMap.containsKey( word ) ) {
                this.modalExpandedComponentWords.addAll( contractionsMap.get( word ) );
            }
            else {
                this.modalExpandedComponentWords.add( word );
            }
        }
    }
    
}
