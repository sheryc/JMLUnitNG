
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
import org.multijava.mjc.JFormalParameter;
import org.multijava.mjc.JMethodDeclaration;
import org.multijava.mjc.JPackageImportType;
import org.multijava.mjc.JTypeDeclaration;
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
    printDataMembers();
    printConstructor();
    printMainMethod();
    createTestMethods(the_Iter, the_decl);
    printClassEnd();
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
    
    writer.print("//This class is generated by JMLUNITNG on " + new Date());
    writer.newLine(2);
    for (int j = 0; j < pkgs.length; j++)
    {
      writer.print("import " + pkgs[j].getName().replace('/', '.') + ".*;");
    }
   
    writer.print("import org.multijava.*;");
   
    writer.print("import org.testng.*;");
   
    writer.print("import org.jmlspecs.*;");
    writer.newLine(2);
    writer.print("/** This class is the test oracle generated by JMLUNITNG");
    writer.print(" * testing framework");
    writer.print(" * for class " + the_decl.ident());
    writer.print(" */");
    writer.print("public class " + className + " extends " +
                 className + "_Data");
    writer.print("{");
  }

  /** Prints the constructor of the Test class to be generated.*/
  private void printConstructor()
  {
    writer.indent(2);
    writer.print("/**");
    writer.indent(2);
    writer.print(" * Constructs the class object.");
    writer.indent(2);
    writer.print(" */");
    writer.indent(2);
    writer.print("public " + className + " ()");
    writer.indent(2);
    writer.print("{");
    writer.indent(2);
    writer.print("}");
    writer.newLine(2);
  }

  /** Creates and prints the methods generated for Testing the methods.
   * @param the_decl
   * @param the_method_Iterator
   */
  private void createTestMethods(final Iterator the_method_Iterator,
                                 final JTypeDeclarationType the_decl)
  {
   
    while (the_method_Iterator.hasNext())
    {
      Object obj = the_method_Iterator.next();
      
      if (obj instanceof JConstructorDeclaration)
      {
        JConstructorDeclaration construct = (JConstructorDeclaration)obj;
        final String name = generateMethodName(obj);
        printMethodJavaDoc(obj, name);
        
        writer.indent(4);
        writer.printOnLine("public void " + name + "(" + the_decl.ident() + " obj");
        for (int i = 0; i < construct.parameters().length; i++)
          writer.printOnLine(", " + construct.parameters()[i].typeToString() + 
                             " " + construct.parameters()[i].ident());
        writer.printOnLine(")");
        writer.printOnLine("\n");
        writer.indent(4);
        writer.print("{");
        writer.indent(6);
        writer.print("try");
        writer.indent(8);
        writer.print("{");
        writer.indent(10);
        writer.printOnLine("obj." + construct.ident() + " (");
        for (int i = 0; i < construct.parameters().length; i++)
        {
          writer.printOnLine(construct.parameters()[i].ident());
          if (i != construct.parameters().length-1)
          {
            writer.printOnLine(", ");
          }
        } 
        writer.printOnLine(");");
        writer.printOnLine("\n");
        writer.indent(8);
        writer.print("}");
        writer.indent(6);
        writer.print("catch (PreconditionSkipException the_exp)");
        writer.indent(8);
        writer.print("{");
        writer.indent(10);
        writer.print("System.out.println(the_exp.printStackTrace());");
        writer.indent(8);
        writer.print("}");
        writer.indent(4);
        writer.print("}");
        writer.newLine(1);
      }
      else if (obj instanceof JMethodDeclaration)
      {
        final String name = generateMethodName(obj);
        printMethodJavaDoc(obj, name);
        JMethodDeclaration method = (JMethodDeclaration)obj;
        writer.indent(4);
        writer.printOnLine("public void " + name + "(" + the_decl.ident() + " obj");
        for(int i = 0; i < method.parameters().length; i++)
          writer.printOnLine(", " + method.parameters()[i].typeToString() + 
                             " " + method.parameters()[i].ident());
        writer.printOnLine(")");
        writer.printOnLine("\n");
        writer.indent(4);
        writer.print("{");
        writer.indent(6);
        writer.print("try");
        writer.indent(8);
        writer.print("{");
        writer.indent(10);
        writer.printOnLine("obj." + method.ident() + " (");
        for (int i = 0; i < method.parameters().length; i++)
        {
          writer.printOnLine(method.parameters()[i].ident());
          if (i != method.parameters().length - 1)
          {
            writer.printOnLine(", ");
          }
        } 
        writer.printOnLine(");");
        writer.printOnLine("\n");
        writer.indent(8);
        writer.print("}");
        writer.indent(6);
        writer.print("catch (PreconditionSkipException the_exp)");
        writer.indent(8);
        writer.print("{");
        writer.indent(10);
        writer.print("System.out.println(the_exp.printStackTrace());");
        writer.indent(8);
        writer.print("}");
        writer.indent(4);
        writer.print("}");
        writer.newLine(1);
      }
    }
  }

  /** Generates the unique names for methods.
   * @param the_method
   * @return String
   */
  private String generateMethodName(final Object the_method)
  {
    final StringBuilder name = new StringBuilder();
    name.append("test");
    
    if(the_method instanceof JConstructorDeclaration)
    {
      JConstructorDeclaration construct = (JConstructorDeclaration) the_method; 
      name.append("_" + construct.ident());
      final JFormalParameter[] pams = construct.parameters();
      for (int i = 0; i < pams.length; i++)
      {
        name.append("_" + pams[i].typeToString());
      }
      return name.toString();
      
    }
    else if (the_method instanceof JMethodDeclaration)
    {
      JMethodDeclaration method = (JMethodDeclaration) the_method;
      name.append("_" + method.ident());
      final JFormalParameter[] pams = method.parameters();
      for (int i = 0; i < pams.length; i++)
      {
        name.append("_" + pams[i].typeToString());
      }
      return name.toString();
    }
    else 
    {
      return null;
    }
  }

  /** Prints Javadoc comment for method.
   * @param the_method
   * @param the_name
   */
  private void printMethodJavaDoc(final Object the_method, final String the_name)
  {
   
    if (the_method instanceof JConstructorDeclaration)
    {
      JConstructorDeclaration jConstruct = (JConstructorDeclaration) the_method;
      writer.indent(2);
      writer.print("/** This method is a test for Constructor " +
                   jConstruct.ident() + " from the ");
      writer.indent(2);
      writer.print(" * class to  be tested.");
      writer.indent(2);
      writer.print(" */");
      writer.indent(2);
      writer.print("@Test(\"dataProvider = " + the_name + "\")");

    }
    else if (the_method instanceof JMethodDeclaration)
    {
      JMethodDeclaration method = (JMethodDeclaration) the_method;
      writer.indent(2);
      writer.print("/** This method is a test for Constructor " + method.ident() +
                   " from the ");
      writer.indent(2);
      writer.print(" * class to  be tested.");
      writer.indent(2);
      writer.print(" */");
      writer.indent(2);
      writer.print("@Test( \"dataProvider = " + the_name + "\")");
    }
  }
  /**
   * Print the main method for generated test class.
   */
  private void printMainMethod()
  {
    writer.indent(2);
    writer.print("public static void main(String[] args)");
    writer.indent(2);
    writer.print("{");
    writer.indent(4);
    writer.print("//Call the test runner here.");
    writer.indent(2);
    writer.print("}");
    writer.newLine(2);
  }
  /**
   * This method prints the end bracket of the class.
   */
  private void printClassEnd()
  {
    writer.print("}");
  }
  /**
   * Print the Data members for the generated Test class.
   */
  private void printDataMembers()
  {
    
  }
}
