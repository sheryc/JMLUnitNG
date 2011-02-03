/*
 * JMLUnitNG 
 * Copyright (C) 2010-11
 */

package org.jmlspecs.jmlunitng.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import org.jmlspecs.jmlunitng.util.InheritanceComparator;
import org.jmlspecs.openjml.API;
import org.jmlspecs.openjml.JmlSpecs.MethodSpecs;
import org.jmlspecs.openjml.JmlTree.JmlClassDecl;
import org.jmlspecs.openjml.JmlTree.JmlCompilationUnit;
import org.jmlspecs.openjml.JmlTree.JmlMethodClauseSignals;
import org.jmlspecs.openjml.JmlTree.JmlMethodClauseSignalsOnly;
import org.jmlspecs.openjml.JmlTree.JmlMethodDecl;
import org.jmlspecs.openjml.JmlTreeScanner;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree.JCExpression;

/**
 * Factory class that generates ClassInfo and MethodInfo objects.
 * 
 * @author Daniel M. Zimmerman
 * @author Jonathan Hogins
 * @version January 2011
 */
public final class InfoFactory {
  /**
   * Cache of already created ClassInfo objects. 
   */
  private static final Map<String, ClassInfo> CLASS_CACHE = 
    new HashMap<String, ClassInfo>();

  /**
   * Cache of already-created top-level ClassInfo objects by compilation unit.
   */
  private static final Map<JmlCompilationUnit, ClassInfo> COMPILATION_UNIT_CACHE = 
    new HashMap<JmlCompilationUnit, ClassInfo>();
  
  /**
   * Cache of already-created methods by method symbol.
   */
  private static final SortedMap<ClassInfo, SortedSet<MethodInfo>> METHOD_CACHE =
    new TreeMap<ClassInfo, SortedSet<MethodInfo>>();
  
  /**
   * Private constructor to prevent initialization.
   */
  private InfoFactory() {
  }

  /**
   * Generates ClassInfo (and dependent) objects for the given
   * compilation units.
   * 
   * @param the_units The compilation units to create ClassInfos from.
   */
  public static void generateInfos(final List<JmlCompilationUnit> the_units, final API api) {    
    final SortedMap<ClassInfo, SortedSet<MethodInfo>> signals_cache = 
      new TreeMap<ClassInfo, SortedSet<MethodInfo>>();
    
    // first, generate ClassInfos and MethodInfos for each tree
    for (JmlCompilationUnit u : the_units) {
      final ClassInfoParser cp = new ClassInfoParser();
      u.accept(cp);
      COMPILATION_UNIT_CACHE.put(u, cp.getEnclosingClassInfo());
      final MethodInfoParser mp = new MethodInfoParser(api, signals_cache);
      u.accept(mp);
    }
    
    // now we should have all the classes and methods, let's match them up
    // the global method cache has those without signals, so let's replace
    // them with those with signals, where applicable
    
    final SortedSet<ClassInfo> all_classes = getAllClassInfos();
    
    for (ClassInfo c : all_classes) {
      final SortedSet<MethodInfo> raw = METHOD_CACHE.get(c);
      final SortedSet<MethodInfo> signals = signals_cache.get(c);
      final SortedSet<MethodInfo> combined = new TreeSet<MethodInfo>();
      
      if (raw != null && signals != null) {
        final Iterator<MethodInfo> it_raw = raw.iterator();
        final Iterator<MethodInfo> it_signals = signals.iterator();
      
        // iterate over the sets to find the methods to include
      
        while (it_signals.hasNext()) {
          final MethodInfo next_signals = it_signals.next();
          boolean found = false;
          while (!found && it_raw.hasNext()) {
            final MethodInfo next_raw = it_raw.next();
            if (next_raw.equalsExceptSignals(next_signals)) {
              found = true;
              combined.add(next_signals);
            } else {
              combined.add(next_raw);
            }
          }
        }
      } else if (raw != null) {
        combined.addAll(raw);
      }
      
      METHOD_CACHE.put(c, combined);
    }
    
    processInheritedMethods();
  }
  
  /**
   * Returns the cached ClassInfo object for the specified
   * qualified class name.
   * 
   * @param the_qualified_name The qualified class name.
   * @return a ClassInfo object representing the class, or null
   * if one has not yet been created.
   */
  public static ClassInfo getClassInfo(final String the_qualified_name) {
    return CLASS_CACHE.get(the_qualified_name);
  }
  
  /**
   * Returns the cached ClassInfo objects for the specified
   * compilation unit.
   * 
   * @param the_unit The compilation unit.
   * @return a list of ClassInfo objects representing the
   * classes in the compilation unit, or null if one
   * has not yet been created.
   */
  public static ClassInfo getClassInfo(final JmlCompilationUnit the_unit) {
    return COMPILATION_UNIT_CACHE.get(the_unit);
  }
  
  /**
   * @return all the ClassInfos that have been generated.
   */
  public static SortedSet<ClassInfo> getAllClassInfos() {
    final SortedSet<ClassInfo> result = new TreeSet<ClassInfo>();
    result.addAll(CLASS_CACHE.values());
    return result;
  }
  
  /**
   * Finds all the child classes of the_class for which tests are being
   * generated.
   * 
   * @param the_class The class to find the children of.
   * @return all the ClassInfos that describe child classes of the_class.
   */
  public static SortedSet<ClassInfo> getAllChildren(final ClassInfo the_class) {
    final SortedSet<ClassInfo> result = new TreeSet<ClassInfo>();
    for (ClassInfo c : CLASS_CACHE.values()) {
      ClassInfo p = c.getParent();
      while (p != null) {
        if (p.equals(the_class)) {
          result.add(c);
          break;
        } else {
          for (ClassInfo i : p.getInterfaces()) {
            if (i == the_class) {
              result.add(c);
            }
          }
          p = p.getParent();
        }
      }
    }
    return result;
  }
  
  /**
   * Finds all the concrete child classes of the_class for which tests are being
   * generated.
   * 
   * @param the_class The class to find the concrete children of.
   * @return all the ClassInfos that describe concrete child classes of the_class.
   */
  public static SortedSet<ClassInfo> getConcreteChildren(final ClassInfo the_class) {
    final SortedSet<ClassInfo> all_children = getAllChildren(the_class);
    final Iterator<ClassInfo> i = all_children.iterator();
    while (i.hasNext()) {
      final ClassInfo c = i.next();
      if (c.isAbstract()) {
        i.remove();
      }
    }
    return all_children;
  }
  
  private static void processInheritedMethods() {
    final SortedSet<ClassInfo> class_set = getAllClassInfos();
    final Queue<ClassInfo> class_queue = new LinkedList<ClassInfo>();
    
    // initialize the method sets for all parentless classes
    
    final Iterator<ClassInfo> it = class_set.iterator();
    while (it.hasNext()) {
      final ClassInfo c = it.next();
      if (c.getParent() == null) {
        it.remove();
        c.initializeMethods(METHOD_CACHE.get(c));
      }
    }
    
    class_queue.addAll(class_set);
    
    // initialize the method sets for other classes
    
    do {
      final ClassInfo c = class_queue.poll();
      if (c.getParent().isInitialized()) {
        final SortedSet<MethodInfo> methods = METHOD_CACHE.get(c);
        // it's safe to add methods from the parent class
        if (c.getParent() != null)
        {
          final Set<MethodInfo> parent_methods = 
            new HashSet<MethodInfo>(c.getParent().getMethods());
          // we do not inherit methods that were already overridden by the parent class
          parent_methods.removeAll(c.getParent().getOverriddenMethods());
          for (MethodInfo pm : parent_methods)
          {
            if (!pm.isConstructor() && !pm.isStatic() &&
                !pm.getProtectionLevel().equals(ProtectionLevel.PRIVATE))
            {
              // we do not inherit constructors or static/private methods
              boolean duplicate = false;
              for (MethodInfo m : methods)
              {
                duplicate = duplicate || 
                            (m.getName().equals(pm.getName()) &&
                             m.getParameters().equals(pm.getParameters()));
              }
              if (!duplicate)
              {
                methods.add(new MethodInfo(pm.getName(), c, pm.getDeclaringClass(),
                                           pm.getProtectionLevel(), pm.getParameters(),
                                           pm.getReturnType(), pm.getSignals(), 
                                           pm.isConstructor(), pm.isStatic(),
                                           pm.isDeprecated()));
              }
            }
          }
        }
        c.initializeMethods(methods);
      } else {
        class_queue.offer(c);
      }
    }
    while (!class_queue.isEmpty());
  }

  /**
   * Creates a ClassInfo object for the given ClassSymbol. Returns a cached
   * version if one exists for the class's qualified name.
   * 
   * @param the_class The Class to generate a ClassInfo object for.
   * @return A ClassInfo object representing the class.
   */
  private synchronized static ClassInfo createClassInfo(final ClassSymbol the_class) {
    if (CLASS_CACHE.containsKey(the_class.getQualifiedName().toString())) {
      return CLASS_CACHE.get(the_class.getQualifiedName().toString());
    }
    final String name = the_class.getQualifiedName().toString();
    final Set<Modifier> flags = the_class.getModifiers();
    final boolean is_abstract = flags.contains(Modifier.ABSTRACT);
    final boolean is_interface = the_class.isInterface();
    final boolean is_static = the_class.isStatic();
    final boolean is_inner = the_class.isInner();
    
    ClassInfo parent = null;
    //check for instanceof. Returns a NoType instance if no superclass exists
    if (the_class.getSuperclass() instanceof ClassType) {
      parent = createClassInfo((ClassSymbol) the_class.getSuperclass().tsym);
    }
    final SortedSet<ClassInfo> interfaces = new TreeSet<ClassInfo>();
    for (Type t : the_class.getInterfaces()) {
      if (t.asElement() instanceof ClassSymbol) {
        // this should always be the case but it doesn't hurt to be safe
        interfaces.add(createClassInfo((ClassSymbol) t.asElement()));
      }
    }
    final boolean is_enumeration =
      parent != null && parent.getFullyQualifiedName().equals("java.lang.Enum");
    final ClassInfo result =
        new ClassInfo(name, getLevel(flags), is_abstract, is_interface, 
                      is_enumeration, is_static, is_inner, parent, interfaces);
    // ensure this ClassInfo object is cached before creating methods
    CLASS_CACHE.put(name, result);

    // add inner classes after ClassInfo creation.
    final Set<ClassInfo> inner_classes = new HashSet<ClassInfo>();
    final Scope members = the_class.members();    
    for (Scope.Entry e = members.elems; e != null; e = e.sibling) {
      if (e.sym != null && (e.sym.getKind().equals(ElementKind.CLASS))) {
        inner_classes.add(createClassInfo((ClassSymbol) e.sym));
      }
    }
    result.initializeNestedClasses(inner_classes);
    
    // add methods after ClassInfo creation.
    
    SortedSet<MethodInfo> methods = METHOD_CACHE.get(result);
    if (methods == null) {
      methods = new TreeSet<MethodInfo>();
      METHOD_CACHE.put(result, methods);
    }
    for (Scope.Entry e = members.elems; e != null; e = e.sibling) {
      if (e.sym != null && (e.sym.getKind().equals(ElementKind.METHOD) || 
          e.sym.getKind().equals(ElementKind.CONSTRUCTOR))) {
        
        methods.add(createMethodInfo((MethodSymbol) e.sym, new ArrayList<ClassInfo>()));
      }
    }
    return result;
  }

  /**
   * Creates a MethodInfo object for the given MethodSymbol enclosed in the
   * given ClassInfo.
   * 
   * @param the_sym The MethodSymbol to create a MethodInfo object for.
   * @param the_signals The ClassInfos for exception types that can be signaled 
   * by this method.
   */
  /*@ ensures (\forall String s; \result.getParameterTypes().contains(s);
    @             (\exists VarSymbol v; the_sym.params.contains(v); 
    @                 s.equals(v.getSimpleName().toString()))) &&
    @         the_parent_class != null ==> \result.getParentClass() == the_parent_class &&
    @         the_parent_class == null ==> \result.getParentClass() == \result.getDeclaringClass() &&
    @         \result.getDeclaringClass().getFullyQualifiedName()
    @             .equals(the_sym.getEnclosingElement().getQualifiedName().toString()) &&
    @         \result.getProtectionLevel() == getLevel(the_sym.getModifiers()) &&
    @         \result.isConstructor() == the_sym.isConstructor() &&
    @         \result.isStatic() == the_sym.isStatic();
   */
  private static MethodInfo createMethodInfo(final MethodSymbol the_sym, 
                                             final List<ClassInfo> the_signals) {
    final List<ParameterInfo> params = new ArrayList<ParameterInfo>(the_sym.getParameters().size());
    for (VarSymbol v : the_sym.params) {
      params.add(createParameterInfo(v));
    }
    final ClassInfo enclosing_class = createClassInfo(the_sym.enclClass());
    final ProtectionLevel level = getLevel(the_sym.getModifiers());
    String name = the_sym.getSimpleName().toString();
    
    // is the method a constructor?
    if ("<init>".equals(name)) {
      name = enclosing_class.getShortName();
    }
    // is the method deprecated? this is crude but functional
    boolean deprecated = false;
    final List<Attribute.Compound> annotations = the_sym.getAnnotationMirrors();
    for (Attribute.Compound a : annotations) {
      deprecated |= "@java.lang.Deprecated".equals(a.toString()); 
    }
    return new MethodInfo(name, enclosing_class, enclosing_class, level, params, 
                          new TypeInfo(the_sym.getReturnType().toString()), the_signals, 
                          the_sym.isConstructor(), the_sym.isStatic(), deprecated);
  }

  /**
   * Returns a ParameterInfo object representing the given VarSymbol.
   * @param the_var_sym The VarSymbol to translate into a ParameterInfo object.
   */
  /*@ ensures \result.getParameterName().equals(the_var_sym.name.toString()) &&
    @         \result.isArray() <==> the_var_sym.type.tag == TypeTags.ARRAY;
   */
  private static ParameterInfo createParameterInfo(final VarSymbol the_var_sym) {
    Type t = the_var_sym.type;

    //remove any generic elements
    while (t.tag == TypeTags.TYPEVAR) {
      t = t.getUpperBound().tsym.asType();
    }
    return new ParameterInfo(t.toString(), the_var_sym.name.toString());
  }

  /**
   * Returns the protection level present in the given set of Modifiers. Returns
   * null if there are no protection level modifiers (PUBLIC, PROTECTED,
   * PRIVATE) in the given set.
   * 
   * @param the_mods The Set<Modifier> from which to extract the protection
   *          level
   */
  /*@ ensures \result.equals(ProtectionLevel.PUBLIC) ==> the_mods.contains(Modifier.PUBLIC) &&
    @         \result.equals(ProtectionLevel.PROTECTED) ==> 
    @           (!the_mods.contains(Modifier.PUBLIC)  && the_mods.contains(Modifier.PROTECTED)) &&  
    @         \result.equals(ProtectionLevel.PRIVATE) ==> (!the_mods.contains(Modifier.PUBLIC) &&
    @            !the_mods.contains(Modifier.PROTECTED) && the_mods.contains(Modifier.PRIVATE)) && 
    @         \result.equals(ProtectionLevel.NO_LEVEL) ==> (!the_mods.contains(Modifier.PUBLIC) &&
    @            !the_mods.contains(Modifier.PROTECTED) && !the_mods.contains(Modifier.PRIVATE));
   */
  private static ProtectionLevel getLevel(final Set<Modifier> the_mods) {

    ProtectionLevel level = ProtectionLevel.NO_LEVEL;
    if (the_mods.contains(Modifier.PUBLIC)) {
      level = ProtectionLevel.PUBLIC;
    } else if (the_mods.contains(Modifier.PROTECTED)) {
      level = ProtectionLevel.PROTECTED;
    } else if (the_mods.contains(Modifier.PRIVATE)) {
      level = ProtectionLevel.PRIVATE;
    }
    return level;
  }

  /**
   * JCTree scanner that records relevant information on the classes and methods
   * scanned.
   */
  private static class ClassInfoParser extends JmlTreeScanner {
    /**
     * The parsed enclosing ClassInfo object.
     */
    private ClassInfo my_class_info;
    
    /**
     * Extracts information about a class.
     * 
     * @param the_tree The class declaration node.
     */
    public void visitJmlClassDecl(final JmlClassDecl the_tree) {
      my_class_info = InfoFactory.createClassInfo(the_tree.sym);
    }
    
    /**
     * Returns the enclosing ClassInfo object parsed by this visitor. Returns null if a
     * tree containing a JmlClassDecl node has not been parsed yet.
     * 
     * @return The enclosing ClassInfo for the tree.
     */
    public ClassInfo getEnclosingClassInfo() {
      return my_class_info;
    }
  }

  /**
   * JCTree scanner that records relevant information on the classes and methods
   * scanned.
   */
  private static class MethodInfoParser extends JmlTreeScanner {
    /**
     * The OpenJML API being used.
     */
    private final API my_api; 
    
    /**
     * A cache of methods associated with classes.
     */
    private final SortedMap<ClassInfo, SortedSet<MethodInfo>> my_cache;
    
    /**
     * Constructs a MethodInfoParser with the specified cache.
     * 
     * @param the_cache The method cache.
     */
    public MethodInfoParser(final API the_api, 
                            final SortedMap<ClassInfo, SortedSet<MethodInfo>> the_cache) {
      my_api = the_api;
      my_cache = the_cache;
    }
    
    /**
     * Extracts information about a method.
     * 
     * @param the_tree The method declaration node.
     */
    public void visitJmlMethodDecl(final JmlMethodDecl the_tree) {
      // find the signals and add them to the existing method declaration
      final ClassInfo encl_class = createClassInfo(the_tree.sym.enclClass());
      final SignalsParser sp = new SignalsParser();
      final MethodSpecs specs = my_api.getSpecs(the_tree.sym);
      specs.cases.accept(sp);  
      final MethodInfo method = createMethodInfo(the_tree.sym, sp.getExceptionTypes());
      SortedSet<MethodInfo> class_methods = my_cache.get(encl_class);
      if (class_methods == null) {
        class_methods = new TreeSet<MethodInfo>();
        my_cache.put(encl_class, class_methods);
      }
      class_methods.add(method); 
      super.visitJmlMethodDecl(the_tree);
    }
  }
  
  /**
   * JCTree scanner that scans specifically for signals/signals_only clause
   * information to generate a list of exception types.
   */
  private static class SignalsParser extends JmlTreeScanner {
    /**
     * The list of exception types.
     */
    private final List<ClassInfo> my_exception_types = new LinkedList<ClassInfo>();
    
    /**
     * The comparator used to order the exception types in inheritance order.
     */
    private final InheritanceComparator my_comparator = new InheritanceComparator();
    
    /**
     * Extracts information about a signals clause for a method.
     * 
     * @param the_tree The signals clause node.
     */
    public void visitJmlMethodClauseSignals(final JmlMethodClauseSignals the_tree) {
      addInOrder(createClassInfo((ClassSymbol) the_tree.vardef.type.tsym));
    }
    
    /**
     * Extracts information about a signals_only clause for a method.
     * 
     * @param the_tree THe signals_only clause node.
     */
    public void visitJmlMethodClauseSigOnly(final JmlMethodClauseSignalsOnly the_tree) {
      // for a signals_only clause, we have to add all the exceptions in the list
      for (JCExpression exception_type : the_tree.list) {
        addInOrder(createClassInfo((ClassSymbol) exception_type.type.tsym));
      }
    }
    
    /**
     * @return the exception types found in the methods signals/signals_only clauses.
     */
    public List<ClassInfo> getExceptionTypes() {
      return my_exception_types;
    }
    
    /**
     * Adds the specified class (which should be an exception type) to the list, 
     * in inheritance order.
     * 
     * @param the_class The class to add to the list.
     */
    private void addInOrder(final ClassInfo the_class) {
      if (my_exception_types.isEmpty()) {
        my_exception_types.add(the_class);
      } else if (!my_exception_types.contains(the_class)) {
        // we have not previously added this exception type
        boolean added = false;
        for (int i = 0; i < my_exception_types.size(); i++) {
          final ClassInfo c = my_exception_types.get(i);
          if (my_comparator.compare(the_class, c) < 0) {
            my_exception_types.add(i, the_class);
            added = true;
            break;
          }
        }
        if (!added)
        {
          my_exception_types.add(the_class);
        }
      }
    }
  }
}
