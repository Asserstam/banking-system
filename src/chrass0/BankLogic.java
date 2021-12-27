package chrass0;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Logiska operationer för att hantera kunder och konton.
 *
 * Last changes: 2021-12-06
 * @author Christoffer Asserstam, chrass-0
 */

public class BankLogic
{
    private static ArrayList<Customer> customerList = new ArrayList<>();
    private static ArrayList<Account> accountList = new ArrayList<>();
    private static LinkedHashMap<String, Integer> transactionList = new LinkedHashMap<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Skapar lista och lägger till alla existerande kunder
     * @return list - presentation av bankens kunder
     */
    public ArrayList<String> getAllCustomers()
    {
        ArrayList<String> list = new ArrayList<>();

        // Itererar kundlista och lägger till
        // kunders personnummer, namn och efternamn i list
        for (Customer customer : customerList)
        {
            list.add(customer.toString());
        }
        return (ArrayList<String>) list.clone();
    }

    /**
     * Skapar kund och lägger till i customerList
     * @param name - kunds namn
     * @param surname - kunds efternamn
     * @param pNo - kunds personnummer
     * @return true om kund skapas, annars false
     */
    public boolean createCustomer(String name, String surname, String pNo) throws InvalidCustomerException
    {
        String newPNo = formatPNo(pNo);
        if (!(isNameAllowed(name, surname) && isPNoAllowed(newPNo)))
        {
            return false;
        }

        Customer customer = new Customer(name, surname, newPNo); // Skapar kundobjekt
        customerList.add(customer); // Lägger in kund-objekt i kundlistan
        return true;
    }

    /**
     * Om kund med specifikt personnummer finns skapas lista med dennes information
     * @param pNo - kunds personnummer
     * @return tempList - kunds personnummer, namn och information av kunds konton
     */
    public ArrayList<String> getCustomer(String pNo)
    {
        ArrayList<String> tempList = new ArrayList<>();

        if (!customerExists(pNo))
        {
            return null;
        }

        for (Customer customer : customerList)
        {
            if (customer.getpNo().equals(pNo)) // Om parameter och kunds personnummer är samma
            {
                // Lägger till personnummer, förnamn och efternamn i temporär lista
                tempList.add(customer.toString());

                for (Account account : accountList)
                {
                    if (account.getpNo().equals(pNo)) // Om kontos personnummer är samma som kontos kopplade personnummer
                    {
                        // Lägger till kontonummer, saldo, kontotyp och ränta i temporär lista
                        tempList.add(account.toString());
                    }
                }
            } // End if customer.getpNo().equals(pNo)
        } // End for-each customer
        return (ArrayList<String>) tempList.clone();
    }

    /**
     * Ändrar kunds förnamn och/eller efternamn
     * @param name - kunds nya förnamn
     * @param surname - kunds nya efternamn
     * @param pNo - personnumret för kund som byter namn
     * @return true om namnbyte gjordes, annars false
     */
    public boolean changeCustomerName(String name, String surname, String pNo) throws InvalidCustomerException
    {
        // Om kund ej finns eller förnamn och efternamn är tomma
        if (!customerExists(pNo) || !isNameAllowed(name, surname))
        {
            return false;
        }

        for (Customer customer : customerList)
        {
            if (customer.getpNo().equals(pNo))
            {
                customer.setName(name);
                customer.setSurname(surname);
            }
        }
        return true;
    }

    /**
     * Skapar sparkonto till kund
     * @param pNo - personnummer för kund som kontot skapas till
     * @return kontonumret om kontot skapades, annars -1
     */
    public int createSavingsAccount(String pNo)
    {
        if (customerExists(pNo))
        {
            SavingsAccount account = new SavingsAccount(pNo); // Skapar konto-objekt
            accountList.add(account); // Lägger till konto i kontolistan
            return account.getAccountId();
        }
        return -1;
    }

    /**
     * Skapar kreditkonto till kund
     * @param pNo - personnummer för kund som kontot skapas till
     * @return kontonumret om kontot skapades, annars -1
     */
    public int createCreditAccount(String pNo)
    {
        if (customerExists(pNo))
        {
            CreditAccount account = new CreditAccount(pNo); // Skapar kreditkonto-objekt
            accountList.add(account); // Lägger till konto i kontolistan
            return account.getAccountId();
        }
        return -1;
    }

    /**
     * Visar ett kontos information
     * @param pNo - personnummer för kund som äger kontot
     * @param accountId - kontonummer för kontot
     * @return a.toString() innehållandes kontoinformation om konto finns, annars null
     */
    public String getAccount(String pNo, int accountId)
    {
        for (Account account : accountList)
        {
            // Om konto stämmer överens med valt personnummer och kontonummer
            if (accountMatches(account,pNo,accountId))
            {
                return account.toString(); // toString-metoden i klassen Account har överskuggats
            }
        }
        return null;
    }

    /**
     * Sätter in pengar på konto
     * @param pNo - personnummer för kund som äger kontot
     * @param accountId - kontonummer för kontot
     * @param amount - insättningsbelopp
     * @return true om insättningen gjordes, annars false
     */
    public boolean deposit(String pNo, int accountId, int amount)
    {
        boolean ifDeposited = false;

        if (customerExists(pNo)) // Om kund finns
        {
            for (Account a : accountList)
            {
                if (accountMatches(a,pNo,accountId)) // Om konto stämmer överens med valt personnummer och kontonummer
                {
                    ifDeposited = a.deposit(amount); // True om insättning genomfördes, annars false
                }
            }
        } // End if (customerExists(pNo))
        return ifDeposited;
    }

    /**
     * Tar ut pengar från konto
     * @param pNo - personnummer för kund som äger kontot
     * @param accountId - kontonummer för kontot
     * @param amount - uttagsbelopp
     * @return true om uttag gjordes, annars false
     */
    public boolean withdraw(String pNo, int accountId, int amount) throws OverdrawException
    {
        boolean ifWithdrawn = false;

        if (customerExists(pNo)) // Om kund finns
        {
            for (Account a : accountList)
            {
                // Om konto stämmer överens med valt personummer och kontonummer
                if (accountMatches(a,pNo,accountId))
                {
                    ifWithdrawn = a.withdraw(amount);
                }
            }
        } // End if (customerExists(pNo) && amount > 0)
        return ifWithdrawn;
    }

    /**
     * Avslutar konto
     * @param pNo - personnummer för kund som äger kontot
     * @param accountId - kontonummer för kontot som ska avslutas
     * @return borttaget kontos information om konto finns, annars null
     */
    public String closeAccount(String pNo, int accountId)
    {
        Iterator<Account> iterator = accountList.iterator(); // Skapar iterator för att kunna ta bort element under iteration
        String temp; // Temporär sträng för information om avslutat konto

        for (Customer c : customerList)
        {
            if (c.getpNo().equals(pNo))
            {
                while (iterator.hasNext())
                {
                    Account a = iterator.next();

                    // Kontos personnummer och kontonummer stämmer överens med valt
                    if (accountMatches(a,pNo,accountId))
                    {
                        if (a.getBalance().compareTo(BigDecimal.ZERO) >= 0)
                        {
                            temp = a.getAccountId() + " " + a.getBalanceKr() + " " + a.getAccountType() + " " +
                            convertToCurrency((a.getBalance().multiply(new BigDecimal(convertToBigDecimal(a.rate)))));

                        }
                        else
                        {
                            temp = a.getAccountId() + " " + a.getBalanceKr() + " " + a.getAccountType() + " " +
                            convertToCurrency((a.getBalance().multiply(new BigDecimal(convertToBigDecimal(CreditAccount.LOANED_RATE)))).abs());
                        }
                        iterator.remove(); // Tar bort konto-objekt
                        return temp;
                    }
                } // End while (iterator.hasNext())
            } // End if (c.getpNo().equals(pNo))
        } // End foreach customer
        return null;
    }

    /**
     * Tar bort kund och dennes konton från banken
     * @param pNo - personnummer för kund som ska tas bort
     * @return tempList innehållandes kunds personnummer, namn och kontoinformation om kund fanns. Annars null
     */
    public ArrayList<String> deleteCustomer(String pNo)
    {
        ArrayList<String> tempList = new ArrayList<>();

        // Skapar iteratorer för att ta bort element under iteration
        Iterator<Customer> iterCustomer = customerList.iterator();
        Iterator<Account> iterAccount = accountList.iterator();

        while (iterCustomer.hasNext())
        {
            Customer c = iterCustomer.next();
            // Om kundens personnummer är samma som valt
            if (c.getpNo().equals(pNo))
            {
                // Lägger in kunds personnummer, förnamn och efternamn i lista
                tempList.add(c.getpNo() + " " + c.getName() + " " + c.getSurname());
                customerList.remove(c); // Tar bort kund från kundlista
                while (iterAccount.hasNext())
                {
                    Account a = iterAccount.next();
                    // Om kontos personnummer stämmer överens med valt
                    if (a.getpNo().equals(pNo))
                    {
                        tempList.add(a.getAccountId() + " " + a.getBalanceKr() + " " + a.getAccountType() + " " +
                                convertToCurrency(a.getBalance().multiply(new BigDecimal(convertToBigDecimal(a.rate)))));
                        iterAccount.remove(); // Tar bort konto
                    }
                }
                return (ArrayList<String>) tempList.clone();
            } // End if (c.getpNo().equals(pNo))
        } // End while iterCustomer.hasNext()
        return null;
    }

    /**
     * Hämtar lista innehållandes presentation av transaktioner
     * @param pNo - personnummer till kontots ägare
     * @param accountId - kontots kontonummer
     * @return lista med transaktionsinformation
     */
    public ArrayList<String> getTransactions(String pNo, int accountId)
    {
        ArrayList <String> tempList = new ArrayList<>();
        boolean isCustomersAccount = false; // om kontot är kundens

        if(!customerExists(pNo)) return null;

        for (Customer c : customerList)
        {
            for (Account a : accountList)
            {
                // Om kontos och kunds personnummer överensstämmer, samt kontonummer
                if (c.getpNo().equals(pNo) && accountMatches(a,pNo,accountId))
                {
                    isCustomersAccount = true;

                    for (Map.Entry<String, Integer> entry : transactionList.entrySet())
                    {
                        if (entry.getValue() == accountId) // Om värdet motsvarar valt personnummer
                        {
                            tempList.add(entry.getKey()); // Lägger in nyckel i temporär lista
                        }
                    }
                }
            } // End for (Account a : accountList)
        } // End for (Customer c : customerList)

        if (!isCustomersAccount) return null; // Om kontot ej är kundens
        return (ArrayList<String>) tempList.clone();
    }

    /**
     * Undersöker om konto matchar person- och kontonummer
     * @param account - konto-objekt
     * @param pNo - personnummer
     * @param accountId kontonummer
     * @return true om konto matchar person- och kontonummer, annars false
     */
    private boolean accountMatches(Account account, String pNo, int accountId)
    {
        return account.getpNo().equals(pNo) && account.getAccountId() == accountId;
    }

    /**
     * Undersöker om kund existerar
     * @param pNo - personnummer
     * @return true om kund med valt personnummer existerar, annars false
     */
    private boolean customerExists(String pNo)
    {
        for (Customer c : customerList)
        {
            // Om kunds personnummer är samma som valt
            if (c.getpNo().equals(pNo))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Undersöker om förnamn och efternamn är tillåtet
     * @param name - förnamn
     * @param surname - efternamn
     * @return true om förnamn och efternamn är tillåtna
     * @throws InvalidCustomerException - felaktigt förnamn och/eller efternamn
     */
    private boolean isNameAllowed(String name, String surname) throws InvalidCustomerException
    {
        if (name.equals(""))
        {
            throw new InvalidCustomerException("Var vänlig fyll i förnamn");
        }
        else if(surname.equals(""))
        {
            throw new InvalidCustomerException("Var vänlig fyll i efternamn");
        }
        else if (!name.matches("[a-zA-Z]+([\s-][a-zA-Z]+)*") || !surname.matches("[a-zA-Z]+([\s'-][a-zA-Z]+)*"))
        {
            throw new InvalidCustomerException("Namn kan endast innehålla bokstäver, mellanslag och bindestreck\nEfternamn kan även innehålla apostrof");
        }
        return true;
    }

    /**
     * Undersöker om personnummer är tillåtet
     * @param pNo - personnummer
     * @return true om personnummer är tillåtet
     * @throws InvalidCustomerException - felaktigt personnummer
     */
    private boolean isPNoAllowed(String pNo) throws InvalidCustomerException
    {
        if(!pNo.matches("^((20)?[0-2][0-1]|(19)?[0-9]{2})((0[1-9])|(10|11|12))([0][1-9]|[1-2][0-9]|[3][0|1])[-+]?[0-9]{4}$"))
        {
            throw new InvalidCustomerException("Skriv in ett giltigt personnummer (ÅÅMMDD-xxxx)");
        }
        if (customerExists(pNo))
        {
            throw new InvalidCustomerException("Kund med detta personnummer finns redan i banken");
        }
        return true;
    }

    /**
     * Formaterar personnummer till YYYYMMDDXXXX
     * @param pNo - personnummer
     * @return formaterat personnummer
     */
    private String formatPNo(String pNo)
    {
        StringBuilder tempString = new StringBuilder(pNo);

        if (!pNo.startsWith("19") && !pNo.startsWith("20"))
        {
            tempString.insert(0,"19");
        }
        if (tempString.indexOf("-") == 8 || tempString.indexOf("+") == 8)
        {
            tempString.deleteCharAt(8);
        }
        return tempString.toString();
    }

    /**
     * Konverterar en double till sträng
     * @param value - värde som ska konverteras
     * @return konverterade värdet
     */
    static String convertToBigDecimal(double value)
    {
        BigDecimal tempBig = new BigDecimal(Double.toString(value)); // Skapar BigDecimal av värdets strängrepresentationen
        tempBig = tempBig.setScale(3, RoundingMode.HALF_EVEN);

        return tempBig.stripTrailingZeros().toPlainString();
    }

    /**
     * Konverterar int till en sträng
     * @param value - värde som ska konverteras
     * @return konverterade värdet
     */
    static String convertToBigDecimal(int value)
    {
        BigDecimal tempBig = new BigDecimal(Integer.toString(value)); // Skapar BigDecimal av värdets strängrepresentation
        return tempBig.stripTrailingZeros().toPlainString();
    }

    /**
     * Konverterar BigDecimal till valutasträng
     * @param value värde att konvertera
     * @return konverterad sträng
     */
    public static String convertToCurrency(BigDecimal value) // statisk för krävs inget objekt
    {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    /**
     * Konverterar int till valutasträng
     * @param value värde att konvertera
     * @return konverterad sträng
     */
    public static String convertToCurrency(int value) // statisk för krävs inget objekt
    {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    /**
     * Lägger till transaktion i lista
     * @param transactionInfo transaktionsinformation
     * @param accountId kontonummer
     */
    public static void addTransaction(String transactionInfo, Integer accountId)
    {
        transactionList.put(transactionInfo, accountId);
    }

    /**
     * Getter för transaktionslista
     * @return lista innehållandes transaktioner
     */
    public LinkedHashMap<String, Integer> getTransactionList()
    {
        return transactionList;
    }

    /**
     * Setter för transaktionslista
     * @param tempTransactionList lista för transaktioner
     */
    public void setTransactionList(LinkedHashMap<String, Integer> tempTransactionList)
    {
        transactionList = tempTransactionList;
    }

    /**
     * Getter för kundlista
     * @return lista innehållandes kunder
     */
    public ArrayList<Customer> getCustomerList()
    {
        return customerList;
    }

    /**
     * Setter för kundlista
     * @param tempCustomerList vald kundlista
     */
    public void setCustomerList(ArrayList<Customer> tempCustomerList)
    {
        customerList = tempCustomerList;
    }

    /**
     * Getter för kontolista
     * @return lista innehållandes konton
     */
    public ArrayList<Account> getAccountList()
    {
        return accountList;
    }

    /**
     * Setter för kontolista
     * @param tempAccountList vald kontolista
     */
    public void setAccountList(ArrayList<Account> tempAccountList)
    {
        accountList = tempAccountList;
    }
}
