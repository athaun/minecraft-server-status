package main;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

	static String savePath = "/home/asher/activityRecord/record.csv";

	static int onlinePlayers;
	static int dailyMax = 0;

	private static BufferedWriter out;

	static ArrayList<Integer> players = new ArrayList<Integer>();

	static boolean running = true;

	public static void main(String[] args) throws IOException {

		players.add(0);

		String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

		int currentDailyHigh = 0;

		while (running) {
			getPlayers();
			String currentDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
			if (date.equals(currentDate)) {
				// Check if it is still the same day
				if (onlinePlayers > currentDailyHigh) {
					players.add(onlinePlayers);
					currentDailyHigh = onlinePlayers;
					System.out.println("Reached new daily high for " + date + ": " + currentDailyHigh);
				}
				dailyMax = getLargestNumber(players);
			} else {
				writeCSV(date + "," + dailyMax + "\n");
				// reset everything for the next day
				currentDailyHigh = 0;
				players = new ArrayList<Integer>();
				date = currentDate;
			}

			System.out.println("DailyMax: " + dailyMax + " | Current daily high: " + currentDailyHigh + " | Online: " + onlinePlayers);

			sleep(2000);
		}
	}

	private static int getLargestNumber(ArrayList<Integer> list) {
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

	static void writeCSV(String data) throws IOException {
		try {
			out = new BufferedWriter(new FileWriter(savePath, true));
			try {
				out.write(data);
			} catch (IOException e) {
			}
		} finally {
			out.close();
		}
	}

	private static Socket sock;

	static void getPlayers() {
		try {
			sock = new Socket("192.168.1.40", 25565);

			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			DataInputStream in = new DataInputStream(sock.getInputStream());

			out.write(0xFE);

			int b;
			StringBuffer str = new StringBuffer();
			while ((b = in.read()) != -1) {
				if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
					// Not sure what use the two characters are so I omit them
					str.append((char) b);
				}
			}
			
			System.out.println(str.toString());

			String[] data = str.toString().split("§");
			// String serverMotd = data[0];
			onlinePlayers = Integer.parseInt(data[1]);
			int maxPlayers = Integer.parseInt(data[2]);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
