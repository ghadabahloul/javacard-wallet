package monpackage;


import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.*;
import javacardx.framework.*;


public class MonApplet extends Applet 
{
// code de  CLA byte dans la  commande APDU header
final static byte MonApplet_CLA =(byte)0xB0;
//codes de INS byte dans la command APDU header
final static byte CREDIT = (byte) 0x30;
final static byte DEBIT = (byte) 0x40;
final static byte GET_BALANCE = (byte) 0x50;
final static byte INS_RESET=(byte) 0x60;
// maximum balance(solde)
final static short MAX_BALANCE = 0x7FFF;
// maximum transaction amount
final static byte  MAX_TRANSACTION_AMOUNT = 100; 
// les cas de  transaction amount invalid
// amount > MAX_TRANSACTION_MAOUNT ou amount < 0
final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;
 
// balance maximum
final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
 
// signal the balance becomes negative
final static short SW_NEGATIVE_BALANCE = 0x6A85;
/* instance variables declaration */
short balance;
private MonApplet (byte[] bArray, short bOffset, byte bLength){
	   
    register();
  } 
public static void install(byte[] bArray, short bOffset, byte bLength) {
    // create  Monapplet instance
    new MonApplet(bArray, bOffset, bLength);
  }
public void process(APDU apdu) {
   
    // les  bytes dans  APDU buffer
    byte[] buffer = apdu.getBuffer();
    if(this.selectingApplet())
    	return ;
    
    if (buffer[ISO7816.OFFSET_CLA] != MonApplet_CLA)
      ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    switch (buffer[ISO7816.OFFSET_INS]) 
    {
	    case GET_BALANCE: getBalance(apdu);
	      return;
	    case DEBIT: debit(apdu);
	      return; 
	    case CREDIT: 
	    	credit(apdu);
	      return;
	    case INS_RESET:
	    	byte[] buffer1 = apdu.getBuffer();
	        balance = buffer1[ISO7816.OFFSET_CDATA];
			return;
	   
    default: ISOException.throwIt (ISO7816.SW_INS_NOT_SUPPORTED);
  }
} // fin methode process
private void credit(APDU apdu) {
	byte[] buffer = apdu.getBuffer();
    byte creditAmount = buffer[ISO7816.OFFSET_CDATA];
   
    if ( ( creditAmount > MAX_TRANSACTION_AMOUNT) || ( creditAmount < 0 ) )
      ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
   
    if ( (short)( balance + creditAmount) < 0 ) 
    	ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);
   
    balance +=  creditAmount;
  
  } 
private void debit(APDU apdu) {
	   
	byte[] buffer = apdu.getBuffer();
   
    byte creditAmount = buffer[ISO7816.OFFSET_CDATA];
   
  
    if ( ( creditAmount > MAX_TRANSACTION_AMOUNT) || ( creditAmount < 0 ) )
      ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
   
    if ( (short)( balance - creditAmount) < 0 ) 
    	ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);
   
    balance -=  creditAmount;
    } 
private void getBalance(APDU apdu) 
{
    byte[] buffer = apdu.getBuffer();
    
    buffer[0] = (byte)(balance);
    buffer[1] = (byte)((balance>>8) & 0x00FF);
    apdu.setOutgoingAndSend((short) 0, (short) 2);
  
  } 
} // end of class MonApplet