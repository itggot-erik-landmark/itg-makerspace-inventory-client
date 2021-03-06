package itg.makerspace.authentication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import itg.makerspace.MainFrame;
import itg.makerspace.dialogs.InformationDialog;

public class NewLoanThread extends Thread {
	
	MainFrame mainFrame;
	int user_id = 0;
	String auth_key = "";
	String items = "";
	
	public NewLoanThread(MainFrame main, int u_id, String au_key, String things) {
		mainFrame = main;
		user_id = u_id;
		auth_key = au_key;
		items = things;
	}
	
	public void run() {
		try {
			String httpsURL = AuthenticationManager.IP_ADRESS + "/users/" + String.valueOf(user_id) + "/loans/new";
			String query = "security_key=" + URLEncoder.encode(auth_key,"UTF-8") + "&items=" + URLEncoder.encode(items,"UTF-8");
	
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
				JSONObject obj = new JSONObject(answer);
				String status = obj.getString("status");
				if(status.equalsIgnoreCase("true")) {
					mainFrame.newLoanSuccessfull();
				} else {
					InformationDialog dialog = new InformationDialog();
					dialog.open(obj.getString("status_msg"));
					mainFrame.newLoanFail();
				}
			} else {
				InformationDialog dialog = new InformationDialog();
				dialog.open("Oops! Ett fel uppstod. Felkod: " + con.getResponseCode() + "\n" + "(" + con.getResponseMessage() + ")");
				mainFrame.newLoanFail();
			}
		} catch (ConnectException e) {
			System.out.println(e.getMessage());
			InformationDialog dialog = new InformationDialog();
			dialog.open("Servern kunde inte hittas!\n404: Not Found.");
			mainFrame.newLoanFail();
		} catch (SocketTimeoutException e) {
			System.out.println(e.getMessage());
			InformationDialog dialog = new InformationDialog();
			dialog.open("Servern kunde inte hittas!\n408: Timeout.");
			mainFrame.newLoanFail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			InformationDialog dialog = new InformationDialog();
			dialog.open("Fel uppstod:\n" + e.getLocalizedMessage());
			mainFrame.newLoanFail();
		}
	}
}
