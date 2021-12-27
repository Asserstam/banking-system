package chrass0;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * Grafiskt användargränssnitt för inloggning
 *
 * Last changes: 2021-12-05
 * @author Christoffer Asserstam, chrass-0
 */

public class GUI_Login extends JFrame
{
   // VARIABELDEKLARERING
   // Fält och knappar för loginruta
   private JTextField userField;
   private JPasswordField passField;
   private JButton loginButton;
   private JButton forgotButton;

   // Paneler
   private JPanel userPanel;
   private JPanel passPanel;
   private JPanel mainPanel;
   private JPanel loginPanel;
   private JPanel buttonPanel;

   // VARIABELINITIERING
   final int FRAME_WIDTH = 500;
   final int FRAME_HEIGHT = 500;

   // Logotyp
   private Icon chrassIcon = new ImageIcon("chrass0_files/chrassbank.png");

   // Ikoner i loginruta
   private Icon userIcon = new ImageIcon("chrass0_files/user24.png");
   private Icon passIcon = new ImageIcon("chrass0_files/padlock24.png");


   // Inre klass som lyssnar till login-knappar
   private class LoginButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         String buttonText = e.getActionCommand();

         if (buttonText.equals("Logga in"))
         {
            loginPressed();
         }
         else if (buttonText.equals("Glömt lösenord"))
         {
            forgotPassword();
         }
      }
   }

   // Konstruktor
   public GUI_Login()
   {
      initComponents();
      buildFrame();
   }

   /**
    * Initierar komponenter
    */
   private void initComponents()
   {
      userField = new JTextField("användarnamn");
      passField = new JPasswordField("lösenord");
      loginButton = new JButton("Logga in");
      forgotButton = new JButton("Glömt lösenord");

      userPanel = new JPanel();
      passPanel = new JPanel();
      mainPanel = new JPanel();
      loginPanel = new JPanel();
      buttonPanel = new JPanel();
   }

   public static void main(String[] args)
   {
      // Sätter look-and-feel till Nimbus
      setNimbusLaf();

      GUI_Login frame = new GUI_Login();
      frame.setVisible(true);
   }

   /**
    * Skapar grundläggande fönster med paneler och lyssnare
    */
   private void buildFrame()
   {
      setTitle("Chrass-bank");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(FRAME_WIDTH, FRAME_HEIGHT);
      setResizable(false);
      setLocationRelativeTo(null);

      // Layout för fönster
      setBackground(GUI_Main.bgColor);
      setLayout(new BorderLayout(15, 15));

      // Layout för huvudpanel
      mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
      mainPanel.setBackground(GUI_Main.bgColor);

      // Lägger in huvudpanel
      add(mainPanel);

      buildIcons(); // Skapar logotyp och ikoner
      buildLogin(); // Skapar inloggningsruta
      buildListeners(); // Skapar lyssnare
   }

   /**
    * Skapar logotyp och ikoner
    */
   private void buildIcons()
   {
      // Logotyp och positionering
      JLabel chrassLabel = new JLabel();
      chrassLabel.setIcon(chrassIcon);
      chrassLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 40, 0));
      chrassLabel.setHorizontalAlignment(SwingConstants.CENTER);

      // Skapar användar- och lösenordsikoner
      JLabel userLabel = new JLabel();
      JLabel passLabel = new JLabel();
      userLabel.setIcon(userIcon);
      passLabel.setIcon(passIcon);

      // Lägger in ikoner i loginpanel
      userPanel.add(userLabel);
      userPanel.add(Box.createRigidArea(new Dimension(5, 0)));
      passPanel.add(passLabel);
      passPanel.add(Box.createRigidArea(new Dimension(5, 0)));

      // Lägger in logotyp i huvudpanel
      mainPanel.add(chrassLabel, BorderLayout.PAGE_START);
   }

   /**
    * Skapar inloggningsruta
    */
   private void buildLogin()
   {
      // Layout för paneler
      userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
      passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.X_AXIS));
      loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
      loginPanel.setBackground(GUI_Main.bgColor);
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setBackground(GUI_Main.bgColor);

      // Layout för användarfält
      userField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
      userField.setPreferredSize(new Dimension(190, 20));
      userField.setBackground(GUI_Main.fieldAndButtonColor);

      // Layout för lösenordsfält
      passField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
      passField.setPreferredSize(new Dimension(190, 20));
      passField.setBackground(GUI_Main.fieldAndButtonColor);

      // Skapar border och använder för loginpanel
      Border border = BorderFactory.createTitledBorder("Inloggning");
      loginPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

      // Lägger in fält och knappar i paneler
      userPanel.add(userField);
      passPanel.add(passField);
      buttonPanel.add(Box.createRigidArea(new Dimension(30,0)));
      buttonPanel.add(loginButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
      buttonPanel.add(forgotButton);

      // Lägger in paneler i loginpanel
      loginPanel.add(userPanel);
      loginPanel.add(Box.createVerticalStrut(10));
      loginPanel.add(passPanel);
      loginPanel.add(Box.createVerticalStrut(10));
      loginPanel.add(buttonPanel);

      // Knappval vid enter
      getRootPane().setDefaultButton(loginButton);

      // Lägger in loginpanel i huvudpanel
      mainPanel.add(loginPanel);
   }

   /**
    * Skapar lyssnare för inloggningskomponenter
    */
   private void buildListeners()
   {
      // Lyssnare för knappar
      loginButton.addActionListener(new LoginButtonListener());
      forgotButton.addActionListener(new LoginButtonListener());

      // Anonym inre klass för, muslyssnare för användarfält
      userField.addMouseListener(new MouseAdapter()
      {
         boolean userFieldPressed;

         @Override
         public void mouseEntered(MouseEvent e)
         {
            // Ökar storlek på nedre border
            userField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
         }

         @Override
         public void mouseExited(MouseEvent e)
         {
            // Normal storlek på nedre border
            userField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
         }

         @Override
         public void mousePressed(MouseEvent e)
         {
            // Om användarfält ej tryckts på innan
            if (!userFieldPressed)
            {
               userField.setText("");
               userFieldPressed = true;
            }
         }
      });

      // Anonym inre klass för, fokuslyssnare för lösenordsfält
      passField.addFocusListener(new FocusAdapter()
      {
         boolean passFieldFocused;

         @Override
         public void focusGained(FocusEvent e)
         {
            // Om lösenordsfält ej varit i fokus innan
            if (!passFieldFocused)
            {
               passFieldFocused = true;
               passField.setText("");
            }
         }
      });

      // Anonym inre klass för, muslyssnare för lösenordsfält
      passField.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseEntered(MouseEvent e)
         {
            // Ökar storlek på nedre border
            passField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
         }

         public void mouseExited(MouseEvent e)
         {
            // Normal storlek på nedre border
            passField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
         }
      });

   }
   /**
    * Sköter inloggning, om rätt användarnamn och lösenord angetts
    */
   private void loginPressed()
   {
      char[] password = passField.getPassword();
      char[] correctPassword = new char[]{'a','d','m','i','n'};

      // Om text i användarfält och lösenordsfält stämmer
      if (userField.getText().equals("admin") && Arrays.equals(password,correctPassword))
      {
         // Fyller correctPassword med nollor
         Arrays.fill(correctPassword,'0');

         // Skapar inloggningsmeddelande
         JLabel loginLabel = new JLabel("Du loggas in . . .");
         loginLabel.setHorizontalAlignment(SwingConstants.CENTER); // centrerar text i messagebox

         // Visar inloggningsmeddelande
         JOptionPane.showMessageDialog(loginPanel,loginLabel, "Välkommen",JOptionPane.PLAIN_MESSAGE);
         setVisible(false);

         // Skapar nytt GUI-objekt
         GUI_Main gui = new GUI_Main();
         gui.setVisible(true);
      }
      else
      {
         // Rensar fält och visar varningsmeddelande
         userField.setText("");
         passField.setText("");
         JOptionPane.showMessageDialog(loginPanel,"Felaktigt användarnamn eller lösenord", "", JOptionPane.WARNING_MESSAGE);
      }
   } // end (userField.getText().equals("admin") && Arrays.equals(password,correctPassword))

   /**
    * Visar meddelanderuta med rätt användarnamn och lösenord
    */
   private void forgotPassword()
   {
      JOptionPane.showMessageDialog(loginPanel,"Användarnamn: admin\nLösenord: admin", "",JOptionPane.PLAIN_MESSAGE);
   }

   /**
    * Sätter look-and-feel till Nimbus om den existerar, annars default
    */
   private static void setNimbusLaf()
   {
      try
      {
         // Loopar igenom alla look-and-feel
         for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
         {
            // Om nimbus finns sätts laf till nimbus
            if ("Nimbus".equals(info.getName()))
            {
               // UIManager för nimbus
               UIManager.setLookAndFeel(info.getClassName());
               UIManager.put("control", GUI_Main.bgColor);
               UIManager.put("nimbusBlueGrey", GUI_Main.fieldAndButtonColor);
               UIManager.getLookAndFeelDefaults().put("Button[Default].backgroundPainter", GUI_Main.fieldAndButtonColor);
               UIManager.put("TextField.background", GUI_Main.fieldAndButtonColor);
               UIManager.put("List.background", GUI_Main.bgColor);
               UIManager.put("nimbusLightBackground", GUI_Main.fieldAndButtonColor);
               break;
            }
         } // end for-loop
         // Om nimbus inte finns i systemet
      } catch (Exception e)
      {
         // UIManager för default
         UIManager.put("background", GUI_Main.bgColor);
         UIManager.put("OptionPane.background", GUI_Main.bgColor);
         UIManager.put("Panel.background", GUI_Main.bgColor);
         UIManager.put("List.background", GUI_Main.bgColor);
         UIManager.put("TextField.background", GUI_Main.fieldAndButtonColor);
         UIManager.put("Button.background", GUI_Main.fieldAndButtonColor);
         UIManager.put("TableHeader.background", GUI_Main.fieldAndButtonColor);
         UIManager.put("Table.background", GUI_Main.fieldAndButtonColor);
         UIManager.put("MenuBar.background", GUI_Main.fieldAndButtonColor);
         UIManager.put("ScrollPane.background", GUI_Main.bgColor);
      }
      // UIManager för Nimbus och default
      UIManager.put("FileChooser.saveButtonText","Spara fil");
      UIManager.put("FileChooser.openButtonText", "Öppna fil");
      UIManager.put("FileChooser.cancelButtonText","Avbryt");
   }
}
