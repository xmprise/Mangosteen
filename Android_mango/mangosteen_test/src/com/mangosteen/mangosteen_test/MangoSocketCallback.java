package com.mangosteen.mangosteen_test;

import java.util.Arrays;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MangoSocketCallback implements IOCallback, IOAcknowledge {
    private MangoSocketCallbackAdapter callback;
    
    public MangoSocketCallback(MangoSocketCallbackAdapter callback) {
        this.callback = callback;
    }

	
	public void ack(Object... data) {
        try {
			callback.callback(new JSONArray(Arrays.asList(data)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    
    public void on(String event, IOAcknowledge ack, Object... data) {
        callback.on(event, (JSONObject) data[0]);
    }

   
    public void onMessage(String message, IOAcknowledge ack) {
        callback.onMessage(message);
    }

   
    public void onMessage(JSONObject json, IOAcknowledge ack) {
        callback.onMessage(json);
    }

   
    public void onConnect() {
        callback.onConnect();
    }

    
    public void onDisconnect() {
        callback.onDisconnect();
    }

	
	public void onError(SocketIOException socketIOException) {
		socketIOException.printStackTrace();
        callback.onConnectFailure();
	}

    
}
