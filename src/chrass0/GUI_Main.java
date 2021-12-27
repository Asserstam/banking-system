package chrass0;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Grafiskt användargränssnitt för systemet när inloggad
 *
 * Last changes: 2021-12-09
 * @author Christoffer Asserstam, chrass-0
 */

public class GUI_Main extends JFrame
{
   // VARIABELDEKLARERING
   private BankLogic bankLogic; // Logikobjekt
   private JButton[] leftMenuButtons; // Knappar för vänster meny

   // Paneler
   private JPanel mainPanel;
   private JPanel addPanel;
   private JPanel areaPanel;
   private JPanel leftMenuPanel;
   private JPanel scrollPanel;

   // Lista, tabell, scrollpanes och tabellmodell
   private JList customerList;
   private JTable accountTable;
   private JScrollPane customerScrollPane;
   private JScrollPane accountScrollPane;
   private DefaultTableModel accountTableModel;

   // VARIABELINITIERING
   final int FRAME_WIDTH = 500;
   final int FRAME_HEIGHT = 500;
   private String[] transactionOutputColumn = {"Datum", "Tid", "Transaktion", "Saldo"};
   private String[] deletedAccountOutputColumn = {"Kontonr", "Saldo", "Kontotyp", "Ränta"}; // Kolumn för borttaget konto
   private String[] options = {"Ja", "Nej"}; // Svarsalternativ på svenska
   private Icon chrassIcon = new ImageIcon("chrass0_files/chrassbank.png"); // Logotyp

   // Klassvariabler
   static Color bgColor = new Color(104,143,173);
   static Color fieldAndButtonColor = new Color(159,193,211);

   // Inre klass som lyssnar till menyraden
   private class MenuBarListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         String buttonText = e.getActionCommand();
         switch(buttonText)
         {
            case "Importera": importFile(); break;
            case "Exportera": exportFile(); break;
            case "Tidigare transaktioner": showTransactionsFromFile(); break;
            case "Logga ut": logout(); break;
            case "Lägg till kund": addCustomerForm(); break;
            case "Lägg till konto": addAccount(); break;
            case "Avsluta": System.exit(0); break;
         }
      }
   }

   // Inre klass som hanterar popup för högerklick på konto
   private class PopUp extends JPopupMenu
   {
      JMenuItem popItem;
      public PopUp()
      {
         popItem = new JMenuItem("Kontoutdrag till fil");
         add(popItem);

         popItem.addActionListener(new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               bankStatement();
            }
         });
      }
   }

   // Inre klass som lyssnar till högerklick på kontolista
   private class PopUpListener extends MouseAdapter
   {
      @Override
      public void mousePressed(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            doPop(e);
         }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            doPop(e);
         }
      }

      private void doPop(MouseEvent e)
      {
         PopUp menu = new PopUp();
         menu.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   // Inre klass som lyssnar till knappar i vänster meny
   private class LeftMenuListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         String buttonText = e.getActionCommand();
         switch(buttonText)
         {
            case "Lägg till kund": addCustomerForm(); break;
            case "Lägg till konto": addAccount(); break;
            case "Byt namn":
               try
               {
                  changeName();
               }
               catch (InvalidCustomerException ex)
               {
                  JOptionPane.showMessageDialog(mainPanel,ex.getMessage(),"Felmeddelande",JOptionPane.PLAIN_MESSAGE);
               }
               break;
            case "Ta bort kund": removeCustomer(); break;
            case "Ta bort konto": removeAccount(); break;
            case "Tillbaka": handleCustAcc(); break;
         }
      }
   }

   // Inre klass som lyssnar till transaktionsknappar
   private class TransactionButtonsListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         String buttonText = e.getActionCommand();
         switch(buttonText)
         {
            case "Insättning":
               try
               {
                  deposit();
               }
               catch (NumberFormatException ex)
               {
                  JOptionPane.showMessageDialog(mainPanel,"Var vänlig ange ett positivt heltal","Felmeddelande",JOptionPane.PLAIN_MESSAGE);
               }
               break;
            case "Uttag":
               try
               {
                  withdraw();
               } catch (OverdrawException ex)
               {
                  JOptionPane.showMessageDialog(mainPanel,ex.getMessage(),"Felmeddelande",JOptionPane.PLAIN_MESSAGE);
               }
               catch (NumberFormatException ex)
               {
                  JOptionPane.showMessageDialog(mainPanel,"Var vänlig ange ett positivt heltal","Felmeddelande",JOptionPane.PLAIN_MESSAGE);
               }
               break;
            case "Visa transaktioner": showTransactions(); break;
         }
      }
   }

   // Konstruktor
   public GUI_Main()
   {
      initComponents();
      buildFrame(); // skapar grundläggande frame
   }

   /**
    * Initierar kompontenter
    */
   private void initComponents()
   {
      bankLogic = new BankLogic();
      mainPanel = new JPanel();
      addPanel = new JPanel();
      areaPanel = new JPanel();
      scrollPanel = new JPanel();
   }

   /**
    * Skapar grundläggande fönster med menyer, paneler och lyssnare
    */
   private void buildFrame()
   {
      // Fönster-inställningar
      setTitle("Chrass-bank");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(FRAME_WIDTH,FRAME_HEIGHT);
      setResizable(false);
      setLocationRelativeTo(null);
      setBackground(bgColor);

      // Huvudpanels layout och bakgrundsfärg
      mainPanel.setLayout(new BorderLayout(15,0));
      mainPanel.setBackground(bgColor);

      // Logotyp
      JLabel iconLabel = new JLabel();
      iconLabel.setIcon(chrassIcon);
      iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

      // Tomma borders för positionering av komponenter
      mainPanel.setBorder(BorderFactory.createEmptyBorder(10,5,50,75));
      iconLabel.setBorder(BorderFactory.createEmptyBorder(0,100,0,0));

      // Lägger till komponenter i huvudpanel
      mainPanel.add(iconLabel,BorderLayout.PAGE_START);

      // Lägger till huvudpanel
      add(mainPanel);

      buildMenu(); // skapar menyrad inklusive lyssnare
      buildLeftMenu(); // skapar vänstermenyn
      handleCustAcc(); // vy för kund- och kontohantering
   }

   /**
    * Skapar vänster meny
    */
   private void buildLeftMenu()
   {
      // Skapar panel för vänster meny
      leftMenuPanel = new JPanel();
      leftMenuPanel.setBackground(bgColor);
      leftMenuPanel.setLayout(new BoxLayout(leftMenuPanel, BoxLayout.Y_AXIS));

      // Skapar knappar till vänster meny med tillhörande text och vertikalavstånd
      leftMenuButtons = new JButton[6];
      String[] leftMenuButtonText = {"Lägg till kund", "Ta bort kund", "Byt namn", "Lägg till konto", "Ta bort konto", "Tillbaka"};
      leftMenuPanel.add(Box.createVerticalStrut(10));
      for (int i = 0; i < 6; i++)
      {
         if (i == 3) leftMenuPanel.add(Box.createVerticalStrut(20)); // Större avstånd mellan kundknappar och kontoknappar
         leftMenuPanel.add(Box.createVerticalStrut(5));
         leftMenuButtons[i] = new JButton(leftMenuButtonText[i]);
         leftMenuButtons[i].addActionListener(new LeftMenuListener());
         leftMenuButtons[i].setMaximumSize(new Dimension(115,20));
         leftMenuPanel.add(leftMenuButtons[i]);
      }
      mainPanel.add(leftMenuPanel,BorderLayout.LINE_START);
   }

   /**
    * Vy för kund- och kontohantering
    */
   private void handleCustAcc()
   {
      // Visar och döljer knappar i vänster meny
      for (int i = 0; i < 5; i++)
      {
         leftMenuButtons[i].setVisible(true);
      }
      leftMenuButtons[5].setVisible(false);

      // Tar bort paneler som en används
      scrollPanel.removeAll();
      mainPanel.remove(addPanel);

      // Skapar lista för användare
      customerList = new JList(bankLogic.getAllCustomers().toArray());
      customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      customerList.setLayoutOrientation(JList.VERTICAL);
      customerList.setVisibleRowCount(25);
      customerList.setBorder(BorderFactory.createTitledBorder("Kunder"));
      customerList.setBackground(bgColor);

      // Skapar scrollpane för användare
      customerScrollPane = new JScrollPane(customerList);
      customerScrollPane.setBorder(null);

      // Tabellmodell med överskuggad metod som nekar editering
      String[] accountColumn = {"Kontonr" , "Saldo", "Kontotyp"};

      accountTableModel = new DefaultTableModel(null,accountColumn)
      {
         @Override
         public boolean isCellEditable(int row, int column)
         {
            return false;
         }
      };

      // Skapar tabell för konton
      accountTable = new JTable(accountTableModel);
      accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      accountTable.getTableHeader().setReorderingAllowed(false);

      // Skapar scrollpane för konton
      accountScrollPane = new JScrollPane(accountTable);
      accountScrollPane.setBorder(BorderFactory.createTitledBorder("Konton"));
      accountScrollPane.getViewport().setBackground(bgColor);

      // Layout för scrollpanel
      scrollPanel.setLayout(new BoxLayout(scrollPanel,BoxLayout.Y_AXIS));
      scrollPanel.setBackground(bgColor);

      // Skapar panel för transaktionsknappar
      JPanel transButtonPanel = new JPanel();
      transButtonPanel.setBackground(bgColor);

      // Skapar transaktionsknappar samt lyssnare till dessa
      JButton[] transButtons = new JButton[3];
      String[] transButtonText = {"Insättning", "Uttag", "Visa transaktioner"};
      for (int i = 0; i < 3; i++)
      {
         transButtons[i] = new JButton(transButtonText[i]);
         transButtonPanel.add(transButtons[i]);
         transButtons[i].addActionListener(new TransactionButtonsListener());
      }

      // Lägger in komponenter i huvudkomponenter
      scrollPanel.add(customerScrollPane);
      scrollPanel.add(accountScrollPane);
      scrollPanel.add(transButtonPanel);
      scrollPanel.setBackground(bgColor);
      mainPanel.add(scrollPanel, BorderLayout.CENTER);

      // Metoder för att uppdatera fönster
      revalidate();
      repaint();

      // Anonym inre klass för, klick på kund visar dennes konton
      customerList.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mousePressed(MouseEvent e)
         {
            // Om kund väljs uppdateras kontolista
            if (e.getButton() == MouseEvent.BUTTON1)
            {
               showAccounts();
            }
         }
      });

      // Anonym inre klass för, dubbelklick på konto visar transaktioner
      accountTable.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent e)
         {
            // Om kund är vald
            if (!customerList.isSelectionEmpty())
            {
               // Om dubbelklick med pekdon
               if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
               {
                  showTransactions();
               }
            }
         }
      });

      // Lägger till lyssnare för popup-meny
      accountTable.addMouseListener(new PopUpListener());
   }

   /**
    * Skapar menyrad med tillhörande lyssnare
    */
   private void buildMenu()
   {
      // Skapar menyradskomponenter
      JMenuBar menuBar = new JMenuBar();
      JMenu archives = new JMenu("Arkiv");
      JMenuItem importFile = new JMenuItem("Importera");
      JMenuItem exportFile = new JMenuItem("Exportera");
      JMenuItem savedTransactions = new JMenuItem("Tidigare transaktioner");
      JMenuItem logout = new JMenuItem("Logga ut");
      JMenuItem exit = new JMenuItem("Avsluta");
      JMenu customer = new JMenu("Lägg till");
      JMenuItem addAccount = new JMenuItem("Lägg till konto");
      JMenuItem addCustomer = new JMenuItem("Lägg till kund");

      // Lägger till menyfält
      archives.add(importFile);
      archives.add(exportFile);
      archives.add(savedTransactions);
      archives.add(logout);
      archives.add(exit);
      customer.add(addCustomer);
      customer.add(addAccount);
      menuBar.add(archives);
      menuBar.add(customer);
      setJMenuBar(menuBar);

      // Lyssnare för menyfält
      importFile.addActionListener(new MenuBarListener());
      exportFile.addActionListener(new MenuBarListener());
      savedTransactions.addActionListener(new MenuBarListener());
      logout.addActionListener(new MenuBarListener());
      addCustomer.addActionListener(new MenuBarListener());
      addAccount.addActionListener(new MenuBarListener());
      exit.addActionListener(new MenuBarListener());
   }

   /**
    * Loggar ut
    */
   private void logout()
   {
      dispose(); // Tar bort inloggad fönster

      // Skapar inloggningsfönster
      GUI_Login frame = new GUI_Login();
      frame.setVisible(true);
   }

   /**
    * Vy för att lägga till kund
    */
   private void addCustomerForm()
   {
      // Ändrar knappar som visas i vänster meny
      for (int i = 0; i < 5; i++)
      {
         leftMenuButtons[i].setVisible(false);
      }
      leftMenuButtons[5].setVisible(true);

      // Tar bort paneler
      mainPanel.remove(scrollPanel);
      addPanel.removeAll();

      // Ändrar bakgrundsfärg
      getContentPane().setBackground(bgColor);

      // Skapar panel för formulär
      JPanel formPanel = new JPanel();
      formPanel.setLayout(new GridLayout(6,1));
      formPanel.setMaximumSize(new Dimension(200,170));
      formPanel.setBackground(bgColor);

      // Panel för formulär och knappar
      addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
      addPanel.setBackground(bgColor);

      // Panel för knappar
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));

      // Skapar och lägger till knappar i panel
      JButton addCustomerButton = new JButton("Lägg till");
      JButton clearButton = new JButton("Rensa");
      buttonPanel.add(addCustomerButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));
      buttonPanel.add(clearButton);
      buttonPanel.setBackground(bgColor);

      // Skapar border för formulär
      Border border = BorderFactory.createTitledBorder("Skapa kund");
      formPanel.setBorder(BorderFactory.createCompoundBorder(border,BorderFactory.createEmptyBorder(0,10,5,10)));

      // Skapar labels och fält för formulär
      JLabel[] addCustomerLabels = new JLabel[3];
      String[] addCustomerText = {"Namn", "Efternamn", "Personnummer"};
      JTextField[] addCustomerFields = new JTextField[3];

      for (int i = 0; i < 3; i++)
      {
         addCustomerLabels[i] = new JLabel(addCustomerText[i]);
         addCustomerFields[i] = new JTextField(10);
         addCustomerFields[i].setBorder(null);
         formPanel.add(addCustomerLabels[i]);
         formPanel.add(addCustomerFields[i]);
      }

      // Lägger in formulär och knappar i panel, som läggs in i huvudpanel
      addPanel.add(formPanel);
      addPanel.add(Box.createVerticalStrut(10));
      addPanel.add(buttonPanel);
      mainPanel.add(addPanel);

      // Uppdaterar paneler
      mainPanel.revalidate();
      mainPanel.repaint();

      // Knappval vid enter
      getRootPane().setDefaultButton(addCustomerButton);

      // Anonym inre klass för knapp 'Lägg till'
      addCustomerButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            // Valt förnamn, efternamn och personnummer på ny kund
            String customerInfo = "Namn: " + addCustomerFields[0].getText() + "\nEfternamn: " + addCustomerFields[1].getText()
            + "\nPersonnummer: " + addCustomerFields[2].getText();

            // Val att skapa användare
            int input = JOptionPane.showOptionDialog(mainPanel, "Vill du skapa användare? \n" + customerInfo,
            "Välj ett alternativ",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

            // Om val är 'Ja' skapar användare
            if (input == 0)
            {
               try
               {
                  bankLogic.createCustomer(addCustomerFields[0].getText(),addCustomerFields[1].getText(),addCustomerFields[2].getText());
                  JOptionPane.showMessageDialog(mainPanel,"Användare skapad","",JOptionPane.PLAIN_MESSAGE);
                  handleCustAcc(); // Återgår till vy för kund- och kontohantering
               } catch (InvalidCustomerException ex)
               {
                  JOptionPane.showMessageDialog(mainPanel,ex.getMessage(),"Felmeddelande",JOptionPane.PLAIN_MESSAGE);
               }
            } // end if (input == 0)

            // Rensar textfälten
            addCustomerFields[0].setText("");
            addCustomerFields[1].setText("");
            addCustomerFields[2].setText("");
         }
      });

      // Rensa-knapp
      clearButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            // Rensar textfälten
            addCustomerFields[0].setText("");
            addCustomerFields[1].setText("");
            addCustomerFields[2].setText("");
         }
      });
   }

   /**
    * Vy för att lägga till konto
    */
   private void addAccount()
   {
      // Uppdaterar kundlista och markerar första kund
      customerList.setListData(bankLogic.getAllCustomers().toArray());
      customerList.setSelectedIndex(0);

      // Maxstorlek på kundlista
      customerScrollPane.setMaximumSize(new Dimension(250,500));

      // Ändrar knappar som visas i vänster meny
      for (int i = 0; i < 5; i++)
      {
         leftMenuButtons[i].setVisible(false);
      }
      leftMenuButtons[5].setVisible(true);

      // Tar bort paneler
      mainPanel.remove(scrollPanel);
      areaPanel.removeAll();
      addPanel.removeAll();

      // Skapar panel för radio-knappar
      JPanel radioButtonPanel = new JPanel();

      // Layout för paneler
      radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.X_AXIS));
      addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
      areaPanel.setLayout(new BoxLayout(areaPanel,BoxLayout.Y_AXIS));
      areaPanel.setBackground(bgColor);

      // Skapar knappgrupp och knappar
      ButtonGroup radioButtons = new ButtonGroup();
      JRadioButton savingsAccountButton = new JRadioButton("Sparkonto", true);
      JRadioButton creditAccountButton = new JRadioButton("Kreditkonto");
      savingsAccountButton.setBackground(bgColor);
      creditAccountButton.setBackground(bgColor);

      // Lägger in radioknappar i grupp och panel
      radioButtons.add(savingsAccountButton);
      radioButtons.add(creditAccountButton);
      radioButtonPanel.add(savingsAccountButton);
      radioButtonPanel.add(creditAccountButton);

      // Skapar knapp för 'Skapa konto'
      JButton createAccountButton = new JButton("Skapa konto");
      createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT); // centrerar knapp för skapa konto

      // Lägger in komponenter i paneler samt paneler i paneler
      addPanel.add(radioButtonPanel);
      areaPanel.add(customerScrollPane);
      leftMenuPanel.add(Box.createVerticalStrut(5));
      addPanel.add(areaPanel);
      addPanel.add(Box.createVerticalStrut(10));
      addPanel.add(createAccountButton);
      addPanel.setBackground(bgColor);
      mainPanel.add(addPanel);

      // Knappval vid enter
      getRootPane().setDefaultButton(createAccountButton);

      // Metoder för att uppdatera fönster
      mainPanel.revalidate();
      mainPanel.repaint();

      // Anonym inre klass för knapp 'Skapa konto'
      createAccountButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            // Om någon kund är vald
            if (!customerList.isSelectionEmpty())
            {
               // Sparar förnamn och efternamn från vald kund
               String[] parts = customerList.getSelectedValue().toString().split(" ");
               String name = parts[1];
               String surname = parts[2];

               // Om radioknapp 'Sparkonto' är vald
               if (savingsAccountButton.isSelected())
               {
                  // Val att skapa Sparkonto
                  int input = JOptionPane.showOptionDialog(mainPanel, "Vill du skapa ett sparkonto?", "Välj ett alternativ",
                  JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

                  // Om 'Ja' så skapas sparkonto och visas i informationsruta
                  if (input == 0)
                  {
                     bankLogic.createSavingsAccount(parts[0]);
                     JOptionPane.showMessageDialog(mainPanel,"Sparkonto är skapat till " + name + " " + surname,"Meddelande",
                     JOptionPane.PLAIN_MESSAGE);
                     handleCustAcc(); // Återgår till vy för kund- och kontohantering
                  }
               }

               // Om radioknapp 'Kreditkonto' är vald
               else if (creditAccountButton.isSelected())
               {
                  // Val att skapa kreditkonto
                  int input = JOptionPane.showOptionDialog(mainPanel, "Vill du skapa ett kreditkonto?", "Välj ett alternativ",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

                  // Om 'Ja' så skapas kreditkonto och visas i informationsruta
                  if (input == 0)
                  {
                     bankLogic.createCreditAccount(parts[0]);
                     JOptionPane.showMessageDialog(mainPanel,"Kreditkonto är skapat till " + name + " " + surname,"Meddelande",JOptionPane.PLAIN_MESSAGE);
                     handleCustAcc(); // Återgår till vy för kund- och kontohantering
                  }
               }
            } // end if (!customerList.isSelectionEmpty())
         }
      });
   }

   /**
    * Visar vald kunds konton
    */
   private void showAccounts()
   {
      // Lista med kundens konton
      ArrayList <String> customersAccounts;

      // Om någon kund är vald
      if (!customerList.isSelectionEmpty())
      {
         // Nollställer lista
         accountTableModel.setRowCount(0);

         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Kunds kontoinformation
         customersAccounts = bankLogic.getCustomer(pNr);

         // Lägger in kundens kontoinformation i tabell
         for (int i = 1; i < customersAccounts.size(); i++)
         {
            String[] customerRow = customersAccounts.get(i).split(" ");
            accountTableModel.addRow(customerRow);
         }
      } // end if (!customerList.isSelectionEmpty())
   }

   /**
    * Byter namn på kund
    */
   private void changeName() throws InvalidCustomerException
   {
      int selectionIndex;
      if (!customerList.isSelectionEmpty())
      {
         // Sparar personnummer från markerad kund
         selectionIndex = customerList.getSelectedIndex();
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Panel för förnamn
         JPanel namePanel = new JPanel();
         JLabel nameText = new JLabel("Nytt förnamn");
         JTextField nameField = new JTextField("",10);
         namePanel.add(nameText);
         namePanel.add(Box.createRigidArea(new Dimension(5,0)));
         namePanel.add(nameField);

         // Panel för efternamn
         JPanel surnamePanel = new JPanel();
         JLabel surnameText = new JLabel("Nytt efternamn");
         JTextField surnameField = new JTextField("",10);
         surnamePanel.add(surnameText);
         surnamePanel.add(surnameField);

         // Panel för namn som används i bekräftelseruta
         JPanel newNamePanel = new JPanel();
         newNamePanel.setLayout(new BoxLayout(newNamePanel,BoxLayout.Y_AXIS));
         newNamePanel.add(namePanel);
         newNamePanel.add(Box.createVerticalStrut(5));
         newNamePanel.add(surnamePanel);

         // Bekräftelseruta
         int ret = JOptionPane.showConfirmDialog(mainPanel,newNamePanel,"Namnbyte", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

         // Om val 'OK' byts namn på kund
         if (ret == JOptionPane.OK_OPTION)
         {
            bankLogic.changeCustomerName(nameField.getText(), surnameField.getText(), pNr);
         }
         // Uppdaterar kundlista
         customerList.setListData(bankLogic.getAllCustomers().toArray());

         // Markerar kund
         customerList.setSelectedIndex(selectionIndex);

      } // end if (!customerList.isSelectionEmpty())
   }

   /**
    * Tar bort vald kund och dennes konton
    */
   private void removeCustomer()
   {
      // Temporär lista för borttagen kunds information
      ArrayList <String> tempList;

      // Om någon kund är markerad
      if (!customerList.isSelectionEmpty())
      {
         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Val att ta bort und
         int input = JOptionPane.showOptionDialog(this, "Vill du ta bort kund?", "Välj ett alternativ",
         JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

         // Om 'Ja' tas kund bort och visar kontoinformation
         if (input == 0)
         {
            // Tar bort kund och sparar information i lista
            tempList = bankLogic.deleteCustomer(pNr);
            String customerInfo = tempList.get(0);

            // Uppdaterar kund- och kontolista
            customerList.setListData(bankLogic.getAllCustomers().toArray());
            accountTableModel.setRowCount(0);

            // Skapar tabellmodell för tydlig presentation av avslutade konton
            DefaultTableModel outputCloseCustomerTableModel = new DefaultTableModel(null, deletedAccountOutputColumn);
            JTable tempTable = new JTable(outputCloseCustomerTableModel);
            tempTable.setEnabled(false);

            // Lägger in kundens kontoinformation i tabell
            for (int i = 1; i < tempList.size(); i++)
            {
               String[] tempRow = tempList.get(i).split(" ");
               outputCloseCustomerTableModel.addRow(tempRow);
            }

            // Skapar pane för informationsmeddelande och bestämmer storlek
            JScrollPane outputPane = new JScrollPane(tempTable);
            outputPane.setPreferredSize(new Dimension(350,200));

            // Informationsmeddelande om avslutad kunds konton
            JOptionPane.showMessageDialog(mainPanel,outputPane,"Avslutade konton för " + customerInfo, JOptionPane.PLAIN_MESSAGE);

         } // end if (input == 0)
      } // end if (!customerList.isSelectionEmpty())
   }

   /**
    * Tar bort valt konto
    */
   private void removeAccount()
   {
      // Om konto valt i kontolista
      if(!accountTable.getSelectionModel().isSelectionEmpty())
      {
         String removedAccountInfo;
         int selectedRow;
         int accountId;

         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Vald rad i kontolistas kontonummer
         selectedRow = accountTable.getSelectedRow();
         accountId = Integer.parseInt(accountTable.getValueAt(selectedRow,0).toString());

         // Val att ta bort konto
         int input = JOptionPane.showOptionDialog(this, "Vill du ta bort kontot?", "Välj ett alternativ",
         JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

         // Om 'Ja' tas konto bort och visar kontoinformation
         if (input == 0)
         {
            // Sträng med avslutat kontos info
            removedAccountInfo = bankLogic.closeAccount(pNr,accountId);

            // Skapar tabell inklusive tabellmodell
            DefaultTableModel outputCloseAccountTableModel = new DefaultTableModel(null, deletedAccountOutputColumn);
            JTable tempTable = new JTable(outputCloseAccountTableModel);
            tempTable.setEnabled(false);

            // Lägger till kontoinformation i tabell
            String[] tempRow = removedAccountInfo.split(" ");
            outputCloseAccountTableModel.addRow(tempRow);


            // Skapar pane för informationsmeddelande och bestämmer storlek
            JScrollPane outputPane = new JScrollPane(tempTable);
            outputPane.setPreferredSize(new Dimension(300,60));

            // Informationsmeddelande om avslutad kunds konto
            JOptionPane.showMessageDialog(this,outputPane,"Kontot är avslutat", JOptionPane.PLAIN_MESSAGE);

         } // end if (input == 0)

         // Uppdaterar kunds konton
         showAccounts();
      }
   }

   /**
    * Insättning på konto
    */
   private void deposit()
   {
      // Om något konto är valt
      if(!accountTable.getSelectionModel().isSelectionEmpty())
      {
         int selectedRow;
         int accountId;
         int amount;

         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Vald rad i kontolistas kontonummer
         selectedRow = accountTable.getSelectedRow();
         accountId = Integer.parseInt(accountTable.getValueAt(selectedRow,0).toString());

         // Val av summa att sätta in
         String input = JOptionPane.showInputDialog(scrollPanel,"","Insättning " + accountId,JOptionPane.PLAIN_MESSAGE);

         if (input !=  null)
         {
            amount = Integer.parseInt(input);
            if (amount > 0)
            {
               // Insättning genomförs
               bankLogic.deposit(pNr,accountId,amount);
            }
            else
            {
               JOptionPane.showMessageDialog(mainPanel,"Var vänlig ange ett positivt heltal","",JOptionPane.PLAIN_MESSAGE);
            }

            // Uppdaterar kontolista
            showAccounts();
         }
      } // end if(!accountTable.getSelectionModel().isSelectionEmpty())
   }

   /**
    * Uttag från konto
    */
   private void withdraw() throws OverdrawException
   {
      // Om något konto är valt
      if(!accountTable.getSelectionModel().isSelectionEmpty())
      {
         int selectedRow;
         int accountId;
         int amount;

         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Vald rad i kontolistas kontonummer
         selectedRow = accountTable.getSelectedRow();
         accountId = Integer.parseInt(accountTable.getValueAt(selectedRow,0).toString());

         // Val av summa att ta ut
         String input = JOptionPane.showInputDialog(scrollPanel,"", "Uttag " + accountId,JOptionPane.PLAIN_MESSAGE);

         if (input != null)
         {
            amount = Integer.parseInt(input);
            if (amount > 0)
            {
               // Uttag genomförs
               bankLogic.withdraw(pNr,accountId,amount);
            }
            else
            {
               // Felmeddelande
               JOptionPane.showMessageDialog(mainPanel,"Var vänlig ange ett positivt heltal","",JOptionPane.PLAIN_MESSAGE);
            }

            // Uppdaterar kontolista
            showAccounts();
         }
      } // end if(!accountTable.getSelectionModel().isSelectionEmpty())
   }

   /**
    *  Visar kontos transaktioner
    */
   private void showTransactions()
   {
      int selectedRow;
      int accountId;
      ArrayList <String> tempList;

      // Om kund är vald samt om konto är valt
      if (!customerList.isSelectionEmpty() && !accountTable.getSelectionModel().isSelectionEmpty())
      {
         // Sparar personnummer från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];

         // Vald rad i kontolistas kontonummer
         selectedRow = accountTable.getSelectedRow();
         accountId = Integer.parseInt(accountTable.getValueAt(selectedRow,0).toString());

         // Sparar kontos transaktioner i lista
         tempList = bankLogic.getTransactions(pNr, accountId);

         // Skapar tabell inklusive tabellmodell
         DefaultTableModel outputTransactionModel = new DefaultTableModel(null,transactionOutputColumn);
         JTable tempTable = new JTable(outputTransactionModel);
         tempTable.setEnabled(false);

         // Lägger in kontos transaktioner i tabell
         for (int i = 0; i < tempList.size(); i++)
         {
            String[] tempRow = tempList.get(i).split(" ");
            outputTransactionModel.addRow(tempRow);
         }

         // Skapar scrollpane innehållandes transaktionsinformation
         JScrollPane outputPane = new JScrollPane(tempTable);
         outputPane.setPreferredSize(new Dimension(400,200));

         // Informationsmeddelande med transaktionsinformation
         JOptionPane.showMessageDialog(scrollPanel,outputPane,"Transaktioner " + accountId, JOptionPane.PLAIN_MESSAGE);

      } // end if (!customerList.isSelectionEmpty() && !accountTable.getSelectionModel().isSelectionEmpty())
   }

   /**
    * Exporterar fil innehållandes kund-, konto- och transaktionsinformation
    */
   private void exportFile()
   {
      File selectedFile = null;
      try
      {
         // Ruta för val av fil
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setCurrentDirectory(new File("chrass0_files"));
         FileNameExtensionFilter txtfilter = new FileNameExtensionFilter("txt filer (*.txt)","txt");
         fileChooser.setFileFilter(txtfilter);
         int res = fileChooser.showSaveDialog(this);

         // Om val är 'Ja'
         if (res == JFileChooser.APPROVE_OPTION)
         {
            selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            // Om det ej är textfil
            if(!filePath.endsWith(".txt"))
            {
               selectedFile = new File(filePath + ".txt");
            }
         }

         // Om fil redan existerar krävs bekräftelse för att spara över
         if (selectedFile.exists())
         {
            int input = JOptionPane.showOptionDialog(mainPanel, "Vill du skriva över " + selectedFile.getName() + "?", "Välj ett alternativ",
            JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

            // Om 'Ja' så sparas vald fil
            if (input == 0)
            {
               exportObjects(selectedFile);
            }
         }
         else
         {
            exportObjects(selectedFile);
         }
      }
      catch (NullPointerException ignored)
      {

      }
   }

   /**
    * Visar tidigare sparade transaktioner från fil
    */
   private void showTransactionsFromFile()
   {
      File selectedFile;
      String correctFileCheck = "";

      // Ruta för val av fil
      JFileChooser fileChooser = new JFileChooser();
      FileNameExtensionFilter txtfilter = new FileNameExtensionFilter("txt filer (*.txt)","txt");
      fileChooser.setFileFilter(txtfilter);
      fileChooser.setCurrentDirectory(new File("chrass0_files"));

      // Skapar tabellmodell för tydlig presentation av avslutade konton
      DefaultTableModel transactionTableModel = new DefaultTableModel(null, transactionOutputColumn);
      JTable tempTable = new JTable(transactionTableModel);
      tempTable.setEnabled(false);

      // Dialogruta
      int res = fileChooser.showOpenDialog(this);
      if (res == JFileChooser.APPROVE_OPTION)
      {
         selectedFile = fileChooser.getSelectedFile();

         // Läser in text radvis från textfil
         try (BufferedReader input = new BufferedReader(new FileReader(selectedFile)))
         {
            String tempLine;
            String line;

            // Tre första rader från textfil används ej i dialogruta
            correctFileCheck = input.readLine();
            input.readLine();
            input.readLine();

            // Läser in rader från fil och lägger in i tabell
            while ((line = input.readLine()) != null)
            {
               tempLine = line.trim().replaceAll(" +", " ");
               String[] tempRow = tempLine.split(" ");
               transactionTableModel.addRow(tempRow);
            }

         }
         catch (IOException ex)
         {
            JOptionPane.showMessageDialog(this,ex.getMessage(), "Felmeddelande",JOptionPane.PLAIN_MESSAGE);
         }
         catch (NullPointerException ignored)
         {

         }

         // Skapar pane för informationsmeddelande och bestämmer storlek
         JScrollPane outputPane = new JScrollPane(tempTable);
         outputPane.setPreferredSize(new Dimension(350,200));

         if (correctFileCheck.contains("Kontoutdrag"))
         {
            // Informationsmeddelande om avslutad kunds konton
            JOptionPane.showMessageDialog(mainPanel,outputPane,"Transaktionshistorik " + selectedFile.getName(), JOptionPane.PLAIN_MESSAGE);
         }
         else
         {
            // Felmeddelande om att fil ej är transaktionsfil
            JOptionPane.showMessageDialog(this,selectedFile.getName() + " är ej en giltig transaktionsfil",
            "Felmeddelande",JOptionPane.PLAIN_MESSAGE);
         }
      } // end if(res == JFileChooser.APPROVE_OPTION)
   }

   /**
    * Importerar fil innehållandes kund-, konto- och transaktionsinformation
    */
   private void importFile()
   {
      File selectedFile = null;
      try
      {
         // Ruta för val av fil
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter txtfilter = new FileNameExtensionFilter("txt filer (*.txt)","txt");
         fileChooser.setFileFilter(txtfilter);
         fileChooser.setCurrentDirectory(new File("chrass0_files"));

         int res = fileChooser.showOpenDialog(this);
         if (res == JFileChooser.APPROVE_OPTION)
         {
            selectedFile = fileChooser.getSelectedFile();
         }

         // Importerar kunder, konton och transaktioner från fil
         ObjectInputStream infil = new ObjectInputStream(new FileInputStream(selectedFile));
         int lastAccountNrFromFile = infil.readInt();
         ArrayList<Customer> customerFromFile = (ArrayList<Customer>) infil.readObject();
         ArrayList<Account> accountsFromFile = (ArrayList<Account>) infil.readObject();
         LinkedHashMap<String, Integer> transactionsFromFile = (LinkedHashMap<String, Integer>) infil.readObject();

         // Senast kontonummer, kund-, konto- och transaktionslista bestäms
         Account.setLastAssignedNumber(lastAccountNrFromFile);
         bankLogic.setCustomerList(customerFromFile);
         bankLogic.setAccountList(accountsFromFile);
         bankLogic.setTransactionList(transactionsFromFile);

         // Stänger filström
         infil.close();

         // Uppdaterar kund- och kontolista
         customerList.setListData(bankLogic.getAllCustomers().toArray());
         accountTableModel.setRowCount(0);
      }
      catch (IOException | ClassNotFoundException ex)
      {
         JOptionPane.showMessageDialog(this,ex.getMessage(), "Felmeddelande",JOptionPane.PLAIN_MESSAGE);
      }
      catch (NullPointerException ignored)
      {

      }
   }

   /**
    * Skriver ut kontoutdrag till textfil
    */
   private void bankStatement()
   {
      File selectedFile;
      int selectedRow;
      int accountId;
      ArrayList <String> tempList;

      String accountIdtoString;

      // Dagens datum
      SimpleDateFormat sfd = new SimpleDateFormat("[yy-MM-dd]");
      String thisDate = sfd.format(new Date());

      // Om kund är vald samt om konto är valt
      if (!customerList.isSelectionEmpty() && !accountTable.getSelectionModel().isSelectionEmpty())
      {
         // Sparar personnummer, namn och efternamn från markerad kund
         String[] parts = customerList.getSelectedValue().toString().split(" ");
         String pNr = parts[0];
         String name = parts[1];
         String surname = parts[2];

         // Vald rad i kontolistas kontonummer
         selectedRow = accountTable.getSelectedRow();
         accountId = Integer.parseInt(accountTable.getValueAt(selectedRow,0).toString());

         // Kontonummer i strängformat
         accountIdtoString = accountId + "";

         // Sparar kontos transaktioner i lista
         tempList = bankLogic.getTransactions(pNr, accountId);

         // Exporterad fils namn
         String fileName = accountIdtoString + " " + thisDate;

         // Ruta för val av fil
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setCurrentDirectory(new File("chrass0_files"));

         FileNameExtensionFilter txtfilter = new FileNameExtensionFilter("txt filer (*.txt)","txt");
         fileChooser.setFileFilter(txtfilter);
         fileChooser.setSelectedFile(new File("chrass0_files/"+fileName+".txt"));
         int res = fileChooser.showSaveDialog(this);

         // Om val är 'Ja'
         if (res == JFileChooser.APPROVE_OPTION)
         {
            selectedFile = fileChooser.getSelectedFile();

            // Sparar alltid som textfil
            if (!selectedFile.getName().endsWith(".txt"))
            {
               selectedFile = new File(selectedFile + ".txt");
            }

            // Om fil redan existerar krävs bekräftelse för att spara över
            if (selectedFile.exists())
            {
               int input = JOptionPane.showOptionDialog(mainPanel, "Vill du skriva över " + selectedFile.getName() + "?", "Välj ett alternativ",
               JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null, options,null);

               // Om 'Ja' så sparas vald fil
               if (input == 0)
               {
                  exportTransactions(selectedFile, accountId, name, surname, thisDate, tempList);
               } // end if (input == 0)
            }
            else
            {
               exportTransactions(selectedFile, accountId, name, surname, thisDate, tempList);
            }
         }
      } // end if (!customerList.isSelectionEmpty() && !accountTable.getSelectionModel().isSelectionEmpty())
   }

   /**
    * Exporterar kontoutdrag till textfil
    * @param selectedFile - vald fil att exportera till
    * @param accountId - kontonummer
    * @param name - förnamn
    * @param surname - efternamn
    * @param thisDate - datum
    * @param tempList - lista innehållandes transaktioner
    */
   private void exportTransactions(File selectedFile, int accountId, String name, String surname, String thisDate, ArrayList<String> tempList)
   {
      FileWriter fw;
      String columnString = String.format("%-13s%-12s%14s%14s","Datum", "Tid", "Transaktion", "Saldo");

      // Skriver transaktionsinformation till fil
      try
      {
         fw = new FileWriter(selectedFile);

         // Skriver text till fil
         fw.write("Kontoutdrag för " + accountId + " [" + name + " " + surname + "] " + thisDate + " \n");
         fw.write("=====================================================\n");
         fw.write(columnString + "\n");

         for(String s : tempList)
         {
            fw.write(bankStatementText(s) + "\n");
         }
         fw.close();

         // Bekräftelsemeddelande för kontoutdrag
         JOptionPane.showMessageDialog(scrollPanel,"Kontoutdrag genomfört","" + accountId, JOptionPane.PLAIN_MESSAGE);

      } catch (IOException ex)
      {
         JOptionPane.showMessageDialog(scrollPanel,ex.getMessage(),"Felmeddelande", JOptionPane.PLAIN_MESSAGE);
      }

   }
   /**
    * Exporterar kunder, konton och transaktioner till fil
    * @param selectedFile - vald fil att exportera till
    */
   private void exportObjects(File selectedFile)
   {
      try
      {
         ObjectOutputStream utfil = new ObjectOutputStream(new FileOutputStream(selectedFile));
         utfil.writeInt(Account.getLastAssignedNumber());
         utfil.writeObject(bankLogic.getCustomerList());
         utfil.writeObject(bankLogic.getAccountList());
         utfil.writeObject(bankLogic.getTransactionList());
         utfil.close();
      }
      catch (IOException ex)
      {
         JOptionPane.showMessageDialog(this,ex.getMessage(), "Felmeddelande",JOptionPane.PLAIN_MESSAGE);
      }
   }

   /**
    * Hjälpmetod som formaterar transaktionsträng
    * @param transactionString sträng med transaktionsinformation
    * @return formatterad transaktionssträng
    */
   private String bankStatementText(String transactionString)
   {
      String outputString;

      String[] parts = transactionString.split(" ");
      String date = parts[0];
      String time = parts[1];
      String transaction = parts[2];
      String balance = parts[3];

      outputString = String.format("%-13s%-12s%14s%14s", date, time, transaction, balance);

      return outputString;
   }
}


