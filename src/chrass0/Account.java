package chrass0;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Konto innehållandes personnummer, saldo, kontonummer, räntesats och kontotyp.
 *
 * Last changes: 2021-12-02
 * @author Christoffer Asserstam, chrass-0
 */

public abstract class Account implements Serializable
{
    // Instansvariabler
    private String pNo;
    private BigDecimal balance = new BigDecimal("0");
    private int accountId;
    protected double rate;

    // Klassvariabel
    private static int lastAssignedNumber = 1000;

    // Konstruktor som skapar och initierar
    // konto med personnummer samt kontonummer
    public Account(String pNo)
    {
        lastAssignedNumber++;
        this.pNo = pNo;
        accountId = lastAssignedNumber;
    }

    // Presentationsmetod
    @Override
    public String toString()
    {
        return getAccountId() + " " + getBalanceKr() + " " + getAccountType() + " " + rate * 100 + " %";
    }

    // Get-metoder
    public int getAccountId()
    {
        return accountId;
    }

    public BigDecimal getBalance()
    {
        return this.balance;
    }

    public String getpNo()
    {
        return this.pNo;
    }

    public String getBalanceKr()
    {
        return BankLogic.convertToCurrency(balance); // valuta-formaterat saldo
    }

    // Set-metod
    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    /**
     * Sätter in pengar på konto och lägger in transaktion i lista
     * @param amount - belopp att sätta in
     * @return true om transaktion genomfördes, annars false
     */
    public boolean deposit(int amount)
    {
        // Om valt belopp är större än 0
        if (amount > 0)
        {
            this.balance = this.balance.add(new BigDecimal(BankLogic.convertToBigDecimal(amount)));
            BankLogic.addTransaction(BankLogic.sdf.format(new Date()) + " " + BankLogic.convertToCurrency(amount) + // Lägger in transaktion i transaktionslista
            " " + BankLogic.convertToCurrency(this.getBalance()), this.getAccountId());
            return true;
        }
        return false;
    }

    /**
     * Getter för senast använda kontonumret
     * @return kontonummer
     */
    public static int getLastAssignedNumber()
    {
        return lastAssignedNumber;
    }

    public static void setLastAssignedNumber(int number)
    {
        lastAssignedNumber = number;
    }

    // Abstrakta metoder
    /**
     * @return kontotyp
     */
    public abstract String getAccountType();

    /**
     * Tar ut pengar från konto
     * @param amount - uttagsbelopp
     * @return true om uttag gjordes, annars false
     */
    public abstract boolean withdraw(int amount) throws OverdrawException;

}
