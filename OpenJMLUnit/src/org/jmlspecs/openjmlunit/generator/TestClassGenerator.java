/*
 * OpenJMLUnit
 * 
 * @author "Daniel M. Zimmerman (dmz@acm.org)"
 * 
 * @author Jonathan Hogins
 * 
 * @module "OpenJML"
 * 
 * @creation_date "April 2010"
 * 
 * @last_updated_date "April 2010"
 * 
 * @keywords "unit testing", "JML"
 */

package org.jmlspecs.openjmlunit.generator;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * Generator for classes that contain unit tests.
 * 
 * @author Jonathan Hogins
 * @version "April 2010"
 */
public class TestClassGenerator {
  /**
   * The default max protection level.
   */
  public static final ProtectionLevel DEF_PROTECTION_LEVEL = ProtectionLevel.PUBLIC;
  /**
   * Are inherited methods tested by default?
   */
  public static final boolean DEF_TEST_INHERITED_METHODS = false;
  /**
   * Are deprecated methods tested by default?
   */
  public static final boolean DEF_TEST_DEPRECATED_METHODS = false;
  /**
   * Do test data classes use reflection by default?
   */
  public static final boolean DEF_USE_REFLECTION = true;
  /**
   * The max protection level for which to generate tests.
   */
  private ProtectionLevel my_level;
  /**
   * Do I test inherited methods?
   */
  private boolean my_test_inherited_methods;
  /**
   * Do I test deprecated methods?
   */
  private boolean my_test_deprecated_methods;
  /**
   * Do I use reflection?
   */
  protected boolean my_use_reflection;

  /**
   * Create a new TestClassGenerator with the default options.
   */
  public TestClassGenerator() {
    this(DEF_PROTECTION_LEVEL, DEF_TEST_INHERITED_METHODS, DEF_TEST_DEPRECATED_METHODS);
  }

  /**
   * Create a new TestClassGenerator with the given options.
   * 
   * @param the_protection_level The maximum protection level for which to
   *          generate method tests.
   * @param the_test_inherited_methods Do I test inherited methods?
   * @param the_test_deprecated_methods Do I test deprecated methods?
   */
  public TestClassGenerator(final ProtectionLevel the_protection_level,
                            final boolean the_test_inherited_methods,
                            final boolean the_test_deprecated_methods) {
    my_level = the_protection_level;
    my_test_inherited_methods = the_test_inherited_methods;
    my_test_deprecated_methods = the_test_deprecated_methods;
  }

  /**
   * Generate tests for methods of protection level at most the_level. Must not
   * be NO_LEVEL.
   * 
   * @param the_protection_level The max protection level of methods for which
   *          to generate tests.
   */
  // @ requires !the_protection_level.equals(ProtectionLevel.NO_LEVEL);
  public void setMaxProtectionLevel(final ProtectionLevel the_protection_level) {
    my_level = the_protection_level;
  }

  /**
   * Returns the maximum protection level of methods for which tests will be
   * generated.
   * 
   * @return The maximum method protection level for which tests will be
   *         generated.
   */
  public ProtectionLevel getMaxProtectionLevel() {
    return my_level;
  }

  // "Generate tests for inherited methods!",
  /**
   * Sets whether or not tests will be generated for inherited methods.
   * 
   * @param the_test_inherited_methods If true, inherited methods will be
   *          tested.
   */
  public void setTestInheritedMethods(final boolean the_test_inherited_methods) {
    my_test_inherited_methods = the_test_inherited_methods;
  }

  /**
   * Returns whether or not tests will be generated for inherited methods.
   * 
   * @return True if tests will be generated for inherited methods.
   */
  public boolean getTestInheritedMethods() {
    return my_test_inherited_methods;
  }

  // "Generate tests for deprecated methods!",
  /**
   * Sets whether or not tests will be generated for deprecated methods.
   * 
   * @param the_test_deprecated_methods If true, deprecated methods will be
   *          tested.
   */
  public void setTestDeprecatedMethods(final boolean the_test_deprecated_methods) {
    my_test_deprecated_methods = the_test_deprecated_methods;
  }

  /**
   * Returns whether or not tests will be generated for deprecated methods.
   * 
   * @return True if tests will be generated for deprecated methods.
   */
  public boolean getTestDeprecatedMethods() {
    return my_test_deprecated_methods;
  }

  /**
   * Sets whether or not this generator will use reflection in the generated
   * test classes.
   * 
   * @param the_use_reflection If true, reflection will be used to generate test
   *          classes.
   */
  public void setUseReflection(final boolean the_use_reflection) {
    my_use_reflection = the_use_reflection;
  }

  /**
   * Returns true if the generated classes will use reflection. False if not.
   * 
   * @return True if the generated classes will use reflection. False if not.
   */
  public boolean getUseReflection() {
    return my_use_reflection;
  }

  /**
   * Generates a test data class for the_class and writes it to the_writer.
   * 
   * @param the_class The ClassInfo for which to generate a test class.
   * @param the_writer The writer for which to output class data.
   * @throws IOException Thrown if an IOException occurs while writing to
   *           the_writer.
   */
  public void generateTestDataClass(final ClassInfo the_class, final Writer the_writer) throws IOException {
    generateClasses(the_class, null, the_writer);
  }
  // "Generate test data of classes the_gen_classes for parameters of class the_param_class!",

  /**
   * Generates a test class for the_class and writes it to the_writer. The generated class
   * extends the class generated by generateTestDataClass().
   * 
   * @param the_class The ClassInfo for which to generate a test class.
   * @param the_writer The writer for which to output class data.
   * @throws IOException Thrown if an IOException occurs while writing to
   *           the_writer.
   */
  public void generateTestClass(final ClassInfo the_class, final Writer the_writer)
    throws IOException {
    generateClasses(the_class, the_writer, null);
  }
  
  /**
   * Returns the methods from the given class to test based on generator settings.
   * @param the_class The class for which to find testable methods.
   * @return A list of methods in the_class to test.
   */
  /*@ ensures (\forall MethodInfo m; \result.contains(m); 
    @   m.isTestable() && 
    @   ((m.isInherited() && my_test_inherited_methods) || !m.isInherited()));
   */
  private List<MethodInfo> getMethodsToTest(final ClassInfo the_class) {
    final List<MethodInfo> methods = new LinkedList<MethodInfo>();
    for (MethodInfo m : the_class.getTestableMethods()) {
      if (m.isInherited()) {
        if (my_test_inherited_methods) {
          methods.add(m);
        }
      } else {
        methods.add(m);
      }
    }
    return methods;
  }
  
  /**
   * Returns the basic types present in the parameters of the given methods.
   * @param the_methods The methods for which to find parameter basic types.
   * @return A list of basic types.
   */
  /*@ ensures (\forall Sring c; \result.contains(c); 
    @   (\exists MethodInfo m; the_methods.contains(m); 
    @     (\exists String s; m.getParameterTypes().contains(s); s.equals(c))
    @   )
    @ );
   */
  private List<Type> getUniqueParameterTypes(final List<MethodInfo> the_methods) {
    final Set<Type> classes = new HashSet<Type>();
    for (MethodInfo m : the_methods) {
      for (Type s : m.getParameterTypes()) {
        classes.add(s);
      }
    }
    return new ArrayList<Type>(classes);
  }
  /**
   * Generates both test and test data classes and writes them to the given writers.
   * If either writer is null, that class is not generated.
   * 
   * @param the_class The class for which to generate test classes.
   * @param the_test_writer The writer for which to output the test class.
   * @param the_data_writer The writer for which to output the test data class.
   * @throws IOException Thrown if an IOException occurs while writing to
   *           the_test_writer or the_data_writer.
   */
  public void generateClasses(final ClassInfo the_class, final Writer the_test_writer, 
                               final Writer the_data_writer) throws IOException {
    StringTemplateUtil.initialize();
    final List<MethodInfo> methods = getMethodsToTest(the_class);
    final List<Type> types = getUniqueParameterTypes(methods);
    //generate test class
    if (the_test_writer != null) {
      final StringTemplateGroup group = 
        StringTemplateGroup.loadGroup("test_class_java");
      final StringTemplate t = group.getInstanceOf("main");
      t.setAttribute("class", the_class);
      t.setAttribute("date", 
                     DateFormat.getDateInstance().format(Calendar.getInstance().getTime()));
      t.setAttribute("methods", methods);
      the_test_writer.write(t.toString());
    }
    //generate data class
    if (the_data_writer != null) {
      final StringTemplateGroup group = StringTemplateGroup.loadGroup("test_data_class_java");
      final StringTemplate t = group.getInstanceOf("main");
      t.setAttribute("class", the_class);
      t.setAttribute("date", 
                     DateFormat.getDateInstance().format(Calendar.getInstance().getTime()));
      t.setAttribute("methods", methods);
      t.setAttribute("types", types);
      the_data_writer.write(t.toString());
    }
  }
}
