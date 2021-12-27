package chrass0;

import java.io.Serializable;

/**
 * Kund med för- och efternamn samt personnummer.
 *
 * Last changes: 2021-12-02
 * @author Christoffer Asserstam, chrass-0
 */

public class Customer implements Serializable
{
    // Instansvariabler
    private String name;
    private String surname;
    private String pNo;

    // Konstruktor som skapar och
    // initierar kund med namn, efternamn och personnummer
    public Customer(String name, String surname, String pNo)
    {
        this.name = name;
        this.surname = surname;
        this.pNo = pNo;
    }

    // Presentationsmetod
    @Override
    public String toString()
    {
        return getpNo() + " " + getName() + " " + getSurname();
    }

    // Get-metoder
    public String getName()
    {
        return name;
    }

    public String getSurname()
    {
        return surname;
    }

    public String getpNo()
    {
        return pNo;
    }

    // Set-metoder
    public void setName(String name)
    {
        this.name = name;
    }

    public void setSurname(String surname)
    {
        this.surname = surname;
    }

}
