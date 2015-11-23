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
import java.util.HashMap;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;

/**
 * Creates the correct program entity class from the given arguments.
 *
 */
class ProgramEntityFactory {

    /**
     * Instantiates an appropriate instance of a class in the 
     * {@code ProgramEntity} hierarchy.
     * @param projectName a project name
     * @param projectVersion a project version
     * @param identifierName an identifier name
     * @param packageName a package name
     * @param tokens a {@code List} of tokens
     * @param modifierList a {@code List} of modifiers
     * @param species a species
     * @param containerUid a UID for the containing program entity
     * @param entityUid a UID for the program entity
     * @param type a type name
     * @param resolvableType a FQN, if available
     * @param isArrayDeclaration {@code true} if entity is declared as an array
     * @param isLoopControlVariable {@code true} if entity is declared in loop statement
     * @param fileName a file name
     * @param startLineNumber a line number
     * @param startColumn a column number
     * @param endLineNumber a line number
     * @param endColumn a column number
     * @param methodSignature a method signature
     * @param superClasses a map of super class names and their tokens
     * @param superTypes a map of super type names and their tokens
     * @return an instance of {@code ProgramEntity} or one of its subclasses
     */
    static ProgramEntity create(
            String projectName,
            String projectVersion,
            String identifierName, 
            String packageName,
            ArrayList<String> tokens, 
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
            int endColumn,
            String methodSignature,
            HashMap<String,ArrayList<String>> superClasses,
            HashMap<String,ArrayList<String>> superTypes) {
        
        if ( species.isClassOrInterface() ) {
            return new InheritableProgramEntity(
                    projectName,
                    projectVersion,
                    identifierName, 
                    packageName, 
                    tokens, 
                    modifierList,
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
                    superClasses,
                    superTypes);
        }
        else if ( species.isMethod() || species.isConstructor() ) {
            return new InvokableProgramEntity(
                    projectName,
                    projectVersion,
                    identifierName, 
                    packageName,
                    tokens, 
                    modifierList,
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
                    methodSignature
            );
        }
        else {
            return new ProgramEntity(
                    projectName,
                    projectVersion,
                    identifierName, 
                    packageName,
                    tokens, 
                    modifierList,
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
                    endColumn);
        }
    }
}
