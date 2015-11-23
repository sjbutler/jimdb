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
import java.util.HashSet;
import java.util.List;
import uk.ac.open.crc.idtk.Species;

/**
 * Describes the API for the reader classes to implement. 
 * 
 */
public interface DatabaseReader {
    /**
     * Recovers the list of projects stored in the database. The 
     * project names returned consist of the project name separated 
     * from the version by a space.
     * <p>
     * The constituents of the list are likely to change in the future.
     * </p>
     * @return a list of strings  
     */
    public ArrayList<String> getProjectList();
    
    /**
     * Recovers the tokens found in an identifier name. The method is 
     * typography sensitive, so the name is only recognised if all alphabetical
     * characters are the same case as a stored name, and the use of underscores 
     * etc is identical, i.e. i, I and _i are three different identifier names.
     * @param identifierName an identifier name
     * @return a list containing the alphanumeric tokens of the name, or 
     * {@code null} if the name is not found in the database
     */
    public ArrayList<String> getTokensFor( String identifierName );
    
    /**
     * Recovers a list of the packages in a given project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return an {@code ArrayList} of package names. An empty list is returned 
     * if the project name is not found in the database
     * 
     */
    public ArrayList<String> getPackageNamesForProject( String projectName ) ;
    
    
    /**
     * Retrieves the classes found in a given package.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @param packageName a package name
     * @return a list of class names. An empty list is returned 
     * if either the project name or package name is not found in the database
     */
    public ArrayList<String> getClassNamesForPackage( 
            String projectName, 
            String packageName );
    
    /**
     * Retrieves all the unique names of a given species declared in a 
     * project. 
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @param species a species of name
     * @return a list of unique names
     */
    public ArrayList<String> getIdentifierNamesFor( 
            String projectName, 
            Species species );
    
    
    /**
     * Retrieves all the identifier names found in a project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of names
     */
    public ArrayList<String> getIdentifierNamesFor( String projectName );
    
    /**
     * Retrieves a list of a specified number of names of a given species declared 
     * in a particular project. The minimum length of each name can be specified.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @param species a species of name
     * @param count the maximum number of names to return. If there are fewer 
     * names meeting the criteria all those names are returned. Where there are 
     * more names than requested the list will contain a random selection of 
     * {@code count} names is that meet the other criteria.
     * @param minimumLength the minimum length in characters of names returned.
     * A value of zero (0) is interpreted as meaning any length.
     * @return a list of names
     */
    public ArrayList<String> getNameSetFor( String projectName, 
            Species species, 
            Integer count, 
            Integer minimumLength );
    
    
    
    /**
     * Retrieves a list of a specified number of names of a given species declared 
     * in the database. The minimum length of each name can be specified.
     * @param species a species of name
     * @param count the maximum number of names to return. If there are fewer 
     * names meeting the criteria all those names are returned. Where there are 
     * more names than requested the list will contain a random selection of 
     * {@code count} names is that meet the other criteria.
     * @param minimumLength the minimum length in characters of names returned.
     * A value of zero (0) is interpreted as meaning any length.
     * @return a list of names
     */
    public ArrayList<String> getNameSetFor(
            Species species, 
            Integer count, 
            Integer minimumLength);
    
    
    /**
     * Retrieves a list of a specified number of tokenised names of a given 
     * species declared in a particular project. The minimum length of each 
     * name can be specified.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @param species a species of name
     * @param count the maximum number of names to return. If there are fewer 
     * names meeting the criteria all those names are returned. Where there are 
     * more names than requested the list will contain a random selection of 
     * {@code count} names is that meet the other criteria.
     * @param minimumLength the minimum length in characters of names returned.
     * A value of zero (0) is interpreted as meaning any length.
     * @return a list of tokenised names, with a space between each 
     * alphanumeric token
     */
    public ArrayList<String> getTokenisedNameSetFor( String projectName, 
            Species species, 
            Integer count, 
            Integer minimumLength );
    
    /**
     * Retrieves a list of a specified number of tokenised names of a given 
     * species declared in the database. The minimum length of each 
     * name can be specified.
     * @param species a species of name
     * @param count the maximum number of names to return. If there are fewer 
     * names meeting the criteria all those names are returned. Where there are 
     * more names than requested the list will contain a random selection of 
     * {@code count} names is that meet the other criteria.
     * @param minimumLength the minimum length in characters of names returned.
     * A value of zero (0) is interpreted as meaning any length.
     * @return a list of tokenised names, with a space between each 
     * alphanumeric token
     */
    public ArrayList<String> getTokenisedNameSetFor(
            Species species, 
            Integer count, 
            Integer minimumLength);
    
    
    /**
     * Retrieves all the classes declared in a specified project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of class declarations
     */
    public ArrayList<InheritableProgramEntity> getAllClassNamesFor( String projectName );
    
    /**
     * Retrieves all the classes and interfaces declared in a project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of class and interface declarations
     */
    public ArrayList<ProgramEntity> getAllClassesAndInterfacesFor( String projectName );
    
    /**
     * Retrieves the field names declared in a project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of declarations
     */
    public ArrayList<ProgramEntity> getAllFieldNamesFor( String projectName );
    
    /**
     * Retrieves all the formal arguments declared in a project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of declarations
     */
    public ArrayList<ProgramEntity> getAllFormalArgumentNamesFor( String projectName );
    
    /**
     * Recovers all the local variable names declared in a project.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @return a list of declarations
     */
    public ArrayList<ProgramEntity> getAllLocalVariableNamesFor( String projectName );
    
    /**
     * Retrieves the specified class or interface declaration.
     * @param projectName a string consisting of the project name, a space 
     * and the project version
     * @param fqn the FQN of the class or interface declaration
     * @return a class or interface declarations
     */
    @Deprecated
    public InheritableProgramEntity getClassOrInterfaceFor( String projectName, String fqn );
    
    /**
     * Retrieves declarations of classes or interfaces with a given name from 
     * the database.
     * @param className a class or interface name
     * @param targetSpecies either class or interface
     * @return a list of class or interface declarations
     */
    public ArrayList<InheritableProgramEntity> getEntityCandidatesFor( 
            String className, 
            Species targetSpecies );
    
    /**
     * Recovers a list of subclasses of a given class. NB this is a lexical 
     * match and is experimental.
     * @param className a class name
     * @return a list of possible subclass declarations
     */
    public ArrayList<InheritableProgramEntity> getSubClassesFor( String className );
    
    /**
     * Retrieves the immediate implementors and extenders of an interface. 
     * NB this is a lexical match and is experimental.
     * @param interfaceName an interface name
     * @return a list of classes and interfaces that implement or extend the 
     * target interface
     */
    public ArrayList<InheritableProgramEntity> getSubTypesFor( String interfaceName );

    /**
     * Retrieves a set of declarations of a specified species and type group.
     * @param species a species
     * @param maxCount maximum number of declarations to return. 
     * @param typeGroup a type group
     * @return a set of declarations
     */
    public HashSet<ProgramEntity> getEntitySetWhere( 
            Species species, 
            int maxCount, 
            TypeGroup typeGroup );
    
    /**
     * Retrieves all the declarations in a project.
     * @param projectName a project name
     * @param projectVersion a project version
     * @return a list containing all name declarations in a project
     */
    public List<ProgramEntity> getEntitiesFor( String projectName, String projectVersion );

    /**
     * Retrieves all the declarations in a project.
     * @param projectNameAndVersion a string consisting of the project name, a space 
     * and the project version
     * @return a list containing all the name declarations in a project
     */
    public List<ProgramEntity> getEntitiesFor( String projectNameAndVersion );
}
