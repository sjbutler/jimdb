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
import java.util.logging.Logger;
import uk.ac.open.crc.idtk.Modifier;
import uk.ac.open.crc.idtk.Species;

/**
 * This class provides functionality that is only for testing. The 
 * class is not to be used in any other circumstances. 
 *
 */
@Deprecated
public class ProgramEntityDataFactory {

    private static final Logger LOGGER = Logger.getLogger( "uk.ac.open.crc.jimdb");
    
    public static ProgramEntity create(
            String projectName,
            String projectVersion,
            String identifierName, 
            String packageName,  // ?
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
            int endColumn ) {
        LOGGER.warning( "THIS CODE SHOULD ONLY BE INVOKED FOR UNIT TESTING" );
        return new ProgramEntity(
                projectName,
                projectVersion,
                identifierName, 
                packageName,  
                componentWords, 
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
                endColumn );
    }
}
