package chrass0;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Kreditkonto
 *
 * Last changes: 2021-11-20
 * @author Christoffer Asserstam, chrass-0
 */

public class CreditAccount extends Account
{
   private static final int CREDIT_LIMIT = 5000;
   static final double LOANED_RATE = 0.07;

   // Konstruktor
   public CreditAccount(String pNo)
   {
      super(pNo);
      this.rate = 0.005;
   }

   @Override
   public String getAccountType()
   {
      return "Kreditkonto";
   }

   // Uttag för kreditkonto
   @Override
   public boolean withdraw(int amount) throws OverdrawException
   {
      if (amount > 0 && this.getBalance().compareTo(new BigDecimal(BankLogic.convertToBigDecimal(amount-CreditAccount.CREDIT_LIMIT))) >= 0)
      {
         this.setBalance(this.getBalance().subtract(new BigDecimal(BankLogic.convertToBigDecimal(amount))));
         BankLogic.addTransaction(BankLogic.sdf.format(new Date()) + " " + BankLogic.convertToCurrency(-amount) + // Lägger in transaktion i transaktionslista
         " " + BankLogic.convertToCurrency(this.getBalance()), this.getAccountId());
         return true;
      }
      else
      {
         throw new OverdrawException("Kreditgränsen är " + CREDIT_LIMIT + " kr, var vänlig försök igen");
      }

   }

   // Presentationsmetod
   @Override
   public String toString()
   {
      // Använder basklassens presentationsmetod om saldot är positivt eller 0
      if (this.getBalance().compareTo(BigDecimal.ZERO) >= 0)
      {
         return super.toString();
      }
      else
      {
         return getAccountId() + " " + this.getBalanceKr() + " " + getAccountType() + " " + Math.round((CreditAccount.LOANED_RATE * 100)*10.0)/10.0 + " %";
      }
   }

}
