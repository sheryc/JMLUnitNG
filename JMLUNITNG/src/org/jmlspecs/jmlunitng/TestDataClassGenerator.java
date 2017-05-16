
package org.jmlspecs.jmlunitng;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.multijava.mjc.JClassOrGFImportType;
import org.multijava.mjc.JCompilationUnit;
import org.multijava.mjc.JCompilationUnitType;
import org.multijava.mjc.JConstructorDeclaration;
import org.multijava.mjc.JFormalParameter;
import org.multijava.mjc.JMethodDeclaration;
import org.multijava.mjc.JPackageImportType;
import org.multijava.mjc.JTypeDeclarationType;
import org.multijava.util.Utils;

/**
 * Generates the JMLUNITNG_Test_Data class by JMLUNITNG framework. The generated
 * class provides data to run unit tests for the class to be tested using
 * JMLUNITNG testing framework.
 * 
 * @author Rinkesh Nagmoti
 * @version 1.0
 */
public class TestDataClassGenerator implements Constants
{
  /**
   * Represents the class name for the Test class to be generated.
   */
  protected final transient String my_class_name;
  /**
   * Represents the name of the class for which the test will be generated.
   */
  protected final transient String my_class_nm;

  /** Writer class object to print the Test Class. */
  protected final transient Writer my_writer;

  /** String representing the file name and location for Test Class. */
  protected final transient String my_file;

  /**
   * JTypeDeclarationType object which holds information about the class for
   * which the test is to be conducted.
   */
  protected final transient JTypeDeclarationType my_decl_type;

  /**
   * This array represents the list of imported packages.
   */
  protected final transient JPackageImportType[] my_pkgs;

  /**
   * This is the map of primitive data types and their Class names.
   */
  protected final transient Map<String, String> my_primitives;
  /**
   * This is the name of iterator.
   */
  private final transient String my_itname = "my_iter";
  /**
   * This is the boolean representing if the tests need to be generated for
   * public methods or both public and protected methods.
   */
  private final transient boolean my_depricated;
 /**
  * This is the package of the class to be tested.
  */
  private final transient String my_class_package;
  /**
   * This it the array of all imported clsses.
   */
  private final transient JClassOrGFImportType my_imported_classes[];
  /**
   * This is the array of all imported packages.
   */
  private final transient JPackageImportType my_imported_packages[];
  
  /**
   * Constructs JMLUNITNGTestDataClassGenerator Object.
   * 
   * @param the_file_name String file name.
   * @param the_decl JTypeDeclarationType object.
   * @param the_depricated boolean.
   * @param the_cunit_type JCompilationUnitType object.
   * @throws FileNotFoundException Exception if unable to find specified file.
   */
  public TestDataClassGenerator(final String the_file_name,
                                final JTypeDeclarationType the_decl,
                                final JCompilationUnitType the_cunit_type,
                                final boolean the_depricated) throws FileNotFoundException
  {
    my_class_nm = the_decl.ident();
    this.my_decl_type = the_decl;
    this.my_class_name = the_decl.ident() + T_D_POSTFIX;
    my_pkgs = the_cunit_type.importedPackages();
    this.my_file = the_file_name;
    this.my_class_package = the_cunit_type.packageNameAsString();
    this.my_imported_classes = the_cunit_type.importedClasses();
    this.my_imported_packages = the_cunit_type.importedPackages();
    
    
    my_writer = new Writer(this.my_file);
    my_primitives = new HashMap<String, String>();
    my_primitives.put(BOOLEAN, "Boolean");
    my_primitives.put("byte", "Byte");
    my_primitives.put("double", "Double");
    my_primitives.put("float", "Float");
    my_primitives.put("int", "Integer");
    my_primitives.put("long", "Long");
    my_primitives.put("short", "Short");
    my_primitives.put("string", "String");
    my_primitives.put("String", "String");
    my_primitives.put("char", "Character");

    this.my_depricated = the_depricated;

  }

  /**
   * Generate the Test Data methods.
   * 
   * @param the_decl JTypeDeclarationType object.
   * @param the_cunit_type JCompilationUnit object.
   * @param the_iter Iterator object.
   */
  public void createTestDataClass(final JTypeDeclarationType the_decl,
                                  final JCompilationUnit the_cunit_type,
                                  final Iterator<JTypeDeclarationType> the_iter)
  {

    printHeaderImportandJavadoc(the_decl, the_cunit_type);
    printConstructor();
    printUserInputData(the_decl);
    printCombinedIteratorClass(the_iter, the_decl);
    printClassEnd();
  }

  /**
   * This method prints the header import and javadoc for generated class.
   * 
   * @param the_cunit JCompilationUnit object.
   * @param the_decl JTypeDeclarationType object.
   */

  private void printHeaderImportandJavadoc(final JTypeDeclarationType the_decl,
                                           final JCompilationUnit the_cunit)
  {
    my_writer.print("//This class is generated by JMLUNITNG on " + new Date());
    if (the_cunit.packageName().getName().length() > 0)
    {
      my_writer.print("package " + the_cunit.packageName().getName().replace('/', '.') + ";");
    }
    my_writer.newLine(LEVEL1);
    for (int j = 0; j < my_pkgs.length; j++)
    {
      my_writer.print("import " + my_pkgs[j].getName().replace('/', '.') + ".*" + SM_COLN);
    }
    my_writer.print("import org.jmlspecs.jmlunitng.strategies.*" + SM_COLN);
    my_writer.print("import org.testng.*;");
    my_writer.print("import java.util.Iterator;");
    my_writer.print("import org.testng.annotations.*;");
    my_writer.print("import java.util.*;");
    my_writer.newLine(LEVEL1);
    my_writer.print(JDOC_ST);
    my_writer.print(" * This class is the data provider class generated by JMLUNITNG");
    my_writer.print(" * testing framework");
    my_writer.print(" * for class " + the_decl.ident() + PERIOD);
    my_writer.print(" * @author JMLUNITNG");
    my_writer.print(" * @version 1.0");
    my_writer.print(JDOC_END);
    my_writer.print("public class " + my_class_name);
    my_writer.print(BLK_ST);

  }

  /** Prints the constructor of the Test Data class to be generated. */
  private void printConstructor()
  {
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL1);
    my_writer.print(" * Constructs the class object.");
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL1);
    my_writer.print("public" + SPACE + my_class_name + BKTS);
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_END);
    my_writer.newLine(ONE);
  }

  /**
   * Prints the data provider methods.
   * 
   * @param the_method Object of type method.
   * @param the_decl JTypeDeclarationType object.
   */
  private void printClassDataProvider(final Object the_method,
                                      final JTypeDeclarationType the_decl)
  {

    final Object obj = the_method;
    JFormalParameter[] parameters;
    String name;
    if (obj instanceof JConstructorDeclaration)
    {

      final JConstructorDeclaration construct = (JConstructorDeclaration) obj;
      parameters = construct.parameters();
      name = construct.ident() + getCombinedName(parameters);

      for (int i = 0; i < parameters.length; i++)
      {
        printDataTypeMethod(parameters[i], name);
      }
      // printCombinedIteratorMethod(parameters, name);
      printObjectIterator(the_decl);
      // printObjectCombinedIterator(name);
    }
    else if (obj instanceof JMethodDeclaration)
    {
      final JMethodDeclaration method = (JMethodDeclaration) obj;
      parameters = method.parameters();
      name = method.ident() + getCombinedName(parameters);

      for (int i = 0; i < parameters.length; i++)
      {
        printDataTypeMethod(parameters[i], name);
      }
      // printCombinedIteratorMethod(parameters, name);
      printObjectIterator(the_decl);
      // printObjectCombinedIterator(name);
    }

  }

  /**
   * This method print the individual method for each data type in the method
   * which returns the Iterator of the data type.
   * 
   * @param the_parameter JFormalParameter object.
   * @param the_name String method name.
   * @param the_param Boolean.
   */
  private void printDataTypeMethod(final JFormalParameter the_parameter,
                                   final String the_name)
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns the Iterator for individual data type.");
    my_writer.indent(LEVEL2);
    my_writer.print(" * @return" + " Iterator");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    String parameter = the_parameter.typeToString();
    final char new_char = Character.toUpperCase(parameter.charAt(0));
    parameter = parameter.replace(parameter.charAt(0), new_char);

    if (the_parameter.typeToString().equals(STR))
    {
      my_writer.print(PRIVATE + " org.jmlspecs.jmlunitng.strategies." + "StrategyIterator " +
                      STR + UND + the_name + UND + the_parameter.ident() + BKTS);
    }
    else if (!the_parameter.dynamicType().isPrimitive())
    {
      if (the_parameter.typeToString().endsWith(SQ_BCKTS))
      {
        my_writer.print(PRIVATE + " org.jmlspecs.jmlunitng.strategies.StrategyIterator" +
                        SPACE + the_parameter.typeToString().replace(SQ_BCKTS, ARR) + UND +
                        the_name + UND + the_parameter.ident() + BKTS);
      }
      else
      {
        my_writer.print(PRIVATE + SPACE + "org.jmlspecs." + "jmlunitng." +
                        "strategies.StrategyIterator " + the_parameter.typeToString() + UND +
                        the_name + UND + the_parameter.ident() + BKTS);
      }

    }
    else
    {
      my_writer.print(PRIVATE + " org.jmlspecs.jmlunitng.strategies.StrategyIterator " +
                      the_parameter.typeToString() + UND + the_name + UND +
                      the_parameter.ident() + BKTS);
    }
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL3);

    if (the_parameter.typeToString().equals(STR))
    {

      if (the_parameter.typeToString().equals(STR))
      {
        my_writer.print("final StringStrategy " +
                        the_parameter.ident() + UND + "string_Strategy " +
                        "=  new StringStrategy()");
      }
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);

      if (the_parameter.typeToString().equals(STR))
      {
        my_writer.indent(LEVEL5);
        my_writer.print(PUBLIC + SPACE + "StrategyIterator addData()");
        my_writer.indent(LEVEL5);
        my_writer.print(BLK_ST);
        my_writer.indent(LEVEL5 + 2);
        my_writer.print("return get" + UND + STR + UND + the_name + UND +
                        the_parameter.ident() + BKTS + SM_COLN);
        my_writer.indent(LEVEL5);
        my_writer.print(BLK_END);

        my_writer.indent(LEVEL5);
        my_writer.print("public StrategyIterator addDataForAll()");
        my_writer.indent(LEVEL5);
        my_writer.print(BLK_ST);
        my_writer.indent(LEVEL5 + 2);
        my_writer.print(RETURN + SPACE + the_parameter.typeToString() + UND + "for_all()" +
                        SM_COLN);
        my_writer.indent(LEVEL5);
        my_writer.print(BLK_END);
      }
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END + SM_COLN);

      my_writer.indent(LEVEL3);
      my_writer.print(RETURN + SPACE + the_parameter.ident() + UND + "string_Strategy." +
                      "iterator()" + SM_COLN);
    }
    else if (!the_parameter.dynamicType().isPrimitive())
    {
      if (the_parameter.typeToString().endsWith(SQ_BCKTS))
      {
        my_writer.print("final org.jmlspecs.jmlunitng." + "strategies.NewObjectStrategy " +
                        the_parameter.ident() + UND +
                        the_parameter.typeToString().replace(SQ_BCKTS, ARR) + UND + STRGY +
                        SPACE + EQUAL);
      }
      else
      {
        my_writer.print("final org.jmlspecs.jmlunitng.strategies.NewObjectStrategy " +
                        the_parameter.ident() + UND + the_parameter.typeToString() + UND +
                        STRGY + SPACE + EQUAL);
      }
      my_writer.indent(LEVEL4);
      my_writer.print("new org.jmlspecs.jmlunitng.strategies.NewObjectStrategy()");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.print("public StrategyIterator addData()");
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5 + 2);
      if (the_parameter.typeToString().endsWith(SQ_BCKTS))
      {
        my_writer.print(RETURN + SPACE + GETSTR + UND +
                        the_parameter.typeToString().replace(SQ_BCKTS, ARR) + UND + the_name +
                        UND + the_parameter.ident() + BKTS + SM_COLN);
      }
      else
      {
        my_writer.print(RETURN + SPACE + GETSTR + UND + the_parameter.typeToString() + UND +
                        the_name + UND + the_parameter.ident() + BKTS + SM_COLN);
      }
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_END);

      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END + SM_COLN);
      my_writer.indent(LEVEL5 + 2);
      if (the_parameter.typeToString().endsWith(SQ_BCKTS))
      {
        my_writer.print(RETURN + SPACE + the_parameter.ident() + UND +
                        the_parameter.typeToString().replace(SQ_BCKTS, ARR) + "_Strategy" +
                        ".iterator()" + SM_COLN);
      }
      else
      {
        my_writer.print(RETURN + SPACE + the_parameter.ident() + UND +
                        the_parameter.typeToString() + "_Strategy.iterator()" + SM_COLN);
      }
    }
    else
    {
      my_writer.print("final org.jmlspecs.jmlunitng.strategies." + parameter + "Strategy " +
                      the_parameter.ident() + UND + the_parameter.typeToString() +
                      "_Strategy =");
      my_writer.indent(LEVEL4);
      my_writer.print("new org.jmlspecs.jmlunitng.strategies." + parameter + "Strategy()");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.print("public StrategyIterator addData()");
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5 + 2);
      my_writer.print("return get_" + the_parameter.typeToString() + UND + the_name + UND +
                      the_parameter.ident() + BKTS + SM_COLN);
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_END);

      my_writer.indent(LEVEL5);
      my_writer.print("public StrategyIterator addDataForAll()");
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5 + 2);
      my_writer.print(RETURN + SPACE + the_parameter.typeToString() + UND + "for_all" + BKTS +
                      SM_COLN);
      my_writer.indent(LEVEL5);
      my_writer.print(BLK_END);

      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END + SM_COLN);
      my_writer.indent(LEVEL3);
      my_writer.print("return " + the_parameter.ident() + UND + the_parameter.typeToString() +
                      "_Strategy.iterator();");
    }
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
    my_writer.newLine(ONE);
  }

  /**
   * This method generates the name for all parameters together.
   * 
   * @param the_parameters Array of JFormalParameter objects.
   * @return String
   */
  private String getCombinedName(final JFormalParameter[] the_parameters)
  {
    final StringBuilder name = new StringBuilder();
    for (int i = 0; i < the_parameters.length; i++)
    {
      if (the_parameters[i].typeToString().equals(STRARR))
      {
        name.append(UND + ST_ARR);
      }
      else
      {
        name.append(UND + the_parameters[i].typeToString());
      }
    }
    return name.toString();
  }

  /**
   * This method prints the method to return the iterator of objects for given
   * class.
   * 
   * @param the_decl JTypeDeclarationType object.
   */
  private void printObjectIterator(final JTypeDeclarationType the_decl)
  {
    final List<JTypeDeclarationType> allMethods = the_decl.methods();

    final List<JFormalParameter> parameters = new ArrayList<JFormalParameter>();
    for (int cnt = 0; cnt < allMethods.size(); cnt++)
    {
      if (allMethods.get(cnt) instanceof JConstructorDeclaration)
      {
        final JConstructorDeclaration a_construct =
            (JConstructorDeclaration) allMethods.get(cnt);
        final JFormalParameter[] params = a_construct.parameters();
        for (int i = 0; i < params.length; i++)
        {
          parameters.add(params[i]);
        }
      }
    }

    my_writer.newLine(ONE);
  }

  /**
   * This method prints the CombinedIterator class for each method to be tested.
   * 
   * @param the_iter Iterator object.
   * @param the_decl JTypeDeclarationType object.
   */
  private void printCombinedIteratorClass(final Iterator<JTypeDeclarationType> the_iter,
                                          final JTypeDeclarationType the_decl)
  {
    boolean has_construct = false;
    while (the_iter.hasNext())
    {
      final Object obj = the_iter.next();
      if (obj instanceof JConstructorDeclaration)
      {
        has_construct = true;
        final JConstructorDeclaration method = (JConstructorDeclaration) obj;
        printClassObjectIterator(method);
      }
      if (obj instanceof JMethodDeclaration)
      {
        if (obj instanceof JConstructorDeclaration)
        {
          final JConstructorDeclaration method = (JConstructorDeclaration) obj;
          if (method.parameters().length == 0)
          {
            continue;
          }
        }
        final JMethodDeclaration method = (JMethodDeclaration) obj;
        final JFormalParameter[] parameters = method.parameters();
        final String name = method.ident() + getCombinedName(parameters);
        if (!method.isDeprecated() || my_depricated)
        {
          printSingleElementIterator(method, parameters, name, the_decl);
        }
      }
    }
    if (!has_construct)
    {
  
      printClassObjectIterator(the_decl);
    }
  }

  /**
   * This method prints the data members of the class.
   * 
   * @param the_param_num The number of parameters for the method to be tested.
   */
  private void printDataMembers(final int the_param_num, final JMethodDeclaration the_method)
  {
    final boolean is_instance_method = 
      !(the_method instanceof JConstructorDeclaration) &&
      !Utils.hasFlag(the_method.modifiers(), JMethodDeclaration.ACC_STATIC);

    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This is the array of StrategyIterator objects for" +
                    " all parameters.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("protected org.jmlspecs.jmlunitng.strategies.StrategyIterator[] " +
                    my_itname + EQUAL);
    my_writer.indent(LEVEL3);
    my_writer.print("new org.jmlspecs.jmlunitng.strategies.StrategyIterator[" +
                    the_param_num + "];");
    if (is_instance_method)
    {
      my_writer.indent(LEVEL2);
      my_writer.print(JDOC_ST);
      my_writer.indent(LEVEL2);
      my_writer.print(" * Iterator over the newly created objects");
      my_writer.indent(LEVEL2);
      my_writer.print(" * of the class to be tested.");
      my_writer.indent(LEVEL2);
      my_writer.print(JDOC_END);
      my_writer.indent(LEVEL2);
      my_writer.print("protected ClassObjectIterator my_newObjs;");
    }
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * True if it is the first element in iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("protected boolean isFirstElement = true;");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * Array of the objects to be returned by next method.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("protected Object[] my_current_objects;");
    
    if (the_param_num == 0)
    {
      my_writer.indent(LEVEL2);
      my_writer.print(JDOC_ST);
      my_writer.indent(LEVEL2);
      my_writer.print(" * Integer representing the current number of object being used.");
      my_writer.indent(LEVEL2);
      my_writer.print(JDOC_END);
      my_writer.indent(LEVEL2);
      my_writer.print("int cur_obj = 0;");
    }
    my_writer.newLine(ONE);
  }

  /**
   * This method prints the individual data provider.
   * 
   * @param the_method JMethodDeclaration object.
   */
  private void printDataProvider(final JMethodDeclaration the_method)
  {

    final JFormalParameter[] parameters = the_method.parameters();
    final String name = the_method.ident() + getCombinedName(parameters);
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL1);
    my_writer.print(" * This is the actual data provider method used by TestNG.");
    my_writer.indent(LEVEL1);
    my_writer.print(" * @return Iterator");
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL1);
    my_writer.print("@DataProvider(name = \"p_test_" + name + "\")");
    my_writer.indent(LEVEL1);
    my_writer.print("public Iterator<Object[]> p_test_" + name + BKTS);
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL2);
    my_writer.printOnLine("return (Iterator<Object[]>) new CombinedIteratorFor_" +
                          name + BKTS + SM_COLN);
    // my_writer.printOnLine(".getIter_" + name + "();");
    my_writer.printOnLine("\n");
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_END);

  }

  /**
   * This method prints the next method in combined iterator class.
   * 
   * @param the_method JMethodDeclaration object.
   */
  private void printNext(final JMethodDeclaration the_method)
  {
    final JFormalParameter[] parameters = the_method.parameters();
    final String name = the_method.ident() + getCombinedName(parameters);
    final boolean is_instance_method = 
      !(the_method instanceof JConstructorDeclaration) &&
      !Utils.hasFlag(the_method.modifiers(), JMethodDeclaration.ACC_STATIC);
    
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns the next Object[]" + SPACE + "in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(SPACE + AST + SPACE + AT + SPACE + RETURN + " Object[]");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("public Object[] next()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);

    my_writer.indent(LEVEL3);
    my_writer.print("if (isFirstElement)");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    int offset = 0;
    if (is_instance_method)
    {
      my_writer.indent(LEVEL4);
      my_writer.print("my_current_objects[0] = my_newObjs.next()" + SM_COLN);
      offset = 1;
    }
    for (int i = 0; i < parameters.length; i++)
    {
      my_writer.indent(LEVEL4);
      my_writer.print(MY_CURR_OBJS + SQ_BCK_ST + (i + offset) + SQ_BCK_END + EQUAL + my_itname +
                      SQ_BCK_ST + i + SQ_BCK_END + GET);
      my_writer.indent(LEVEL4);
      my_writer.print(my_itname + SQ_BCK_ST + i + SQ_BCK_END + ADV);
    }
    my_writer.indent(LEVEL4);
    my_writer.print("isFirstElement = false;");
    my_writer.indent(LEVEL4);
    my_writer.print("return my_current_objects" + SM_COLN);

    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);

    my_writer.indent(LEVEL3);
    my_writer.print(ELSE);
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);

    my_writer.indent(LEVEL4);
    if (parameters.length > 0)
    {
      my_writer.print("if(" + "!" + my_itname + SQ_BCK_ST + (parameters.length - 1) +
                      SQ_BCK_END + ATEND + BKT_END);
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);

      my_writer.indent(LEVEL5);
      my_writer.print(MY_CURR_OBJS + SQ_BCK_ST + (parameters.length - 1 + offset) + SQ_BCK_END + EQUAL +
                      my_itname + SQ_BCK_ST + (parameters.length - 1) + SQ_BCK_END + GET);
      my_writer.indent(LEVEL5);
      my_writer.print(my_itname + SQ_BCK_ST + (parameters.length - 1) + SQ_BCK_END + ADV);
      if (is_instance_method)
      {
        my_writer.indent(LEVEL5);
        my_writer.print("my_current_objects[0] = my_newObjs.regenerate()" + SM_COLN);
      }
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);

      for (int i = parameters.length - 2; i >= 0; i--)
      {
        my_writer.indent(LEVEL4);
        my_writer.print("else if (!" + my_itname + "[" + i + SQ_BCK_END + ATEND + BKT_END);
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_ST);

        my_writer.indent(LEVEL5);
        my_writer.print(MY_CURR_OBJS + SQ_BCK_ST + (i + offset) + SQ_BCK_END + EQUAL + my_itname +
                        SQ_BCK_ST + i + SQ_BCK_END + GET);
        my_writer.indent(LEVEL5);
        my_writer.print(my_itname + SQ_BCK_ST + i + SQ_BCK_END + ADV);
        for (int j = parameters.length - 1; j > i; j--)
        {
          my_writer.indent(LEVEL5);
          if (parameters[j].typeToString().equals(STRARR))
          {
            my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + ST_ARR + UND +
                            name + UND + parameters[j].ident() + BKTS + SM_COLN);
          }
          else if (parameters[j].typeToString().equals(STR))
          {
            my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + STR + UND + name +
                            UND + parameters[j].ident() + BKTS + SM_COLN);
          }
          else
          {
            my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL +
                            parameters[j].typeToString() + UND + name + UND +
                            parameters[j].ident() + BKTS + SM_COLN);
          }
          my_writer.indent(LEVEL5);
          my_writer.print(MY_CURR_OBJS + SQ_BCK_ST + (j + offset) + SQ_BCK_END + EQUAL + my_itname +
                          SQ_BCK_ST + j + SQ_BCK_END + GET);
          my_writer.indent(LEVEL5);
          my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + ADV);
        }
        if (is_instance_method)
        {
          my_writer.indent(LEVEL5);
          my_writer.print("my_current_objects[0] = my_newObjs.regenerate()" + SM_COLN);
        }
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_END);
      }
   
      my_writer.indent(LEVEL4);
      my_writer.print(ELSE);
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
    }
    
    if (is_instance_method)
    {
      if (parameters.length > 0)
      {
        my_writer.indent(LEVEL5);
      }
      else
      {
        my_writer.indent(LEVEL3);
      }
      my_writer.print("my_current_objects[0] = my_newObjs.next();");
    }
    
    for (int j = parameters.length - 1; j >= 0; j--)
    {
      my_writer.indent(LEVEL5);
      if (parameters[j].typeToString().equals(STRARR))
      {

        my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + ST_ARR + UND + name +
                        UND + parameters[j].ident() + BKTS + SM_COLN);
      }
      else if (parameters[j].typeToString().equals(STR))
      {
        my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + STR + UND + name +
                        UND + parameters[j].ident() + BKTS + SM_COLN);
      }
      else
      {
        my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + EQUAL +
                        parameters[j].typeToString() + UND + name + UND +
                        parameters[j].ident() + BKTS + SM_COLN);
      }
      my_writer.indent(LEVEL5);
      my_writer.print(MY_CURR_OBJS + SQ_BCK_ST + (j + offset) + SQ_BCK_END + EQUAL + my_itname +
                      SQ_BCK_ST + j + SQ_BCK_END + GET);
      my_writer.indent(LEVEL5);
      my_writer.print(my_itname + SQ_BCK_ST + j + SQ_BCK_END + ADV);
    }
    if (parameters.length > 0)
    {
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL4);
    }
    else
    {
      my_writer.indent(LEVEL3);
    }

    my_writer.print("return my_current_objects;");

    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);

    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }

  /**
   * This method prints the next method in combined iterator class.
   */
  private void printHasNext(JMethodDeclaration the_method)
  {
    final boolean is_instance_method = 
      !(the_method instanceof JConstructorDeclaration) &&
      !Utils.hasFlag(the_method.modifiers(), JMethodDeclaration.ACC_STATIC);
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns true if there exists" +
                    " next element in the Iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(" * @return boolean");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print(PUBLIC + " boolean hasNext()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    
    if (is_instance_method)
    {
      my_writer.indent(LEVEL3);
      my_writer.print("if (my_newObjs.hasNext())");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL4);
      my_writer.print("return " + "true;");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL3);

      my_writer.print(ELSE);
    }
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.print("for (int i = 0; i < " + my_itname + ".length; i++)");
    my_writer.indent(LEVEL4);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL5);
    my_writer.print("if (!" + my_itname + "[i].atEnd())");
    my_writer.indent(LEVEL5);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL5 + 2);
    my_writer.print(RETURN + " true;");
    my_writer.indent(LEVEL5);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL4);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL4);
    my_writer.print(RETURN + " false;");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);

    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }

  /**
   * This method prints the next method in combined iterator class.
   */
  private void printRemove()
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This optional operation is not implemented for this type of iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print(PUBLIC + " void remove()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }

  /**
   * This method prints the end of class bracket "{".
   */
  private void printClassEnd()
  {
    my_writer.print(BLK_END);
  }

  /**
   * This is the method to print the methods for user data input.
   * 
   * @param the_decl JTypeDeclarationType object
   */
  private void printUserInputData(final JTypeDeclarationType the_decl)
  {
    final List<Object> methods = the_decl.methods();
    final Set<String> variables = new HashSet<String>();
    boolean has_construct = false;
    boolean has_params = false;
    for (int cnt = 0; cnt < methods.size(); cnt++)
    {
      if (methods.get(cnt) instanceof JConstructorDeclaration)
      {
        has_construct = true;
        final JConstructorDeclaration construst = (JConstructorDeclaration) methods.get(cnt);
        final JFormalParameter[] params = construst.parameters();
        if (params.length != 0)
        {
          has_params = true;
        }
      }
      if (methods.get(cnt) instanceof JMethodDeclaration)
      {
        final JMethodDeclaration method = (JMethodDeclaration) methods.get(cnt);

        final JFormalParameter[] params = method.parameters();
        for (int j = 0; j < params.length; j++)
        {
          if (my_primitives.containsKey(params[j].typeToString()) &&
              !variables.contains(params[j].typeToString()))
          {
            variables.add(params[j].typeToString());
          }
        }
      }
    }

    if (!has_construct || !has_params)
    {
//      my_writer.indent(LEVEL1);
//      my_writer.print(JDOC_ST);
//      my_writer.indent(LEVEL1);
//      my_writer.print(SPACE + AST + " @return Objects of class " + the_decl.ident() + PERIOD);
//      my_writer.indent(LEVEL1);
//      my_writer.print(JDOC_END);
//      my_writer.indent(LEVEL1);
//      my_writer.print(PRIVATE + SPACE + STATIC + SPACE + the_decl.ident() +
//                      SQ_BCKTS + " getUserObjects()");
//      my_writer.indent(LEVEL1);
//      my_writer.print(BLK_ST);
//      my_writer.indent(LEVEL1);
//      my_writer.print(RETURN + SPACE + NEW + SPACE + the_decl.ident() +
//                      SQ_BCKTS + "{/*Please provide the objects of class to be tested.*/};");
//      my_writer.indent(LEVEL1);
//      my_writer.print(BLK_END);
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_ST);
      my_writer.indent(LEVEL1);
      my_writer.print(SPACE + AST + " @return number of objects of class " + 
                      the_decl.ident() + PERIOD);
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_END);
      my_writer.indent(LEVEL1);
      my_writer.print(PRIVATE + SPACE + STATIC + SPACE + "Integer" +
                     " get_num_objects()");
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL1);
      my_writer.print(RETURN + SPACE + "2 /*Please provide the number of" +
                " objects of class to be tested.*/;");
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_END);
    }
    for (String param : variables)
    {
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_ST);
      my_writer.indent(LEVEL1);
      my_writer.print(" * @" + RETURN + SPACE + my_primitives.get(param) + "s for all");
      my_writer.indent(LEVEL1);
      my_writer.print(" *" + SPACE + param + " variables.");
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_END);

      my_writer.indent(LEVEL1);
      my_writer.print(PRIVATE + SPACE + "static StrategyIterator" +
                      SPACE + param + UND + "for" + UND + "all" + BKTS);
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL2);
      my_writer.print(RETURN + SPACE + "new ParameterIterator(new " + my_primitives.get(param) + "[]");
      my_writer.indent(LEVEL3);
      my_writer.print("{ /* enter data for all " + param + " iterators here */ });");
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_END);
    }

    for (int i = 0; i < methods.size(); i++)
    {
//      if (methods.get(i) instanceof JConstructorDeclaration)
//      {
//        if (((JConstructorDeclaration) methods.get(i)).parameters().length > 0)
//        {
//          final JConstructorDeclaration method = (JConstructorDeclaration) methods.get(i);
//          final JFormalParameter[] params = method.parameters();
//          
//          final String name = method.ident() + UND + "objects";
//          for (int j = 0; j < params.length; j++)
//          {
//            my_writer.indent(LEVEL1);
//            my_writer.print(JDOC_ST);
//            my_writer.indent(LEVEL1);
//            my_writer.print(SPACE + AST + SPACE + AT + RETURN + SPACE +
//                            my_primitives.get(params[j].typeToString()) +
//                            "s" + SPACE + "to use for parameter " + 
//                            params[j].ident() + SPACE + "of ");
//            my_writer.indent(LEVEL1);
//            my_writer.printOnLine(SPACE + AST + SPACE + method.ident() + BKT_ST);
//            for (int m = 0; m < method.parameters().length; m++)
//            {
//              my_writer.printOnLine(method.parameters()[m].typeToString());
//              if (m < method.parameters().length - 1)
//              {
//                my_writer.printOnLine(COMMA);
//              }
//            }
//            my_writer.printOnLine(BKT_END + "\n");
//            my_writer.indent(LEVEL1);
//            my_writer.print(JDOC_END);
//
//            if (params[j].typeToString().equals(STR))
//            {
//              my_writer.print(PRIVATE + SPACE + STATIC + SPACE + OBJ + SQ_BCKTS +
//                              SPACE + GETSTR + UND + STR + UND + name +
//                              UND + params[j].ident() + BKTS);            
//            }
//            else if (!params[j].dynamicType().isPrimitive())
//            {
//              if (params[j].typeToString().endsWith(SQ_BCKTS))
//              {
//                my_writer.print(PRIVATE + SPACE + STATIC + SPACE + OBJ +
//                                SQ_BCKTS + SPACE + GETSTR + UND +
//                                params[j].typeToString().replace(SQ_BCKTS, ARR) + UND + name +
//                                UND + params[j].ident() + BKTS);
//              }
//              else
//              {
//                my_writer.print(PRIVATE + SPACE + STATIC  + SPACE + OBJ + 
//                                SQ_BCKTS + SPACE + GETSTR + UND +
//                                params[j].typeToString() + UND + name + UND +
//                                params[j].ident() + BKTS);
//              }
//
//            }
//            else
//            {
//              my_writer.print(PRIVATE + SPACE + STATIC + SPACE + OBJ + 
//                              SQ_BCKTS + SPACE + GETSTR + UND +
//                              params[j].typeToString() + UND + name + UND + params[j].ident() +
//                              BKTS);
//            }
//            my_writer.indent(LEVEL1);
//            my_writer.print(BLK_ST);
//            my_writer.indent(LEVEL2);
//
//            if (!params[j].dynamicType().isPrimitive())
//            {
//              if (params[j].typeToString().endsWith(SQ_BCKTS))
//              {
//                final String param = params[j].typeToString().replace(SQ_BCKTS, "");
//                if (my_primitives.containsKey(param))
//                {
//                  my_writer.print(RETURN + SPACE + NEW + SPACE +
//                                  my_primitives.get(params[j].typeToString()) +
//                                  "[][] {/* Add data elements to generate class objects.*/};");
//                }
//                else
//                {
//                  my_writer.print(RETURN + SPACE + NEW + SPACE + params[j].typeToString() +
//                                  SQ_BCKTS + SPACE + BLK_ST + CMT_ST +
//                                  " Add data elements to generate class objects." + 
//                                  CMT_END + BLK_END + SM_COLN);
//                }
//              }
//              else
//              {
//                my_writer.print(RETURN + " new Object[] " +
//                                " {/* Add data elements to generate class objects.*/};");
//              }
//            }
//            else
//            {
//              my_writer.print(RETURN + SPACE + NEW + SPACE + 
//                              my_primitives.get(params[j].typeToString()) + SQ_BCKTS +
//                              "{/* Add data elements to generate class objects.*/};");
//            }
//            my_writer.indent(LEVEL1);
//            my_writer.print(BLK_END);
//          }
//        }
//        else
//        {
//          my_writer.indent(LEVEL1);
//          my_writer.print(JDOC_ST);
//          my_writer.indent(LEVEL1);
//          my_writer.print(" * @return Objects of class " + the_decl.ident() + PERIOD);
//          my_writer.indent(LEVEL1);
//          my_writer.print(JDOC_END);
//          my_writer.indent(LEVEL1);
//          my_writer.print(PRIVATE + SPACE + STATIC + SPACE + the_decl.ident() +
//                          "[] getUserObjects()");
//          my_writer.indent(LEVEL1);
//          my_writer.print(BLK_ST);
//          my_writer.indent(LEVEL1);
//          my_writer.print(RETURN + SPACE + NEW + SPACE + the_decl.ident() +
//                          "[]{/*Please provide the objects of class to be tested.*/};");
//          my_writer.indent(LEVEL1);
//          my_writer.print(BLK_END);
//        }
//      }
      if (methods.get(i) instanceof JMethodDeclaration)
      {
        final JMethodDeclaration method = (JMethodDeclaration) methods.get(i);
        final JFormalParameter[] params = method.parameters();
        final String name = method.ident() + getCombinedName(params);
        if (my_depricated)
        {
          printGetSingleElement(method, params, name);
        }
        else
        {
          if (!method.isDeprecated())
          {
            printGetSingleElement(method, params, name);
          }
        }

      }
    }
  }

  /**
   * This class prints the user input data method for a single data element.
   * 
   * @param the_method JMethodDeclaration.
   * @param the_params JFormalParameter.
   * @param the_name String.
   */
  private void printGetSingleElement(final JMethodDeclaration the_method,
                       final JFormalParameter[] the_params, final String the_name)
  {
    for (int j = 0; j < the_params.length; j++)
    {
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_ST);
      my_writer.indent(LEVEL1);
      my_writer.print(SPACE + AST + SPACE + AT + RETURN + SPACE +
                      my_primitives.get(the_params[j].typeToString()) +
                      "s to use for parameter " + the_params[j].ident() + " of ");
      my_writer.indent(LEVEL1);
      my_writer.printOnLine(SPACE + AST + SPACE + the_method.ident() + BKT_ST);
      for (int m = 0; m < the_method.parameters().length; m++)
      {
        my_writer.printOnLine(the_method.parameters()[m].typeToString());
        if (m < the_method.parameters().length - 1)
        {
          my_writer.printOnLine(COMMA);
        }
      }
      my_writer.printOnLine(BKT_END + "\n");
      my_writer.indent(LEVEL1);
      my_writer.print(JDOC_END);
      my_writer.indent(LEVEL1);

      if (the_params[j].typeToString().equals(STR))
      {
        my_writer.print(PRIVATE + SPACE + STATIC + SPACE + "StrategyIterator" + 
                        SPACE + GETSTR  +
                        UND + STR + UND + the_name +
                        UND + the_params[j].ident() + BKTS);
      }
      else if (!the_params[j].dynamicType().isPrimitive())
      {
        
        
        if (the_params[j].typeToString().endsWith(SQ_BCKTS))
        {
          my_writer.print(PRIVATE + SPACE + STATIC + SPACE + "StrategyIterator" +
                          SPACE + GETSTR + UND +
                          the_params[j].typeToString().replace(SQ_BCKTS, ARR) + UND +
                          the_name + UND + the_params[j].ident() + BKTS);
        }
        else
        {
          my_writer.print(PRIVATE + SPACE + STATIC  + SPACE + "StrategyIterator" + 
                          SPACE + GETSTR + UND +
                          the_params[j].typeToString() + UND + the_name + UND +
                          the_params[j].ident() + BKTS);
        }

      }
      else
      {
        my_writer.print(PRIVATE + SPACE + STATIC + SPACE + "StrategyIterator" + 
                        SPACE + GETSTR + UND +
                        the_params[j].typeToString() + UND + the_name + UND +
                        the_params[j].ident() + BKTS);
      }
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL2);

      if (!the_params[j].dynamicType().isPrimitive())
      {
        if (the_params[j].typeToString().endsWith(SQ_BCKTS))
        {
          final String param = the_params[j].typeToString().replace(SQ_BCKTS, "");
          if (my_primitives.containsKey(param))
          {
            my_writer.print(RETURN + SPACE + NEW + SPACE +
                            my_primitives.get(the_params[j].typeToString()) +
                            "[][] {/* Add data elements here.*/};");
          }
          else if (param.contains("String"))
          {
            my_writer.print(RETURN + SPACE + NEW + SPACE + "ParameterIterator(new " + 
                            the_params[j].typeToString() +
                          SQ_BCKTS + " {/* " + " Add data elements here." + "*/});");
          }
          else
          {
            printReflectionIterator(the_params[j]);
          }
        }
        else
        {
          printReflectionIterator(the_params[j]);
          //my_writer.print("return new Object[] " + " {/* Add data elements here.*/};");
        }
      }
      else
      {
        my_writer.print(RETURN + SPACE + NEW + SPACE + "ParameterIterator(new " +
                        my_primitives.get(the_params[j].typeToString()) +
                        SQ_BCKTS + "{/* Add data elements here. */});");
      }
      my_writer.indent(LEVEL1);
      my_writer.print(BLK_END);
    }
  }


  private void printReflectionIterator(JFormalParameter the_param)
  {
    my_writer.print("Object[] user_objs = { /* provide any specific objects here */ };");
    my_writer.indent(LEVEL2);
    my_writer.print("Class curr_class = " + my_class_nm + ".class;");
    
    my_writer.indent(LEVEL2);
    my_writer.print("String class_package = curr_class.getPackage().getName();");
    my_writer.indent(LEVEL2);
    my_writer.print("try");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    
    my_writer.indent(LEVEL3);
    my_writer.print("Class obj_class = Class.forName(class_package.replace(\"/\",\".\")+\"." +
                    the_param.typeToString() + "_JML_Test_Data$ClassObjectIterator" + "\");");
    my_writer.indent(LEVEL3);
    my_writer.print("Iterator<Object> itr = (Iterator<Object>) obj_class.newInstance" + BKTS + SM_COLN);
    my_writer.indent(LEVEL3);
    my_writer.print("return new MultiIterator(new IteratorAdapter(itr), new ParameterIterator(user_objs));");
    my_writer.print(BLK_END);
    my_writer.print("catch (Throwable e)");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    
    my_writer.indent(LEVEL3);
    my_writer.print("// e.printStackTrace();");
    my_writer.indent(LEVEL3);
    my_writer.print(RETURN + SPACE + "new ParameterIterator(user_objs);");

    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);  
    my_writer.print("\n");
  }
  
  /**
   * Prints the combined iterator class.
   * @param the_method JMethodDeclaration object.
   * @param the_parameters JFormalParameter array
   * @param the_name Name String.
   * @param the_decl JTypeDeclarationType object.
   */
  private void printSingleElementIterator(final JMethodDeclaration the_method,
                                          final JFormalParameter[] the_parameters,
                                          final String the_name,
                                          final JTypeDeclarationType the_decl)
  {
    printDataProvider(the_method);
    my_writer.newLine(LEVEL1);
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL1);
    my_writer.print(" * This class is the CombinedIterator for method " + the_name +
                    PERIOD);
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL1);
    my_writer.print("public static class CombinedIteratorFor_" + the_name +
                    " implements Iterator<Object[]>");
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_ST);
    my_writer.newLine(ONE);
    printDataMembers(the_parameters.length, the_method);
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST + " This is the constructor for CombinedIteratorFor" +
                    the_name + ".*/");
    my_writer.indent(LEVEL2);
    my_writer.print("public CombinedIteratorFor_" + the_name + BKTS);
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);

    my_writer.indent(LEVEL3);
    int offset = 1;
    final boolean is_instance_method = 
      !(the_method instanceof JConstructorDeclaration) &&
      !Utils.hasFlag(the_method.modifiers(), JMethodDeclaration.ACC_STATIC);
    if (!is_instance_method)
    {
      offset = 0;
    }
    my_writer.print("my_current_objects = new Object" + SQ_BCK_ST +
                    (the_method.parameters().length + offset) + SQ_BCK_END + SM_COLN);
    
    if (is_instance_method)
    {
      my_writer.indent(LEVEL3);
      my_writer.print("my_newObjs = new ClassObjectIterator();");
    }
    
    for (int i = 0; i < the_parameters.length; i++)
    {
      my_writer.indent(LEVEL3);
      if (the_parameters[i].typeToString().equals(STRARR))
      {

        my_writer.print(my_itname + SQ_BCK_ST + i + SQ_BCK_END + EQUAL + ST_ARR + UND +
                        the_name + UND + the_parameters[i].ident() + BKTS + SM_COLN);
      }
      else if (the_parameters[i].typeToString().equals(STR))
      {
        my_writer.print(my_itname + SQ_BCK_ST + i + SQ_BCK_END + EQUAL + STR + UND + the_name +
                        UND + the_parameters[i].ident() + BKTS + SM_COLN);
      }
      else
      {
        my_writer.print(my_itname + SQ_BCK_ST + i + SQ_BCK_END + EQUAL +
                        the_parameters[i].typeToString() + UND + the_name + UND +
                        the_parameters[i].ident() + BKTS + SM_COLN);
      }
    }

    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
    my_writer.newLine(LEVEL1);
    printClassDataProvider(the_method, the_decl);
    printHasNext(the_method);
    printNext(the_method);
    printRemove();
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_END);

  }

  /**
   * Prints the class object iterator.
   * 
   * @param the_construct JConstructorDeclaration object.
   */
  public void printClassObjectIterator(final Object the_obj)
  {
    my_writer.newLine(1);
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL1);
    my_writer.print(" * This class is the Iterator for class objects" + PERIOD);
    my_writer.indent(LEVEL1);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL1);
    my_writer.print(PUBLIC + " static class ClassObjectIterator implements Iterator<Object>");
    my_writer.indent(LEVEL1);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This is an array to hold current parameters for object construction.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("Object[] parameters;");
    my_writer.indent(LEVEL2);
    my_writer.print("StrategyIterator[] iterators;");
    my_writer.indent(LEVEL2);
    my_writer.print("boolean is_first_time;");
      
    if (the_obj instanceof JConstructorDeclaration)
    {
      final JConstructorDeclaration the_construct = (JConstructorDeclaration) the_obj;
      final JFormalParameter[] params = the_construct.parameters();
      final String com_name = getCombinedName(params);
      if (the_construct.parameters().length == 0)
      {
        my_writer.indent(LEVEL2);
        my_writer.print("int cur_obj_num;");
      }
      my_writer.newLine(1);
      
      my_writer.indent(LEVEL2);
      my_writer.print(PUBLIC + SPACE + "ClassObjectIterator" + BKTS);
      my_writer.indent(LEVEL2);
      my_writer.print(BLK_ST);
  
      my_writer.indent(LEVEL3);
      my_writer.print("parameters " + EQUAL + SPACE + NEW + SPACE + OBJ + SQ_BCK_ST +
                      params.length + SQ_BCK_END + SM_COLN);
      my_writer.indent(LEVEL3);
      my_writer.print("iterators " + EQUAL + SPACE + NEW + SPACE + "StrategyIterator" + 
                      SQ_BCK_ST + params.length + SQ_BCK_END + SM_COLN);
      my_writer.indent(LEVEL3);
      my_writer.print("is_first_time = true;");
  
      for (int i = 0; i < params.length; i++)
      {
        my_writer.indent(LEVEL3);
        if (params[i].typeToString().equals(STRARR))
        {
  
          my_writer.print(ITR + SQ_BCK_ST + i + SQ_BCK_END + EQUAL + ST_ARR + UND +
                          the_construct.ident() + com_name + UND + params[i].ident() +
                          BKTS + SM_COLN);
        }
        else if (params[i].typeToString().equals(STR))
        {
          my_writer.print(ITR + SQ_BCK_ST + i + SQ_BCK_END + EQUAL + STR + UND +
                          the_construct.ident() + com_name + UND + params[i].ident() + 
                          BKTS + SM_COLN);
        }
        else
        {
          my_writer.print(ITR + SQ_BCK_ST + i + SQ_BCK_END + EQUAL +
                          params[i].typeToString() + UND + the_construct.ident() + com_name +
                          UND + params[i].ident() + BKTS + SM_COLN);
        }
      }

      my_writer.indent(LEVEL2);
      my_writer.print(BLK_END);
      my_writer.newLine(1);
    
      for (int i = 0; i < params.length; i++)
      {
    
        printDataTypeMethod(params[i], the_construct.ident() + com_name);
    
      }
   
    
      printObjectHasNext(the_construct);
      printObjectNext(the_construct, com_name);
      printObjectRegenerate(the_construct);
      printObjectRemove();
    }
    else if (the_obj instanceof JTypeDeclarationType)
    {
      final JTypeDeclarationType type_decl = (JTypeDeclarationType) the_obj;
      
      my_writer.indent(LEVEL2);
      my_writer.print("int cur_obj_num;");
      my_writer.indent(LEVEL2);
      my_writer.print(PUBLIC + SPACE + "ClassObjectIterator" + BKTS);
      my_writer.indent(LEVEL2);
      my_writer.print(BLK_ST);
      my_writer.print("is_first_time = true;");
      my_writer.indent(LEVEL2);
      my_writer.print(BLK_END);
      
      printObjectHasNext(type_decl);
      printObjectNext(type_decl);
      printObjectRegenerate(type_decl);
      printObjectRemove();
    }

    my_writer.indent(LEVEL1);
    my_writer.print(BLK_END);
  }

  /**
   * This method prints the hasNext method for class object iterator.
   * @param the_construct JConstructorDeclaration object.
   */
  private void printObjectHasNext(final Object the_obj)
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns true if there exists an" +
                    " element in the Iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(SPACE + AST + SPACE + AT + RETURN + SPACE + BOOLEAN);
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print("public boolean hasNext()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    if (the_obj instanceof JConstructorDeclaration)
    { 
      
      final JConstructorDeclaration the_construct = (JConstructorDeclaration) the_obj;
    
      if (the_construct.parameters().length == 0)
      {
        my_writer.indent(LEVEL3);
        my_writer.print("if (cur_obj_num<=get_num_objects())");
        my_writer.indent(LEVEL3);
        my_writer.print(BLK_ST);
        my_writer.indent(LEVEL4);
        my_writer.print(RETURN + " true;");
        my_writer.indent(LEVEL3);
        my_writer.print(BLK_END);
      }
      else
      {
        my_writer.indent(LEVEL3);
        my_writer.print("for (int i = 0; i <" + SPACE + "iterators.length; i++)");
        my_writer.indent(LEVEL3);
        my_writer.print(BLK_ST);
        my_writer.indent(LEVEL4);
        my_writer.print("if (!iterators[i].atEnd())");
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_ST);
        my_writer.indent(LEVEL5);
        my_writer.print("return true;");
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_END);
        my_writer.indent(LEVEL3);
        my_writer.print(BLK_END);
        
      }
    }
    else if (the_obj instanceof JTypeDeclarationType)
    {
     
      my_writer.indent(LEVEL3);
      my_writer.print("if (cur_obj_num<=get_num_objects())");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL4);
      my_writer.print(RETURN + " true;");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_END);
      
    }
    my_writer.indent(LEVEL3);   
    my_writer.print("return false;");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }
  /**
   * This method prints the object next body if the constructor is not parameterized.
   * @param the_construct JConstructorDeclaration object.
   */
  private void printObjectNextBody(final String the_construct)
  {
    my_writer.indent(LEVEL3);
    my_writer.print("if (cur_obj_num<=get_num_objects())");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.print("cur_obj_num++;");
    my_writer.indent(LEVEL4);
    my_writer.print(RETURN + SPACE + NEW + SPACE + the_construct + BKTS + SM_COLN);
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);
    
    my_writer.indent(LEVEL3);
    my_writer.print("else");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.print("return null;");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);
  }
  
  
  private void printObjectNext(final JTypeDeclarationType the_decl)
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns the next Object" + " in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(SPACE + AST + SPACE + AT + SPACE + RETURN + " Object");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("public Object next()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    printObjectNextBody(the_decl.ident());
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }
  /**
   * This method prints the next method for class object iterator.
   * @param the_construct JConstructorDeclaration object.
   * @param the_name String.
   */
  private void printObjectNext(final JConstructorDeclaration the_construct, final String the_name)
  {
    final JFormalParameter[] params = the_construct.parameters();
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method returns the next Object" + " in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(SPACE + AST + SPACE + AT + SPACE + RETURN + " Object");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);
    my_writer.indent(LEVEL2);
    my_writer.print("public Object next()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);

    
    if (the_construct.parameters().length == 0)
    {
      printObjectNextBody(the_construct.ident());
    }
    else
    {
      my_writer.indent(LEVEL3);
      my_writer.print("Object result = null;");
      my_writer.indent(LEVEL3);
      my_writer.print("while (result == null && hasNext())");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL3);
      my_writer.print("if (is_first_time)");
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_ST);
  
      for (int i = 0; i < params.length; i++)
      {
        my_writer.indent(LEVEL4);
        my_writer.print(PARAMS + SQ_BCK_ST + (i) + SQ_BCK_END + EQUAL + SPACE +
                        ITR + SQ_BCK_ST + i + SQ_BCK_END + GET);
        my_writer.indent(LEVEL4);
        my_writer.print(ITR + SQ_BCK_ST + i + SQ_BCK_END + ADV);
      }
      my_writer.indent(LEVEL4);
      my_writer.print("is_first_time = false;");
      my_writer.indent(LEVEL4);
      my_writer.print("try");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.printOnLine("result = " + SPACE + NEW + SPACE + the_construct.ident() + BKT_ST);
  
      for (int i = 0; i < params.length; i++)
      {
        if (my_primitives.containsKey(params[i].typeToString()))
        {
          my_writer.printOnLine(BKT_ST + 
                                my_primitives.get(params[i].typeToString()) + BKT_END);
        }
        else if (params[i].typeToString().endsWith(SQ_BCKTS))
        {
          if (my_primitives.containsKey(params[i].typeToString().replace(SQ_BCKTS, "")))
          {
            my_writer.printOnLine(BKT_ST +
                                  my_primitives.get(params[i].typeToString().
                                                    replace(SQ_BCKTS, "")) + BKT_END);
          }
          else
          {
            my_writer.printOnLine(BKT_ST + params[i].typeToString() + BKT_END);
          }
        }
        else
        {
          my_writer.printOnLine(BKT_ST + params[i].typeToString() + BKT_END);
        }
  
        my_writer.printOnLine(PARAMS + SQ_BCK_ST + i + SQ_BCK_END);
        if (i < (params.length - 1))
        {
          my_writer.printOnLine(COMMA);
        }
      }
  
      my_writer.printOnLine(BKT_END + SM_COLN + "\n");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL4);
      my_writer.print(CATCH + SPACE + "(final Throwable e)");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.print("// construction error");
      my_writer.indent(LEVEL5);
      my_writer.print("result = null;");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_END);
  
      my_writer.indent(LEVEL3);
      my_writer.print(ELSE);
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_ST);
  
      my_writer.indent(LEVEL4);
      my_writer.print("if" + "(!iterators" + SQ_BCK_ST + (params.length - 1) + SQ_BCK_END +
                      ATEND + BKT_END);
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
  
      my_writer.indent(LEVEL5);
      my_writer.print(PARAMS + SQ_BCK_ST + (params.length - 1) + SQ_BCK_END + EQUAL +
                      " iterators" + SQ_BCK_ST + (params.length - 1) + SQ_BCK_END + GET);
      my_writer.indent(LEVEL5);
      my_writer.print(ITR + SQ_BCK_ST + (params.length - 1) + SQ_BCK_END + ADV);
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
  
      for (int i = params.length - 2; i >= 1; i--)
      {
        my_writer.indent(LEVEL4);
        my_writer.print("else if (!iterators" + SQ_BCK_ST + i + SQ_BCK_END + ATEND + BKT_END);
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_ST);
  
        my_writer.indent(LEVEL5);
        my_writer.print(PARAMS + SQ_BCK_ST + (i) + SQ_BCK_END + EQUAL + SPACE +
                        ITR + SQ_BCK_ST + i + SQ_BCK_END + GET);
        my_writer.indent(LEVEL5);
        my_writer.print(ITR + SQ_BCK_ST + i + SQ_BCK_END + ADV);
        for (int j = params.length - 1; j > i; j--)
        {
          my_writer.indent(LEVEL5);
          if (params[j].typeToString().equals(STRARR))
          {
  
            my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + ST_ARR + UND +
                            the_construct.ident() + the_name + UND + 
                            params[j].ident() + BKTS + SM_COLN);
          }
          else if (params[j].typeToString().equals(STR))
          {
            my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + STR + UND +
                            the_construct.ident() + the_name + UND + 
                            params[j].ident() + BKTS + SM_COLN);
          }
          else
          {
            my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL +
                            params[j].typeToString() + UND + the_construct.ident() + 
                            the_name + UND +
                            params[j].ident() + BKTS + SM_COLN);
          }
          my_writer.indent(LEVEL5);
          my_writer.print(PARAMS + SQ_BCK_ST + (j) + SQ_BCK_END + EQUAL + ITR +
                          SQ_BCK_ST + j + SQ_BCK_END + GET);
          my_writer.indent(LEVEL5);
          my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + ADV);
        }
        my_writer.indent(LEVEL4);
        my_writer.print(BLK_END);
      }
  
      my_writer.indent(LEVEL4);
      my_writer.print(ELSE);
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
  
      for (int j = params.length - 1; j >= 1; j--)
      {
        my_writer.indent(LEVEL5);
        if (params[j].typeToString().equals(STRARR))
        {
  
          my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + ST_ARR + UND +
                          the_construct.ident() + the_name + UND + 
                          params[j].ident() + BKTS + SM_COLN);
        }
        else if (params[j].typeToString().equals(STR))
        {
          my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL + STR + UND +
                          the_construct.ident() + the_name + UND + 
                          params[j].ident() + BKTS + SM_COLN);
        }
        else
        {
          my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + EQUAL +
                          params[j].typeToString() + UND + the_construct.ident() + 
                          the_name + UND +
                          params[j].ident() + BKTS + SM_COLN);
        }
        my_writer.indent(LEVEL5);
        my_writer.print(PARAMS + SQ_BCK_ST + (j) + SQ_BCK_END + EQUAL + ITR +
                        SQ_BCK_ST + j + SQ_BCK_END + GET);
        my_writer.indent(LEVEL5);
        my_writer.print(ITR + SQ_BCK_ST + j + SQ_BCK_END + ADV);
      }
  
      my_writer.indent(LEVEL5);
      my_writer.print(PARAMS + SQ_BCK_ST + 0 + SQ_BCK_END + EQUAL + ITR +
                      SQ_BCK_ST + 0 + SQ_BCK_END + GET);
      my_writer.indent(LEVEL5);
      my_writer.print(ITR + SQ_BCK_ST + 0 + SQ_BCK_END + ADV);
  
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
  
      my_writer.indent(LEVEL4);
      my_writer.print("try");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.printOnLine("result = " + SPACE + NEW + SPACE + the_construct.ident() + BKT_ST);
  
      for (int i = 0; i < params.length; i++)
      {
        if (my_primitives.containsKey(params[i].typeToString()))
        {
          my_writer.printOnLine(BKT_ST + 
                                my_primitives.get(params[i].typeToString()) + BKT_END);
        }
        else if (params[i].typeToString().endsWith(SQ_BCKTS))
        {
          if (my_primitives.containsKey(params[i].typeToString().replace(SQ_BCKTS, "")))
          {
            my_writer.printOnLine(BKT_ST +
                                  my_primitives.get(params[i].typeToString().
                                                    replace(SQ_BCKTS, "")) + BKT_END);
          }
          else
          {
            my_writer.printOnLine(BKT_ST + params[i].typeToString() + BKT_END);
          }
        }
        else
        {
          my_writer.printOnLine(BKT_ST + params[i].typeToString() + BKT_END);
        }
  
        my_writer.printOnLine(PARAMS + SQ_BCK_ST + i + SQ_BCK_END);
        if (i < (params.length - 1))
        {
          my_writer.printOnLine(COMMA);
        }
      }
  
      my_writer.printOnLine(BKT_END + SM_COLN + "\n");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL4);
      my_writer.print("catch (final Throwable e)");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_ST);
      my_writer.indent(LEVEL5);
      my_writer.print("// construction error");
      my_writer.indent(LEVEL5);
      my_writer.print("result = null;");
      my_writer.indent(LEVEL4);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL3);
      my_writer.print(BLK_END);
      my_writer.indent(LEVEL3);
      my_writer.print("return result;");
    }
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }

  /**
   * This method prints the remove method for class object iterator.
   */
  private void printObjectRemove()
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method removes the next Object in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print("public void remove()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }
  
  private void printObjectRegenerate(final JTypeDeclarationType the_decl)
  {
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method regenerates the Object in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print("public " + the_decl.ident() + " regenerate()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    
    my_writer.indent(LEVEL3);
    my_writer.print("try");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.printOnLine(RETURN + SPACE + NEW + SPACE + the_decl.ident() + BKT_ST);
    my_writer.printOnLine(BKT_END + SM_COLN + NEWLINE);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL3);
    my_writer.print(CATCH + SPACE + "(final Exception the_exp)");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.print("// construction error");
    my_writer.indent(LEVEL4);
    my_writer.print("return null;");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
    
  }
  /**
   * This method prints the object regenerate method.
   * @param the_construct JConstructorDeclaration object.
   */
  private void printObjectRegenerate(final JConstructorDeclaration the_construct)
  {
    final JFormalParameter[] params = the_construct.parameters();
    
    
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_ST);
    my_writer.indent(LEVEL2);
    my_writer.print(" * This method regenerates the Object in the iterator.");
    my_writer.indent(LEVEL2);
    my_writer.print(JDOC_END);

    my_writer.indent(LEVEL2);
    my_writer.print("public " + the_construct.ident() + " regenerate()");
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_ST);
    
    my_writer.indent(LEVEL3);
    my_writer.print("try");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.printOnLine(RETURN + SPACE + NEW + SPACE + the_construct.ident() + BKT_ST);
    
    for (int i = 0; i < params.length; i++)
    {
      if (params[i].dynamicType().isPrimitive())
      {
        my_writer.printOnLine(BKT_ST + my_primitives.get(params[i].typeToString()) + BKT_END);
      }
      else
      {
        if (params[i].typeToString().endsWith(SQ_BCKTS))
        {
          final String param = params[i].typeToString().replaceAll(SQ_BCKTS, "");
          my_writer.printOnLine(BKT_ST + my_primitives.get(param) + 
                                BKT_END);
        }
        else
        {
          my_writer.printOnLine(BKT_ST + params[i].typeToString() + BKT_END); 
        }
      }
      
      my_writer.printOnLine("parameters" + SQ_BCK_ST + i + SQ_BCK_END);
      
      if (i < params.length - 1)
      {
        my_writer.printOnLine(COMMA + SPACE);
      }
      
    }
    my_writer.printOnLine(BKT_END + SM_COLN + NEWLINE);
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL3);
    my_writer.print("catch (Throwable e)");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_ST);
    my_writer.indent(LEVEL4);
    my_writer.print("return null;");
    my_writer.indent(LEVEL3);
    my_writer.print(BLK_END);
    my_writer.indent(LEVEL2);
    my_writer.print(BLK_END);
  }
}
