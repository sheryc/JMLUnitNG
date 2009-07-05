
package org.jmlspecs.jmlunitng;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.multijava.mjc.CMethod;
import org.multijava.mjc.JCompilationUnit;
import org.multijava.mjc.JCompilationUnitType;
import org.multijava.mjc.JConstructorDeclaration;
import org.multijava.mjc.JMethodDeclaration;
import org.multijava.mjc.JPackageImportType;
import org.multijava.mjc.JTypeDeclarationType;


/**
 * Generates the JMLUNITNG_Test class by JMLUNITNG framework. The generated
 * class runs unit tests for the class to be tested using JMLUNITNG testing
 * framework.
 * 
 * @author Rinkesh Nagmoti
 * @version 1.0
 */
public class TestClassGenerator implements Constants
{

  /**
   * Represents the class name for the Test class to be generated.
   */
  protected String className;
  
  /** Writer class object to print the Test Class. */
  protected Writer writer;

  /** String representing the file name and location for Test Class. */
  protected final String my_file;

  /**
   * JTypeDeclarationType object which holds information about the class for
   * which the test is to be conducted.
   */
  protected JTypeDeclarationType declarationType;

  /**
   * This array represents the list of imported packages.
   */
  protected JPackageImportType[] pkgs;

  /**
   * This is the list of all the methods the class to be tested contains.
   */
 // protected List<Object> my_methods;
  
  /** 
   * Constructs the TestClassGenerator Object.
   * 
   * @param the_file The file to write the generated class to.
   */
  public TestClassGenerator(final /*@ non_null @*/ String the_file)
  {
    this.my_file = the_file;
 //   this.my_methods = null;
   // my_methods = new ArrayList();
    try
    {
      writer = new Writer(the_file);
    }
    catch (final FileNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  /** Calls other methods to generate the Test Class.
   * @param the_decl
   * @param the_cUnitType
   */
  public void createTest(final JTypeDeclarationType the_decl, 
                         final JCompilationUnit the_cUnitType, final Iterator the_Iter)
  {

    printHeaderImportandJavadoc(the_decl, the_cUnitType);
    printConstructor();
    createTestMethods(the_Iter);

  }

  /** Prints Class header import statements and class Javadoc comment.
   * @param the_decl
   * @param the_cUnitType
   */
  private void printHeaderImportandJavadoc(final JTypeDeclarationType the_decl,
                                           final JCompilationUnitType the_cUnitType)
  {

    this.declarationType = the_decl;
    this.className = the_decl.ident() + TEST_CLASS_NAME_POSTFIX;
    pkgs = the_cUnitType.importedPackages();
    
    writer.print("This class is generated by JMLUNITNG on " + new Date());
    writer.newLine(2);
    for (int j = 0; j < pkgs.length; j++)
    {
      writer.print("import " + pkgs[j].getName().replace('/', '.') + ".*;");
    }

    writer.print("/** This class is the test oracle generated by JMLUNITNG");
    writer.print(" testing framework");
    writer.print(" *  for class " + className);
    writer.print(" */");
    writer.print("/*@Test");
    writer.print("@DataProvider = params" + className);
    writer.print("@*/");
    writer.print("public class " + className + " {");

  }

  /** Prints the constructor of the Test class to be generated.*/
  private void printConstructor()
  {
    writer.print("/** Constructs the class object.*/");
    writer.print("public " + className + DOT_JAVA + " () {");
    writer.print("}");
  }

  /** Creates and prints the methods generated for Testing the methods.
   * @param the_decl
   * @param the_cUnitType
   */
  private void createTestMethods(final Iterator the_method_Iterator)
  {
   
//   if(the_decl.getCClass().getAllInheritedMethods().isEmpty() == false)
//     my_methods.addAll(the_decl.getCClass().getAllInheritedMethods());
          
    while(the_method_Iterator.hasNext())
    {
      Object obj = the_method_Iterator.next();
      if (obj instanceof JConstructorDeclaration)
      {
        JConstructorDeclaration construct = (JConstructorDeclaration) obj;
       
        CMethod method = construct.getMethod();
      
      }
      else if (obj instanceof JMethodDeclaration)
      {
        JMethodDeclaration jMethod = (JMethodDeclaration) obj;
      
        CMethod method = jMethod.getMethod();
       
      }
//      final MethodToBeTested method = new MethodToBeTested(the_method_Iterator.next());
//     
//      printMethodJavaDoc(method);
//      final String name = generateMethodName(method);
//      writer.print("public void " + name + "() {");
//      writer.print("}");
//   
    }
  }

  /** Generates the unique names for methods.
   * @param the_method
   * @return String
   */
  private String generateMethodName(final MethodToBeTested the_method)
  {
    final StringBuilder name = new StringBuilder();
    name.append("test");
    final Parameter[] pams = the_method.getParaters();
    for (int i = 0; i < pams.length; i++)
    {
      name.append("_" + pams[i]);
    }
    return name.toString();

  }

  /** Prints Javadoc comment for method.
   * @param the_method
   */
  private void printMethodJavaDoc(final MethodToBeTested the_method)
  {
    writer.print("/** This method is a test for method " + the_method.getName() + "from the ");
    writer.print(" * Classs to  be tested.");
    writer.print("*/");
  }

}
