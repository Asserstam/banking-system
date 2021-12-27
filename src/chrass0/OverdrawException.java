package chrass0;

/**
 * Exception för övertrassering av konto
 *
 * Last changes: 2021-11-28
 * @author Christoffer Asserstam, chrass-0
 */

public class OverdrawException extends Exception
{
   public OverdrawException(String message)
   {
      super(message);
   }

}
