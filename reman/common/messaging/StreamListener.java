package reman.common.messaging;

import java.util.*;
import java.io.*;

/**
 * Not used.
 * @author Will
 *
 */
public class StreamListener extends Thread {
	InputStream is;
	String type;

	StreamListener(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(type + ">" + line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
