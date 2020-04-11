package me.infinityz.players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ConnectionExample {

	private static final String USER_AGENT = "Mozilla/5.0";

	private static final String GET_URL = "https://playerdb.co/api/player/minecraft/";

	public static String sendGET(UUID uuid) throws IOException {
		URL obj = new URL(GET_URL + uuid.toString());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

            // print result
            return response.toString();
		} else {
			System.out.println("GET request not worked");
            return null;
		}

	}

}