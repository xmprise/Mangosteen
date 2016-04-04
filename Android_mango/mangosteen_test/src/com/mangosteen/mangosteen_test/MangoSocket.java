package com.mangosteen.mangosteen_test;

import io.socket.SocketIO;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

public class MangoSocket extends Thread {
    private SocketIO socket;
    private MangoSocketCallback callback;
    
    public MangoSocket(MangoSocketCallbackAdapter callback) {
        this.callback = new MangoSocketCallback(callback);
    }
    
    @Override
    public void run() {
        try {
			socket = new SocketIO("http://210.118.69.79:3000", callback);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
    public void sendMessage(String message) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("message", message);
            socket.emit("server open", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    public void login(String nickname) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("nick", nickname);
            socket.emit("server open", json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
