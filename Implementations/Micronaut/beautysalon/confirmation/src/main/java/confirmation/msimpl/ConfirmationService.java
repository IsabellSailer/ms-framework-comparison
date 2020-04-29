package beautysalon.confirmation.msimpl;

import javax.inject.Singleton;
import java.lang.*;
import java.io.*;
import javax.swing.JDialog;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;

@Singleton
public class ConfirmationService {

	public ConfirmationService() {
	}

	public void confirmAppointment(Appointment appointment) {
		JPanel pan=new JPanel();
		pan.setLayout(new FlowLayout());
		
        JDialog confirmationWindow = new JDialog();
        confirmationWindow.setTitle("Appointment Confirmation");
        confirmationWindow.setSize(400,100);
        
        JLabel label1 = new JLabel("Thank you " + appointment.getCustomerName() + "!");
		label1.setVerticalTextPosition(JLabel.TOP);
		label1.setHorizontalTextPosition(JLabel.CENTER);
		
		JLabel label2 = new JLabel("Your Appointment for " + appointment.getTreatmentName() + " was confirmed.");
		JLabel label3 = new JLabel("It will be on " + appointment.getDate().getDate() + "." + (appointment.getDate().getMonth() + 1) + "." + (1900 + appointment.getDate().getYear()));
		JLabel label4 = new JLabel("from " + appointment.getStartTime() + ":00 to " + appointment.getEndTime() + ":00.");
        
        pan.add(label1);
        pan.add(label2);
        pan.add(label3);
        pan.add(label4);
        confirmationWindow.add(pan);
        
        confirmationWindow.setModal(true);
        confirmationWindow.setVisible(true);

	}
}
