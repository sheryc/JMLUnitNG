package org.jmlspecs.jmlunitng.strategies;
/**
 *  This class provides the strategy to generate the iterator 
 *  to provide data of type Char.
 * @author Rinkesh Nagmoti
 * @version 1.0
 */
public class CharStrategy extends AbstractBasicStrategy
{
  /**
   * This method provides the user input data 
   * and need to be overridden in the test data class. 
   * @return Object[]
   */
  @Override
  public Object[] addData()
  {
   
    return new Character[]{};
  }
/**
 * This method provides the default data.
 * @return Object[]
 */
  @Override
  public Object[] defaultData()
  {
    return new Character[]{null, 'a', };
  }
  /**
   * This method provides the user data for all char strategies.
   * @return Object[]
   */
  @Override
  public Object[] addDataForAll()
  {
   
    return new Character[]{};
  }
}
