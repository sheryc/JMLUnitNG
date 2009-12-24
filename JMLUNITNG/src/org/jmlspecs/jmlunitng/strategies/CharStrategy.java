
package org.jmlspecs.jmlunitng.strategies;

/**
 * This is the strategy to handle basic data of type char.
 * 
 * @author Rinkesh Nagmoti
 * @version 1.0
 */
public class CharStrategy extends AbstractBasicStrategy
{
  /**
   * This method provides the user input data and need to be overridden in the
   * test data class.
   * 
   * @return Object[]
   */
  @Override
  public StrategyIterator addData()
  {

    return new ParameterIterator(new Character[]{});
  }

  /**
   * This method provides the default data.
   * 
   * @return Object[]
   */
  @Override
  public StrategyIterator defaultData()
  {
    return new ParameterIterator(new Character[]{0, 1});
  }

  /**
   * This method provides the user data for all Integer strategies.
   * 
   * @return Object[]
   */
  @Override
  public StrategyIterator addDataForAll()
  {

    return new ParameterIterator(new Character[]{});
  }

}
