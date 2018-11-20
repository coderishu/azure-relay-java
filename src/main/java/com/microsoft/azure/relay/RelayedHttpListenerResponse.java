package com.microsoft.azure.relay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.RuntimeIOException;

public class RelayedHttpListenerResponse {
    private boolean disposed;
    private int statusCode;
    private String statusDescription;
    private Map<String, String> headers;
    private OutputStream outputStream;
    private RelayedHttpListenerContext context;

    public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statuscode) {
		this.checkClosed();
		if (statuscode < 100 || statuscode > 999) {
			throw new IllegalArgumentException("http status code must be between 100 and 999.");
		}
		this.statusCode = statuscode;
	}
	public String getStatusDescription() {
		if (this.statusDescription == null) {
			this.statusDescription = (HttpStatus.getMessage(this.statusCode) != null) ? 
					(HttpStatus.getMessage(this.statusCode)) : null;
		}
		return statusDescription;
	}
	public void setStatusDescription(String statusDescription) {
		this.checkClosed();
		if (statusDescription == null) {
			throw new IllegalArgumentException("cannot set a null statusDecription.");
		}
		
        // Need to verify the status description doesn't contain any control characters except HT.
        for (int i = 0; i < statusDescription.length(); i++) {
            char c = statusDescription.charAt(i);
            if ((c <= 31 && c != '\t') || c >= 127) {
                throw new IllegalArgumentException("status description should not contain any control characters.");
            }
        }
		this.statusDescription = statusDescription;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	public RelayedHttpListenerContext getContext() {
		return context;
	}
	
	protected RelayedHttpListenerResponse(RelayedHttpListenerContext context) {
        this.context = context;
        this.statusCode = HttpStatus.CONTINUE_100;
        this.headers = new HashMap<String, String>();
        this.outputStream = null;
    }

    /// <summary>Sends the response to the client and releases the resources held by this <see cref="RelayedHttpListenerResponse"/> instance.</summary>
    public void close() {
    	try {
			this.outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.disposed = true;
    }
    
    private void checkClosed() {
    	if (this.disposed) {
    		throw new RuntimeIOException("this connection has already been closed.");
    	}
    }
}
