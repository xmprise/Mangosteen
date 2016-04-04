package com.mangosteen.mangosteen_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class Relayhandler implements Runnable {

	private int PORT, myport = 55556, mjpegport = 8555;
	private String host = "210.118.69.49";
	private Socket kernersoc, Websoc;
	private String me = "127.0.0.1";

	public Relayhandler(int port) {
		PORT = port;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			kernersoc = new Socket(host, PORT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Log.e("kernersoc connect", "success : " + PORT);
		InputStream kernerin = null, webin = null;
		OutputStream kernerout = null, webout = null;
		try {
			kernerin = kernersoc.getInputStream();
			kernerout = kernersoc.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		byte[] buffer = new byte[100000];
		int size = 0;
		String requeststr, responestr;
		while (true) {
			try {
				size = kernerin.read(buffer);
				requeststr = new String(buffer, 0, size, "utf-8");
				if((requeststr.contains("Mangosteen/8555")
						|| requeststr.contains("/shot.jpg")
						|| requeststr.contains("/mjpeg.") && Websoc == null))
				{
					Log.e("Relayhandler", "mjpeg¡¢º”");
					Websoc = new Socket(me, mjpegport);
					Websoc.setSoTimeout(1500);
					webin = Websoc.getInputStream();
					webout = Websoc.getOutputStream();
					if(!requeststr.contains("MSIE"))
					{
						webout.write(buffer, 0, size);
						while(true)
						{
							try
							{
							size = webin.read(buffer);
							kernerout.write(buffer, 0, size);
							}
							catch(Exception e1)
							{
								break;
							}
						}
						break;
					}
//					webout.write(buffer, 0, size);
//
//					size = webin.read(buffer);
//					responestr = new String(buffer, 0, size, "utf-8");
//					kernerout.write(buffer, 0, size);
//					Log.e("requeststr", requeststr);
//					int sendsize = size;
//					if (responestr.contains("Content-Length")) {
//						String[] Splitstr = responestr.split("\n");
//						String Lengthstr = null;
//						for (int i = 0; i < Splitstr.length; i++) {
//							if (Splitstr[i].contains("Content-Length")) {
//								Lengthstr = Splitstr[i].split(" ")[1].replace("\r",
//										"");
//								break;
//							}
//						}
//						int ContentLength = Integer.parseInt(Lengthstr);
//						while (sendsize < ContentLength) {
//							try {
//								// Log.e("whileinlength", "in,  size : " + size +
//								// "   sendsize : " + sendsize);
//								size = webin.read(buffer);
//								kernerout.write(buffer, 0, size);
//								sendsize += size;
//							} catch (Exception e1) {
//								Log.e("whileinlength", "out");
//								break;
//							}
//						}
//					}
//					Log.e("respone", "" + sendsize);
//					break;
				}
				if(requeststr.contains("/shot.jpg"))
				{
					webout.write(buffer, 0, size);
					while(true)
					{
						try {
							size = webin.read(buffer);
							responestr = new String(buffer, 0, size, "utf-8");
							kernerout.write(buffer, 0, size);
							if (responestr.contains("Content-Length")) {
								String[] Splitstr = responestr.split("\n");
								String Lengthstr = null;
								for (int i = 0; i < Splitstr.length; i++) {
									if (Splitstr[i].contains("Content-Length")) {
										Lengthstr = Splitstr[i].split(" ")[1]
												.replace("\r", "");
										break;
									}
								}
								int ContentLength = Integer.parseInt(Lengthstr);
								int sendsize = size;
								while (sendsize < ContentLength) {
									try {
										// Log.e("whileinlength", "in,  size : "
										// + size +
										// "   sendsize : " + sendsize);
										size = webin.read(buffer);
										kernerout.write(buffer, 0, size);
										sendsize += size;
									} catch (Exception e1) {
										Log.e("whileinlength", "out");
										break;
									}
								}
							}
						} catch (Exception e1) {
							break;
						}
					}
					break;
				}
				if (Websoc == null) {
					Websoc = new Socket(me, myport);
					Websoc.setSoTimeout(1500);
					webin = Websoc.getInputStream();
					webout = Websoc.getOutputStream();
				}
				webout.write(buffer, 0, size);

				size = webin.read(buffer);
				responestr = new String(buffer, 0, size, "utf-8");
				kernerout.write(buffer, 0, size);
				if (responestr.contains("Content-Length")) {
					String[] Splitstr = responestr.split("\n");
					String Lengthstr = null;
					for (int i = 0; i < Splitstr.length; i++) {
						if (Splitstr[i].contains("Content-Length")) {
							Lengthstr = Splitstr[i].split(" ")[1].replace("\r",
									"");
							break;
						}
					}
					int ContentLength = Integer.parseInt(Lengthstr);
					int sendsize = size;
					while (sendsize < ContentLength) {
						try {
							// Log.e("whileinlength", "in,  size : " + size +
							// "   sendsize : " + sendsize);
							size = webin.read(buffer);
							kernerout.write(buffer, 0, size);
							sendsize += size;
						} catch (Exception e1) {
							Log.e("whileinlength", "out");
							break;
						}
					}
				} else if (responestr.contains("chunked")) {
					while (!responestr.contains("\r\n0\r\n")) {
						try {
							// Log.e("whileinchunked", "in,  size : " + size);
							size = webin.read(buffer);
							kernerout.write(buffer, 0, size);
							responestr = new String(buffer, 0, size, "utf-8");
						} catch (Exception e1) {
							Log.e("whileinchunked", "out");
							break;
						}
					}
					// relayoutput.write("#STOP#".getBytes());
					// Log.e("relayoutput", "stop ∫∏≥ø");
				}
			} catch (Exception e) {
				break;
			}
		}
		stop();
	}
	
	public void stop()
	{
		try {
			if(kernersoc !=null)
				kernersoc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if(Websoc !=null)
				Websoc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
