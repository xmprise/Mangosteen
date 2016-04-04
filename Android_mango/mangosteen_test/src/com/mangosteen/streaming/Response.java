package com.mangosteen.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import android.util.Log;

public class Response {

	private static final int BUFFER_SIZE = 1024;
	boolean socketshut;
	Request request;
	OutputStream output;
	private String Boundary = "--boundary";

	public Response(OutputStream output) {
		// TODO Auto-generated constructor stub
		this.output = output;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public void sendStaticResource(List<Socket> socket, Socket resource,
			boolean ex) throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis = null;
		try {
			if (ex) {
				Log.e("http Server", "ex");
				if (request.getUri().contains("/shot.jpg")) {
					socket.add(resource);
					socketshut = false;
				} else {
					String uri = request.getUri();
					Log.e("uri before", uri);
					if(uri.contains("/Mangosteen/8555"))
					{
						uri = uri.replace("/Mangosteen/8555", "");
					}
					if(uri.contains("/Mangosteen"))
					{
						uri = uri.replace("/Mangosteen", "");
					}
					if (uri.length() < 2) {
						uri = "mjpeg.js.html";
					}
					Log.e("uri after", uri);
					File file = new File(HttpServer.WEB_ROOT, uri);
					String Header = "HTTP/1.1 200 OK\r\n"
							+ "Content-Length: " + file.length() +"\r\n\r\n";
					socketshut = true;
					if (file.exists()) {
						output.write(Header.getBytes());
						output.flush();
						fis = new FileInputStream(file);
						int ch = fis.read(bytes, 0, BUFFER_SIZE);
						while (ch != -1) {
							output.write(bytes, 0, ch);
							ch = fis.read(bytes, 0, BUFFER_SIZE);
						}
					} else {
						String errorMessage = "HTTP/1.1 404 File Not Found\r\n"
								+ "Content-Type: text/html\r\n"
								+ "Content-Length: 23\r\n" + "\r\n"
								+ "<h1>File Not Found</h1>";
						output.write(errorMessage.getBytes());
					}
				}

			} else {
				Log.e("http Server", "other");
				String Header = "HTTP/1.1 200 OK\r\n"
						+ "Content-Type: multipart/x-mixed-replace; boundary="
						+ this.Boundary + "\r\n\r\n";
				output.write(Header.getBytes());
				output.flush();
				socket.add(resource);
			}
		} catch (Exception e) {
			// 파일 객체 생성시 문제가 발생했을 경우
			Log.e("http Server", e.toString());
		} finally {
			if (fis != null)
				fis.close();
			if (socketshut) {
				try {
					resource.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
