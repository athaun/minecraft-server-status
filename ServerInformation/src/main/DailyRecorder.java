package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DailyRecorder {

	static JSONArray lastPlayerList = new JSONArray();
	static JSONArray playerList;
	static int currentlyOnline = 0;
	static int serverPlayerCap;
	static int lastOnline = 0;
	static int dailyMax = 0; // max number of players on at one time
	static int dailyTotal = 0; // total number of times players logged in
	static int totalDailyPlayers = 0; // total number of players who played on a given day

	static String date;
	static String tempDate;

	static String dailyMaxCSV = "c:/tmp/dailyMax.csv";
	static String dailyTotalCSV = "c:/tmp/dailyTotal.csv";
	static String totalDailyPlayersCSV = "c:/tmp/totalDailyPlayers.csv";

	public static void main(String[] args) {
		date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

		while (true) {
			tempDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

			getServerStats();
			calculateDailyMax();
			calculateDailyTotal();
			calculateTotalUsers();

			if (lastOnline != currentlyOnline) {
			String timeStamp = new SimpleDateFormat("[MM/dd - HH:mm:ss]").format(new Date());
			System.out.println(timeStamp + " Online: " + currentlyOnline + "/" + serverPlayerCap + " | Daily max: " + dailyMax
					+ " | Daily logins: " + dailyTotal + " | Total Daily Players: " + totalDailyPlayers);
			}
			sleep(4 * 1000);
		}
	}

	static void getServerStats() {
		try {
			// Get JSON from minetools.eu API
			URL url = new URL("https://api.minetools.eu/ping/mc.athaun.me/25565");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			int responsecode = conn.getResponseCode();

			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				// Scan text from API into JSON Object
				String jsonString = "";
				Scanner scanner = new Scanner(url.openStream());

				// Write all the JSON data into a string using a scanner
				while (scanner.hasNext()) {
					jsonString += scanner.nextLine();
				}
				scanner.close();

				// Parse the JSON Data

				lastOnline = currentlyOnline;
				lastPlayerList = playerList;

				JSONObject obj = new JSONObject(jsonString);
				JSONObject players = obj.getJSONObject("players");
				playerList = players.getJSONArray("sample");
				currentlyOnline = players.getInt("online");
				serverPlayerCap = players.getInt("max");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static int tempDailyMax;
	static ArrayList<Integer> playerCounts = new ArrayList<Integer>();

	static void calculateDailyMax() {
		if (date.equals(tempDate)) {
			if (currentlyOnline > dailyMax) {
				playerCounts.add(currentlyOnline);
				tempDailyMax = currentlyOnline;
			}
			dailyMax = getLargestNumber(playerCounts);
			if (dailyMax < 0) {
				dailyMax = 0;
			}
		} else {
			try {
				writeCSV(date + "," + dailyMax + "\n", dailyMaxCSV);
			} catch (Exception e) {
			}
			// reset everything for the next day
			tempDailyMax = 0;
			playerCounts = new ArrayList<Integer>();
			date = tempDate;
		}
	}

	static void calculateDailyTotal() {
		if (date.equals(tempDate)) {
			if (lastOnline < currentlyOnline) {
				dailyTotal += (currentlyOnline - lastOnline);
			}
		} else {
			try {
				writeCSV(date + "," + dailyTotal + "\n", dailyTotalCSV);
			} catch (Exception e) {
			}
			// reset everything for the next day
			dailyTotal = 0;
			lastOnline = 0;
			date = tempDate;
		}
	}

	static ArrayList<String> playedToday = new ArrayList<String>();

	static void calculateTotalUsers() {
		if (date.equals(tempDate)) {
			if (currentlyOnline > lastOnline) {
				try {
					for (int i = 0; i < playerList.length(); i++) {
						String current = playerList.getJSONObject(i).getString("name");
						if (!playedToday.contains(current)) {
							totalDailyPlayers++;
							playedToday.add(current);
						}					
					}
				} catch (JSONException e) {
					System.out.println("(ERROR) " + e.getStackTrace());
				}
			}
		} else {
			try {
				writeCSV(date + "," + totalDailyPlayers + "\n", totalDailyPlayersCSV);
			} catch (Exception e) {
			}
			// reset everything for the next day
			totalDailyPlayers = 0;
			lastOnline = 0;
			date = tempDate;
		}
	}

	static BufferedWriter out;

	static void writeCSV(String data, String path) throws IOException {
		try {
			out = new BufferedWriter(new FileWriter(path, true));
			try {
				out.write(data);
			} catch (IOException e) {
			}
		} finally {
			out.close();
		}
	}

	static int getLargestNumber(ArrayList<Integer> list) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			if (obj instanceof Integer && max < (Integer) obj)
				max = (Integer) obj;
		}
		return max;
	}

	static void sleep(int time) {
		try {
			// thread to sleep for 1000 milliseconds
			Thread.sleep(time);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
