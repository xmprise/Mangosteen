package com.mangosteen.streaming;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

import android.util.Log;

public class HttpServer {

	public static final String WEB_ROOT = "/mnt/sdcard/jetty/webapps/Mangosteen/mjpeg";
	// 종료명령
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

	// 종료 명령이 받아졌는지의 여부
	private boolean shutdown = false;
	public boolean sockon = false;
	private int port;
	Thread acceptthread;

	// public static void main(String[] args){
	// HttpServer server = new HttpServer();
	// server.await();
	// }
	public HttpServer(int PORT) {
		port = PORT;

		this.await();

	}

	List<Socket> socket = new ArrayList<Socket>();
	List<Socket> exsocket = new ArrayList<Socket>();
	String Boundary = "--boundary";
	ServerSocket serverSocket;

	public void await() {
		serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException er) {
			System.exit(1);
		}
		sockon = true;
		// 요청을 기다리는 루프
		acceptthread = new Thread(new Runnable() {
			public void run() {
				while (sockon) {
					// socket = null;
					// output = null;
					try {

						Socket soc_acc = serverSocket.accept();

						// Log.e("HttpServer1111111",
						// soc_acc.getRemoteSocketAddress()+"");

						InputStream input = soc_acc.getInputStream();
						OutputStream output = soc_acc.getOutputStream();

						// Request 객체 생성 및 parse 호출
						Request request = new Request(input);
						int b = request.parse();

						// Response 객체 생성
						Response response = new Response(output);
						response.setRequest(request);
						if (b == 1)
							response.sendStaticResource(exsocket, soc_acc, true);
						else if(b == 0)
							response.sendStaticResource(socket, soc_acc, false);
						// socket.add(soc_acc);
						// 소켓 닫기
						// soc_acc.close();
						// URL이 종료 명령이었는를 확인
						// shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
						// if(shutdown)
						// break;
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

			}
		});
		acceptthread.start();
	}

	public void Stop() {
		sockon = false;
		if (acceptthread != null)
			acceptthread.interrupt();
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!socket.isEmpty()) {
			for (int i = 0; i < socket.size(); i++) {
				try {
					socket.get(i).close();
					socket.remove(i);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public void Send(byte[] stream) {
		if (!socket.isEmpty()) {
			{
				for (int i = 0; i < socket.size(); i++) {
					StringBuilder sb = new StringBuilder();
					int length = stream.length;
					sb.append("\r\n"
							+ this.Boundary
							+ "\r\nContent-Type: image/jpeg\r\nContent-Length: "
							+ Integer.toString(length) + "\r\n\r\n");
					OutputStream output = null;
					try {
						output = socket.get(i).getOutputStream();
						output.write(sb.toString().getBytes());
						output.write(stream);
						output.write("\r\n\r\n".toString().getBytes());
						output.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						try {
							output.close();
							socket.get(i).close();
							socket.remove(i);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						e.printStackTrace();
					}

				}
			}
		}
		if (!exsocket.isEmpty()) {
			List<String> sendIP = new ArrayList<String>();
			String tempIP;
			boolean Break;
			for (int i = 0; i < exsocket.size(); i++) {
				OutputStream output = null;
				Break = false;
				try {
//					tempIP = exsocket.get(i).getRemoteSocketAddress().toString().split(":")[0];
//					for (int j = 0; j < sendIP.size(); j++) {
//						if (sendIP.contains(tempIP)) {
//							Break = true;
//							break;
//						}
//					}
//					if (Break)
//						continue;
					String header = "HTTP/1.1 200 OK\r\n" +
							 "Content-Type: image/jpeg\r\n" + 
							"Content_Length:" + stream.length + "\r\n\r\n";
					output = exsocket.get(i).getOutputStream();
//					sendIP.add(tempIP);
					output.write(header.getBytes());
					output.flush();
					output.write(stream);
					output.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						output.close();
						exsocket.remove(i).close();
						continue;
					} catch (IOException e1) {
					}
				}finally
				{
//					exsocket.add(exsocket.remove(i));
				}
			}
		}
	}
}
