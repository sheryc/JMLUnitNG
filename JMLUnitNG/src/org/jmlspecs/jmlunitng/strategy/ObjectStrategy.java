/*
 * JMLUnitNG 
 * Copyright (C) 2010
 */

package org.jmlspecs.jmlunitng.strategy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jmlspecs.jmlunitng.iterator.InstantiationIterator;
import org.jmlspecs.jmlunitng.iterator.MultiIterator;
import org.jmlspecs.jmlunitng.iterator.NonNullMultiIterator;
import org.jmlspecs.jmlunitng.iterator.ObjectArrayIterator;
import org.jmlspecs.jmlunitng.iterator.RepeatedAccessIterator;

/**
 * The strategy for all non-primitive, non-array types. For Enums, 
 * it always provides all values of the enum unless the default 
 * values are overridden. For other types of object, it attempts
 * to find test data generators for the default values.
 * 
 * @author Jonathan Hogins
 * @author Daniel M. Zimmerman
 * @version January 2011
 */
public abstract class ObjectStrategy implements Strategy {  
  /**
   * The class for which this strategy was made.
   */
  private final Class<?> my_class;
  
  /**
   * Should we use reflective data generation?
   */
  private boolean my_reflective;
  // TODO consider making the following 3 collections into Sets
  /**
   * The test data generators found for this object strategy to use.
   */
  private final List<Class<? extends ObjectStrategy>> my_generators;

  /**
   * The classes that are generated by the test data generators.
   */
  private final List<Class<?>> my_generator_classes;
  
  /**
   * The classes for which we should use the default constructors
   * since no test data classes were found.
   */
  private final List<Class<?>> my_non_generator_classes;
  
  /**
   * The enum constants for the given class, if it is an enum type.
   */
  private final Object[] my_enum_constants; 

  /**
   * Creates a new ObjectStrategy for the given class. Default values will be
   * generated reflectively by the test data class for the_class if present;
   * for enumerations, all enum constants will be used. This behavior can
   * be subsequently changed with control methods.
   * 
   * @param the_class The class for which to generate test data.
   */
  public ObjectStrategy(final Class<?> the_class) {
    my_class = the_class;
    my_reflective = true;
    my_generators = new LinkedList<Class<? extends ObjectStrategy>>();
    my_generator_classes = new LinkedList<Class<?>>();
    my_non_generator_classes = new LinkedList<Class<?>>();
    my_enum_constants = the_class.getEnumConstants();
    
    if (my_enum_constants == null) {
      // it is not an enum
      addDataClass(the_class);
    } 
  }
  
  /**
   * A default empty iterator, may be overridden by child classes.
   * @return An empty iterator.
   */
  public RepeatedAccessIterator<?> getLocalValues() {
    return new ObjectArrayIterator<Object>
    ((Object[]) Array.newInstance(my_class, 0));
  }
  
  /**
   * A default empty iterator, may be overridden by child classes.
   * @return An empty iterator.
   */
  public RepeatedAccessIterator<?> getClassValues() {
    return new ObjectArrayIterator<Object>
    ((Object[]) Array.newInstance(my_class, 0));
  }

  /**
   * A default empty iterator, may be overridden by child classes.
   * @return An empty iterator.
   */
  public RepeatedAccessIterator<?> getPackageValues() {
    return new ObjectArrayIterator<Object>
    ((Object[]) Array.newInstance(my_class, 0));
  }
  
  /**
   * Returns an iterator over the values defined in the class' test data
   * definition if it exists. Otherwise, returns an iterator over
   * DEFAULT_VALUES.
   * 
   * @return An Iterator over default values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public RepeatedAccessIterator<?> getDefaultValues() {
    final List<RepeatedAccessIterator<?>> result_list = 
      new LinkedList<RepeatedAccessIterator<?>>();
    result_list.add(new ObjectArrayIterator<Object>(new Object[] { null }));
    if (my_reflective && my_enum_constants == null && !my_generators.isEmpty()) {
      // try to return data generated using reflection
      final List<RepeatedAccessIterator<?>> iterators = 
        new LinkedList<RepeatedAccessIterator<?>>();
      for (Class<? extends ObjectStrategy> c : my_generators) {
        try {
          iterators.add(c.newInstance().iterator());
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      for (Class<?> c : my_non_generator_classes) {
        // add the default constructor for each non-generator class
        iterators.add
          (new InstantiationIterator(c, new Class<?>[0], 
                                     new ObjectArrayIterator<Object[]>(new Object[][]{{}})));
      }
      result_list.add(new NonNullMultiIterator(iterators));
    } else if (!my_reflective && my_enum_constants == null) {
      // no reflection, but we can still use default constructors
      final List<RepeatedAccessIterator<?>> iterators = 
        new LinkedList<RepeatedAccessIterator<?>>();
      for (Class<?> c : my_generator_classes) {
        // add the default constructor for each generator class
        iterators.add
          (new InstantiationIterator(c, new Class<?>[0], 
                                     new ObjectArrayIterator<Object[]>(new Object[][]{{}})));        
      }
      for (Class<?> c : my_non_generator_classes) {
        // add the default constructor for each non-generator class
        iterators.add
          (new InstantiationIterator(c, new Class<?>[0], 
                                     new ObjectArrayIterator<Object[]>(new Object[][]{{}})));        
      }
      result_list.add(new NonNullMultiIterator(iterators));
    } else if (my_enum_constants != null) { 
      // return the enum constants
      result_list.add(new ObjectArrayIterator<Object>(my_enum_constants));
    }
    
    return new MultiIterator(result_list);
  }
  
  /**
   * Returns a RepeatedAccessIterator over all values in the order: local-scope
   * values, class-scope values, package-scope values, default values.
   * 
   * @return What are all your values?
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public RepeatedAccessIterator<?> iterator() {
    final List<RepeatedAccessIterator<?>> iterators = new ArrayList<RepeatedAccessIterator<?>>(3);
    iterators.add(getLocalValues());
    iterators.add(getClassValues());
    iterators.add(getPackageValues());
    iterators.add(getDefaultValues());
    return new MultiIterator(iterators);
  }
  
  /**
   * Adds a data class to be used by this strategy when reflection
   * is turned on.
   * 
   * @param the_class The new data class.
   * @exception ClassCastException if the specified class cannot be
   * cast to the class for which this strategy was constructed.
   */
  @SuppressWarnings("unchecked")
  public final void addDataClass(final Class<?> the_class) {
    if (!my_class.isAssignableFrom(the_class)) {
      throw new ClassCastException
      ("Cannot add " + the_class + " as a data class to the strategy " +
       "for " + my_class);
    }
    if (the_class.getEnumConstants() == null &&
        !my_generator_classes.contains(the_class) &&
        !my_non_generator_classes.contains(the_class)) { 
      // it's not an enum, or already added, so we can add it
      Class<?> generator_class = findStrategyClass(the_class); 
      if (generator_class != null &&
          ObjectStrategy.class.isAssignableFrom(generator_class)) {
        my_generators.add((Class<? extends ObjectStrategy>) generator_class);
        my_generator_classes.add(the_class);
      } else {
        my_non_generator_classes.add(the_class);
      }
    }
  }
  
  /**
   * Clears the list of data classes to be used by this strategy
   * when reflection is turned on.
   */
  public final void clearDataClasses() {
    my_generators.clear();
    my_generator_classes.clear();
    my_non_generator_classes.clear();
  }
  
  /**
   * Controls the use of reflection by this strategy.
   * 
   * @param the_reflective true to enable the use of reflection to
   * generate objects, false otherwise.
   */
  public final void setReflective(final boolean the_reflective) {
    my_reflective = the_reflective;
  }
  
  /**
   * Finds the appropriate strategy class to use for the specified class.
   * 
   * @param the_class The class to find a strategy class for.
   * @return the strategy class, or null if no strategy class can be loaded.
   */
  private final Class<?> findStrategyClass(final Class<?> the_class) {
    Class<?> result = null;
    final String class_name = the_class.getName();
    
    if (the_class.getPackage() == null) {
      result = loadClass(class_name + "_InstanceStrategy");
    } else {
      result = loadClass(class_name + "_JML_Data.InstanceStrategy");
    }
    
    if (result == null) {
      final String formatted_name = formatClassName(class_name);
      // no instance strategy, try to find a class strategy in our "package"
      if (my_class.getPackage() == null) {
        // the first bit of our name, before the first underscore, is our prefix
        final String prefix = 
          my_class.getName().substring(0, my_class.getName().indexOf('_'));
        // a class strategy name looks like "prefix_ClassStrategy_formattedclassname"
        result = 
          loadClass(prefix + "_ClassStrategy_" + formatted_name);
      } else {
        // a class strategy name, in our package, looks like 
        // "ClassStrategy_formattedclassname"
        final String pkg_name = 
          my_class.getName().substring(0, my_class.getName().lastIndexOf('.'));
        result = loadClass(pkg_name + ".ClassStrategy_" + formatted_name);
      }
    }

    if (result == null) {
      final String formatted_name = formatClassName(class_name);
      // no instance or class strategy, try to find a package strategy
      if (my_class.getPackage() == null) {
        // a package strategy name looks like "PackageStrategy_formattedclassname"
        result = loadClass("PackageStrategy_" + formatted_name);
      } else {
        // we need to look in our parent package
        final String pkg_name = 
          my_class.getName().substring(0, my_class.getName().lastIndexOf('.'));
        final String parent_pkg_name = 
          pkg_name.substring(0, pkg_name.lastIndexOf('.'));
        result = loadClass(parent_pkg_name + ".PackageStrategy_" + formatted_name);
      }
    }
    
    // and that's it; if we didn't find anything, that's too bad, we return null
    return result;
  }
  
  /**
   * Attempts to load the specified class.
   * 
   * @param the_name The name of the class.
   * @return the class, or null if it does not exist.
   */
  private final Class<?> loadClass(final String the_name) {
    try {
      return Class.forName(the_name);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
  
  /**
   * Formats the name of a class, for use in locating strategies.
   * 
   * @param the_name The name to format.
   * @return The formatted name.
   */
  private final String formatClassName(final String the_name) {
    final StringBuilder formatted = new StringBuilder(the_name.replace('.', '_'));
    if (the_name.contains("[]")) {
      final int array_dimension = 
        the_name.substring(the_name.indexOf("[]"), the_name.length()).length() / 2;
      formatted.delete(formatted.indexOf("[]"), formatted.length());
      formatted.append(array_dimension + "DArray");
    } 
    return formatted.toString();
  }
}
