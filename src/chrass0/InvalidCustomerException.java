package chrass0;

/**
 * Exception för felaktigt namn, efternamn och personnummer för kund
 *
 * Last changes: 2021-11-28
 * @author Christoffer Asserstam, chrass-0
 */

public class InvalidCustomerException extends Exception
{
   public InvalidCustomerException(String message)
   {
      super(message);
   }
}
