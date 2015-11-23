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
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;
import uk.ac.open.crc.idtk.TypeName;

/**
 * Represents an unprocessed block of program entity data being passed to jimdb
 * for processing and storage.
 * <p>
 * For the data classes used to extract data from jimdb see
 * {@linkplain ProgramEntity}, {@linkplain InheritableProgramEntity}, and
 * {@linkplain InvokableProgramEntity}.
 * </p>
 */
public class RawProgramEntity {

    private final String fileName;
    private final String packageName;

    private final String containerUid;
    private final String entityUid;

    private final String identifierName;
    private final Species species;

    private final boolean isArray;
    private final boolean isLoopControlVariable;
    private final TypeName typeName;
    private final String methodSignature;

    private final ArrayList<Modifier> modifiers;

    private final ArrayList<TypeName> superClassList;
    private final ArrayList<TypeName> superTypeList;

    private final int beginLineNumber;
    private final int beginColumn;

    private final int endLineNumber;
    private final int endColumn;

    /**
     * Creates a transfer object to be passed to jimdb.
     *
     * @param fileName a file name
     * @param packageName a package name
     * @param containerUid the UID of the containing entity
     * @param entityUid the UID of the entity
     * @param identifierName an identifier name
     * @param species a species
     * @param typeName a type name
     * @param isArray {@code true} if the entity is an array declaration
     * @param methodSignature a method signature, or null if entity is not a constructor or method
     * @param modifiers a {@code List} of modifiers
     * @param isLoopControlVariable {@code true} if the entity is declared as a loop control variable
     * @param superClassList a {@code List} of super class names, or {@code null} if entity is not class or interface
     * @param superTypeList a {@code List} of super type names, or {@code null} if entity is not class or interface
     * @param beginLineNumber a line number
     * @param beginColumn a column number
     * @param endLineNumber a line number
     * @param endColumn a column number 
     */
    public RawProgramEntity(
            String fileName,
            String packageName,
            String containerUid,
            String entityUid,
            String identifierName,
            Species species,
            TypeName typeName,
            boolean isArray,
            String methodSignature,
            ArrayList<Modifier> modifiers,
            boolean isLoopControlVariable,
            ArrayList<TypeName> superClassList,
            ArrayList<TypeName> superTypeList,
            int beginLineNumber,
            int beginColumn,
            int endLineNumber,
            int endColumn ) {
        this.fileName = fileName;
        this.packageName = packageName;
        this.containerUid = containerUid;
        this.entityUid = entityUid;

        this.identifierName = identifierName;
        this.species = species;

        this.typeName = typeName;
        this.isArray = isArray;
        this.isLoopControlVariable = isLoopControlVariable;
        this.methodSignature = methodSignature;

        if ( modifiers == null ) {
            this.modifiers = new ArrayList<>();
        }
        else {
            this.modifiers = new ArrayList<>( modifiers ); // copy to prevent problems with caller modification
        }

        if ( superClassList == null ) {
            this.superClassList = new ArrayList<>();
        }
        else {
            this.superClassList = new ArrayList<>( superClassList );
        }

        if ( superTypeList == null ) {
            this.superTypeList = new ArrayList<>();
        }
        else {
            this.superTypeList = new ArrayList<>( superTypeList );
        }

        this.beginLineNumber = beginLineNumber;
        this.beginColumn = beginColumn;

        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
    }

    /**
     * Retrieves the file name.
     * @return a file name
     */
    public final String getFileName() {
        return this.fileName;
    }

    /**
     * Retrieves the package name.
     * @return a package name
     */
    public final String getPackageName() {
        return this.packageName;
    }

    /**
     * Retrieves the UID of the containing entity.
     * @return a UID
     */
    public final String getContainerUid() {
        return this.containerUid;
    }

    /**
     * Retrieves the entity's UID.
     * @return a UID
     */
    public final String getEntityUid() {
        return this.entityUid;
    }

    /**
     * A list of super classes.
     * @return a {@code List} of {@code TypeName} objects, or {@code null} 
     * where there are no super classes.
     */
    public final ArrayList<TypeName> getSuperClassList() {
        return this.superClassList;
    }

    /**
     * A list of super types.
     * @return  a {@code List} of {@code TypeName} objects, or {@code null} 
     * where there are no super types
     */
    public final ArrayList<TypeName> getSuperTypeList() {
        return this.superTypeList;
    }

    /** 
     * Retrieves the identifier name.
     * @return an identifier name
     */
    public final String getIdentifierName() {
        return this.identifierName;
    }

    /**
     * Retrieves the method signature.
     * @return a method signature, or {@code null} where the entity is 
     * not a method or constructor
     */
    public final String getMethodSignature() {
        return this.methodSignature;
    }

    /**
     * Retrieves the list of modifiers.
     * @return a {@code List} of modifiers used in the entity declaration, can 
     * be an empty list.
     */
    public final ArrayList<Modifier> getModifiers() {
        return this.modifiers;
    }

    /**
     * Retrieves the species of the entity declaration.
     * @return a species
     */
    public final Species getSpecies() {
        return this.species;
    }

    /**
     * The type name of the declaration.
     * @return a type name
     */
    public final TypeName getTypeName() {
        return this.typeName;
    }

    /**
     * Indicates if the declaration is an array.
     * @return {@code true} if entity is declared as an array
     */
    public final boolean isArrayDeclaration() {
        return this.isArray;
    }
    
    /**
     * Indicates if entity is declared in a loop control statement.
     * @return {@code true} if entity is a loop control variable
     */
    public final boolean isLoopControlVariable() {
        return this.isLoopControlVariable;
    }
    
    /**
     * The line number the entity begins at.
     * @return a line number
     */
    public final int getBeginLineNumber() {
        return this.beginLineNumber;
    }

    /**
     * The column the entity starts at.
     * @return a column number
     */
    public final int getBeginColumn() {
        return this.beginColumn;
    }

    /**
     * The line number the entity ends at.
     * @return a line number
     */
    public final int getEndLineNumber() {
        return this.endLineNumber;
    }

    /**
     * the column the entity ends at.
     * @return a column number
     */
    public final int getEndColumn() {
        return this.endColumn;
    }

    /**
     * A summary of the entity.
     * @return a string listing the properties of the entity.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder( "Package: " );
        output.append( this.packageName );
        output.append( "\nFile: " );
        output.append( this.fileName );
        output.append( "\nIdentifier name: " );
        output.append( this.identifierName );
        output.append( "\nType: ");
        output.append( this.typeName.identifierName() );
        output.append( " | " );
        output.append( this.typeName.fqn() );
        output.append( "\nSpecies: ");
        output.append( this.species.description() );
        output.append( "\nModifiers:" );
        this.modifiers.stream().forEach( (modifier) -> {
            output.append( " " ).append( modifier.description() );
        } );
        if ( this.species == Species.METHOD 
                || this.species == Species.CONSTRUCTOR ) {
            output.append( "\nSignature: " );
            output.append( this.methodSignature );
        }
        output.append( "\nContainer UID: " );
        output.append( this.containerUid );
        output.append( "\nEntity UID: " );
        output.append( this.entityUid );
        output.append( "\n begin line: " );
        output.append( this.beginLineNumber );
        output.append( "  column: " );
        output.append( this.beginColumn );
        output.append( "\n end line: " );
        output.append( this.endLineNumber );
        output.append( "  column: " );
        output.append( this.endColumn );

        return output.toString();
    }

}
