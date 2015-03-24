
package monpackage;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;
public class MaClass extends JFrame{
	
	final static byte MonApplet_CLA =(byte)0xB0;
	public static final byte VERIFYPIN = 0x20;  
	final static byte CREDIT = (byte) 0x30;
	final static byte DEBIT = (byte) 0x40;
	final static byte GET_BALANCE = (byte) 0x50;
	final static byte INS_RESET=(byte) 0x60;
	
/* interface*/
	
	JSplitPane jsp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JTabbedPane jtp= new JTabbedPane();
    JTextField res=new JTextField();
    JButton bInt= new JButton("Interroger");
      JButton bCre= new JButton("Crediter");
      JButton bDeb= new JButton("Debiter");
      JButton bInitial= new JButton("Initiliser");
      JLabel pin=new JLabel("veuillez saisir votre code pin");
      JLabel ser=new JLabel("Application JavaCard");
      JTextField pi=new JTextField();
 
public MaClass() throws IOException, CadTransportException {
 
super();
setTitle("Carte Credit ");
setSize(800,500);
 
 
//création d'un panneau contenant les contrôles d'un onglet
 JPanel pgauche=new JPanel(new GridLayout(4,1));
 JPanel pdroite=new JPanel(new GridLayout(4,1));
 JPanel p=new JPanel(new GridLayout(2,1));
 JPanel ps=new JPanel();
 p.add(pin);
 p.add(pi);
 
//this.add(p);
 pgauche.add(bInt);
 pgauche.add(bCre);
 pgauche.add(bDeb);
 pgauche.add(bInitial);
//ajouter les composants au panel
pdroite.add(res);
jsp.setLeftComponent(pgauche);
jsp.setRightComponent(pdroite);
this.setLayout(new GridLayout(2,1));
this.add(jsp);
ps.add(ser);
this.add(ps);
this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
this.pack();
this.setVisible(true);
	/* Constantes */
byte[] donnees;
Scanner sc=new Scanner(System.in);
final CadT1Client cad;
Socket sckCarte;
try {
	sckCarte = new Socket("localhost", 9025);
	sckCarte.setTcpNoDelay(true);
	BufferedInputStream input = new BufferedInputStream(sckCarte.getInputStream());
	BufferedOutputStream output = new BufferedOutputStream(sckCarte.getOutputStream());
	cad = new CadT1Client(input, output);
} catch (Exception e) {
	System.out.println("Erreur : impossible de se connecter a la Javacard");
	return;
}		
/* Mise sous tension de la carte */
try {
	cad.powerUp();
} catch (Exception e) {
	System.out.println("Erreur lors de l'envoi de la commande Powerup a la Javacard");
	return;
}
/* Sélection de l'applet */
final Apdu apdu = new Apdu();
apdu.command[Apdu.CLA] = 0x00;
apdu.command[Apdu.INS] = (byte) 0xA4;
apdu.command[Apdu.P1] = 0x04;
apdu.command[Apdu.P2] = 0x00;
byte[] appletAID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00 };
apdu.setDataIn(appletAID);
cad.exchangeApdu(apdu);
if (apdu.getStatus() != 0x9000) {
	System.out.println("Erreur lors de la sélection de l'applet");
	System.exit(1);
}
apdu.command[Apdu.CLA] = MaClass.MonApplet_CLA;
apdu.command[Apdu.P1] = 0x00;
apdu.command[Apdu.P2] = 0x00;
/* Menu principal */
bInt.addActionListener(new ActionListener() {
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JOptionPane j=new JOptionPane();
		apdu.command[Apdu.INS] = MaClass.GET_BALANCE;
		try {
			cad.exchangeApdu(apdu);
		} catch (IOException | CadTransportException e) {
			
			e.printStackTrace();
		}
		if (apdu.getStatus() != 0x9000) {
			j.showMessageDialog(null,"Erreur : status word different de 0x9000","Message D'erreur",JOptionPane.ERROR_MESSAGE);
		} else {
			res.setText(""+apdu.dataOut[0]);
			j.showMessageDialog(null,"Votre solde est de "+""+apdu.dataOut[0]+ "  dinars", "Message informatif", JOptionPane.INFORMATION_MESSAGE);
		}
	}
});
bCre.addActionListener(new ActionListener() {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane j=new JOptionPane();
		byte[] donnees;
		Scanner sc=new Scanner(res.getText());
		donnees = new byte[1];
      	// System.out.println("Donner une montant :");
      	donnees[0] =sc.nextByte();
			apdu.command[Apdu.INS] = MaClass.CREDIT;
       	apdu.setDataIn(donnees);
			try {
				cad.exchangeApdu(apdu);
			} catch (IOException | CadTransportException e1) {
				
				e1.printStackTrace();
			}
			
			if (apdu.getStatus() != 0x9000) 
			{
				j.showMessageDialog(null,"Erreur : status word different de 0x9000","Message D'erreur",JOptionPane.ERROR_MESSAGE);
			} else {
				
				j.showMessageDialog(null,"Votre montant  de "+""+res.getText()+ " a était Créditer avec succées! ", "Message informatif", JOptionPane.INFORMATION_MESSAGE);
			
			}
		
	}
});
bDeb.addActionListener(new ActionListener() {
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JOptionPane j=new JOptionPane();
		byte[] donnees;
		Scanner sc=new Scanner(res.getText());
		donnees = new byte[1];
      	// System.out.println("Donner une montant :");
      	donnees[0] =sc.nextByte();
			apdu.command[Apdu.INS] = MaClass.DEBIT;
       	apdu.setDataIn(donnees);
			try {
				cad.exchangeApdu(apdu);
			} catch (IOException | CadTransportException e1) {
				
				e1.printStackTrace();
			}
			
			if (apdu.getStatus() != 0x9000) 
			{
				j.showMessageDialog(null,"Erreur : status word different de 0x9000","Message D'erreur",JOptionPane.ERROR_MESSAGE);
			} else {
				
				j.showMessageDialog(null,"Votre montant  de "+""+res.getText()+ " a était Débiter avec succées! ", "Message informatif", JOptionPane.INFORMATION_MESSAGE);
			
			}
		
	}
		
	
});
bInitial.addActionListener(new ActionListener() {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane j=new JOptionPane();
		byte[] donnees;
		Scanner sc=new Scanner(res.getText());
		donnees = new byte[1];
      	// System.out.println("Donner une montant :");
      	donnees[0] =0;
			apdu.command[Apdu.INS] = MaClass.INS_RESET;
       	apdu.setDataIn(donnees);
			try {
				cad.exchangeApdu(apdu);
			} catch (IOException | CadTransportException e1) {
				
				e1.printStackTrace();
			}
			
			if (apdu.getStatus() != 0x9000) 
			{
				j.showMessageDialog(null,"Erreur : status word different de 0x9000","Message D'erreur",JOptionPane.ERROR_MESSAGE);
			} else {
				
				j.showMessageDialog(null,"Votre montant  de "+""+res.getText()+ " a était initialisé avec succées! ", "Message informatif", JOptionPane.INFORMATION_MESSAGE);
			
			}
		
		
	}
});
boolean fin = false;
while (!fin) {
	System.out.println();
	System.out.println("Application cliente Javacard");
	System.out.println("-------------------------------");
	System.out.println();
	System.out.println("1 - Verifier code pin ");
	System.out.println("2 - Interroger votre solde bancaire ");
	System.out.println("3 - Enverser argent ");
	System.out.println("4 - Retirer argent ");
	System.out.println("5 - Reinitialiser votre solde bancaire");
	System.out.println("6 - Quitter");
	System.out.println();
	System.out.println("Votre choix ?");
	
	int choix = System.in.read();
	while (!(choix >= '1' && choix <= '6')) {
		choix = System.in.read();
	}
	
	//apdu = new Apdu();
	apdu.command[Apdu.CLA] = MaClass.MonApplet_CLA;
	apdu.command[Apdu.P1] = 0x00;
	apdu.command[Apdu.P2] = 0x00;
	switch (choix) 
	{
	
	
	case '1':
		apdu.command[Apdu.INS] =  MaClass.VERIFYPIN;
		cad.exchangeApdu(apdu);
		if (apdu.getStatus() != 0x9000) {
		System.out.println("Erreur : status word different de 0x9000");
		} 
		else {
			System.out.println("OK");
			 } 
		break;
		
		case '2':
			apdu.command[Apdu.INS] = MaClass.GET_BALANCE;
			cad.exchangeApdu(apdu);
			if (apdu.getStatus() != 0x9000) {
				System.out.println("Erreur : status word different de 0x9000");
			} else {
				System.out.println("Valeur de votre compte bancaire : " + apdu.dataOut[0]);
			}
			break;
			
		case '3':
			donnees = new byte[1];
       	 System.out.println("Donner une montant :");
       	donnees[0] =sc.nextByte();
			apdu.command[Apdu.INS] = MaClass.CREDIT;
        	apdu.setDataIn(donnees);
			cad.exchangeApdu(apdu);
			
			if (apdu.getStatus() != 0x9000) 
			{
				System.out.println("Erreur : status word different de 0x9000");
			} else {
				System.out.println("Donner une montant :");
				System.out.println("OK");
			}
			break;
			
		case '4':
			donnees = new byte[1];
	        	 System.out.println("Donner une montant :");
	        	donnees[0] =sc.nextByte();
				apdu.command[Apdu.INS] = MaClass.DEBIT;
	        	apdu.setDataIn(donnees);
				cad.exchangeApdu(apdu);
				
				if (apdu.getStatus() != 0x9000) 
				{
					System.out.println("Erreur : status word different de 0x9000");
				} else {
					System.out.println("Donner une montant :");
					System.out.println("OK");
			}
			break;
			
		case '5':
			apdu.command[Apdu.INS] = MaClass.GET_BALANCE;
			donnees = new byte[1];
        	donnees[0] = 5;
        	apdu.setDataIn(donnees);
			cad.exchangeApdu(apdu);
			if (apdu.getStatus() != 0x9000) {
				System.out.println("Erreur : status word different de 0x9000");
			} else {
				System.out.println("OK");
			}
			break;
			
		case '6':
			fin = true;
			break;
	}
}
/* Mise hors tension de la carte */
try {
	cad.powerDown();
} catch (Exception e) {
	System.out.println("Erreur lors de l'envoi de la commande Powerdown a la Javacard");
	return;
}		
}
	public static void main(String[] args) throws Exception {
		/* Connexion a la Javacard */
		new MaClass();
		
		
}}