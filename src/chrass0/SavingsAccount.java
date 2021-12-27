package chrass0;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Sparkonto
 *
 * Last changes: 2021-11-20
 * @author Christoffer Asserstam, chrass-0
 */
public class SavingsAccount extends Account
{
   private final static double WITHDRAW_RATE = 0.02;
   private boolean freeWithdraw;
   private Calendar freeWithdrawDate;

   // Konstruktor
   public SavingsAccount(String pNo)
   {
      super(pNo);
      this.rate = 0.012;
      this.freeWithdraw = true;
   }

   @Override
   public String getAccountType()
   {
      return "Sparkonto";
   }

   // Om fritt uttag finns
   public boolean isFreeWithdraw()
   {
      return freeWithdraw;
   }

   // Set-metod
   public void setFreeWithdraw(boolean freeWithdraw)
   {
      this.freeWithdraw = freeWithdraw;
   }

   // Uttag för sparkonto
   @Override
   public boolean withdraw(int amount) throws OverdrawException
   {
      BigDecimal newBalance;
      Calendar temp = null;

      // Om valt belopp är större än 0 och saldot är större än valt uttagsbelopp inklusive ränta
      if (amount > 0 && this.getBalance().compareTo(new BigDecimal(BankLogic.convertToBigDecimal(amount*(1+SavingsAccount.WITHDRAW_RATE)))) >= 0)
      {
         // Om fritt uttag finns, sparar datumet
         if (this.freeWithdraw)
         {
            freeWithdrawDate = Calendar.getInstance();
         }
         temp = freeWithdrawDate;

         // Ett år efter senaste fria uttaget
         temp.add(Calendar.YEAR,1);

         // Om fritt uttag ej finns och det har gått ett år sedan förra fria uttaget
         if (!this.freeWithdraw && temp.before(Calendar.getInstance()))
         {
            setFreeWithdraw(true);
         }

         // Nytt saldo är föregående saldo subtraherat med valt belopp
         newBalance = this.getBalance().subtract(new BigDecimal(BankLogic.convertToBigDecimal(amount)));

         // Om fritt uttag ej finns så är nytt saldo föregående saldo subtraherat med valt belopp plus ränta
         if (!this.isFreeWithdraw())
         {
            newBalance = this.getBalance().subtract(new BigDecimal(BankLogic.convertToBigDecimal(amount*(1+SavingsAccount.WITHDRAW_RATE))));
         }

         this.setBalance(newBalance);
         this.setFreeWithdraw(false);
         BankLogic.addTransaction(BankLogic.sdf.format(new Date()) + " " + BankLogic.convertToCurrency(-amount) + // Lägger in transaktion i transaktionslista
         " " + BankLogic.convertToCurrency(this.getBalance()), this.getAccountId());
         return true;
      }
      else
      {
         throw new OverdrawException("Ej tillräckligt saldo på sparkontot, var vänlig försök igen");
      }
   }
}
