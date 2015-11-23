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

/**
 * Represents a name for a constructor or method.
 */
public class InvokableProgramEntity extends ProgramEntity {
    
    private final String methodSignature;
    private final int argumentCount;
    
    /**
     * Constructor.
     * @param projectName a project name
     * @param projectVersion a version
     * @param identifierName an identifier name
     * @param packageName a package name
     * @param tokens a list of tokens
     * @param modifierList a list of modifiers
     * @param species the species of the declaration
     * @param containerUid a UID for the containing declaration
     * @param entityUid a UID for the declaration
     * @param type a type name
     * @param resolvableType a resolved type name
     * @param isArrayDeclaration array declaration?
     * @param isLoopControlVariable loop controller?
     * @param fileName a file name
     * @param startLineNumber a line number
     * @param startColumn a column number
     * @param endLineNumber a line number
     * @param endColumn a column number
     * @param methodSignature a method signature
     */
    public InvokableProgramEntity(
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
            String methodSignature
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
        
        this.methodSignature = methodSignature;
        this.argumentCount = methodSignature.split( ";" ).length - 1;
    }
    
    /**
     * Recovers the method signature for the method or constructor.
     * @return a method signature
     */
    public String getMethodSignature() {
        return this.methodSignature;
    }

    /**
     * Recovers the number of arguments to the method. NB the '...' varargs 
     * notation is counted as one formal parameter.
     * @return the number of formal parameters
     */
    public int argumentCount() {
        return this.argumentCount;
    }
}
