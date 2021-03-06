package itg.makerspace.authentication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import itg.makerspace.MainFrame;
import itg.makerspace.dialogs.ErrorDialog;
import itg.makerspace.dialogs.InformationDialog;

public class ReturnItemThread extends Thread {
	
	int item_id;
	int quantity;
	int loan_id;
	MainFrame mainFrame;
	int d_row;
	String security_key;
	int user_id;
	
	public ReturnItemThread(MainFrame main, String security_key, int user_id, int l_id, int id, int qu, int row) {
		item_id = id;
		quantity = qu;
		loan_id = l_id;
		mainFrame = main;
		d_row = row;
		this.security_key = security_key;
		this.user_id = user_id;
	}
	
	public void run() {
		try {
			String httpsURL = AuthenticationManager.IP_ADRESS + "/users/" + String.valueOf(user_id) + "/loans/delete";
			
			String query = "loan_id=" + URLEncoder.encode(String.valueOf(loan_id),"UTF-8")
			+ "&" + "item_id=" + URLEncoder.encode(String.valueOf(item_id),"UTF-8")
			+ "&" + "quantity=" + URLEncoder.encode(String.valueOf(quantity),"UTF-8")
			+ "&" + "security_key=" + URLEncoder.encode(String.valueOf(security_key),"UTF-8")
			+ "&" + "origin=" + URLEncoder.encode("2","UTF-8");
	
			URL myurl = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
			con.setRequestMethod("POST");
			con.setConnectTimeout(5000);
			con.setRequestProperty("Content-length", String.valueOf(query.length())); 
			con.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			con.setDoOutput(true); 
			con.setDoInput(true); 
	
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  
			output.writeBytes(query);
			output.close();
			if(con.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8")); 
				String answer = in.readLine();
				in.close();
				JSONObject jsonObj = new JSONObject(answer);
				if(jsonObj.getString("status").equalsIgnoreCase("true")) {
					int old_quantity = (int) mainFrame.myLoansPanel.tableContent.getValueAt(d_row, 2);
					if (old_quantity > quantity) {
						mainFrame.myLoansPanel.tableContent.setValueAt(old_quantity - quantity, d_row, 2);
					} else {
						if (mainFrame.myLoansPanel.tableContent.getRowCount() > 1) {
							String date = (String) mainFrame.myLoansPanel.tableContent.getValueAt(d_row, 0);
							String date_below = (String) mainFrame.myLoansPanel.tableContent.getValueAt(d_row + 1, 0);
							if(!date.isEmpty() && date_below.isEmpty()) {
								mainFrame.myLoansPanel.tableContent.setValueAt(date, d_row + 1, 0);
							}
						}
						mainFrame.myLoansPanel.tableContent.removeRow(d_row);
					}
					mainFrame.myLoansPanel.updateTable();
				} else {
					InformationDialog dialog = new InformationDialog();
					dialog.open("Error with request!");
				}
			} else {
				ErrorDialog dialog = new ErrorDialog();
				dialog.open("Oops! Ett fel uppstod. Felkod: " + con.getResponseCode() + "\n" + "(" + con.getResponseMessage() + ")");
			}
		} catch (ConnectException e) {
			System.out.println(e.getMessage());
			ErrorDialog dialog = new ErrorDialog();
			dialog.open("Servern kunde inte hittas!\n404: Not Found.");
		} catch (SocketTimeoutException e) {
			System.out.println(e.getMessage());
			ErrorDialog dialog = new ErrorDialog();
			dialog.open("Servern kunde inte hittas!\n408: Timeout.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			ErrorDialog dialog = new ErrorDialog();
			dialog.open("Fel uppstod:\n" + e.getLocalizedMessage());
		}
    }
}
