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
 * Represents a {@linkplain ProgramEntity} program entity that can 
 * be inherited. In practice, this means Java classes and interfaces. 
 *
 */
public class InheritableProgramEntity extends ProgramEntity {

    private final HashMap<String,ArrayList<String>> superClasses;
    private final HashMap<String,ArrayList<String>> superTypes;

    /**
     * Constructor.
     * @param projectName {@inheritDoc}
     * @param projectVersion {@inheritDoc}
     * @param identifierName {@inheritDoc}
     * @param packageName {@inheritDoc}
     * @param tokens {@inheritDoc}
     * @param modifierList {@inheritDoc}
     * @param species {@inheritDoc}
     * @param containerUid {@inheritDoc}
     * @param entityUid {@inheritDoc}
     * @param type {@inheritDoc}
     * @param resolvableType {@inheritDoc}
     * @param isArrayDeclaration {@inheritDoc}
     * @param isLoopControlVariable {@inheritDoc}
     * @param fileName {@inheritDoc}
     * @param startLineNumber {@inheritDoc}
     * @param startColumn {@inheritDoc}
     * @param endLineNumber {@inheritDoc}
     * @param endColumn {@inheritDoc}
     * @param superClasses a map of super classes
     * @param superTypes a map of super types
     */
    InheritableProgramEntity(
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
            HashMap<String,ArrayList<String>> superClasses,
            HashMap<String,ArrayList<String>> superTypes
            ) {
        super(  projectName,
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
        this.superClasses = superClasses;
        this.superTypes = superTypes;
    }

    /**
     * Retrieves a map of super class names and their tokens
     * @return A {@code HashMap} of class names mapped to their tokens, 
     * or an empty list.
     */
    public HashMap<String,ArrayList<String>> getSuperClasses() {
        return this.superClasses;
    }
    
    /**
     * Retrieves a map of super type names and their component words. 
     * @return A {@code HashMap} of interface names mapped to their tokens, 
     * or an empty list.
     */
    public HashMap<String,ArrayList<String>> getSuperTypes() {
        return this.superTypes;
    }

}
