package com.silentducks.digitallighter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Connection {

	private Handler mUpdateHandler;
	private static ChatClient mChatClient;

	private static final String TAG = "Connection";

	private Socket mSocket;
	private int mPort = -1;

	public Connection(Handler handler) {
		mUpdateHandler = handler;
		mChatClient = null;
		mSocket = null;
		mPort = -1;
	}

	public void tearDown() {
		mChatClient.tearDown();
	}

	public void connectToServer(InetAddress address, int port) {

		if (mChatClient != null && mChatClient.PORT == port && mChatClient.mAddress.equals(address))
			return;

		if (mChatClient != null) {
			mChatClient.tearDown();
		}

		if (mChatClient == null)
			mChatClient = new ChatClient(address, port);

	}

	public void sendMessage(String msg) {
		if (mChatClient != null) {
			mChatClient.sendMessage(msg);
		}
	}

	public int getLocalPort() {
		return mPort;
	}

	public void setLocalPort(int port) {
		mPort = port;
	}

	public synchronized void updateMessages(String msg, boolean local) {
		Log.e(TAG, "Updating message: " + msg);

		Message message = new Message();
		message.what = Protocol.MESSAGE_TYPE_COMMAND;

		Bundle messageBundle = new Bundle();
		messageBundle.putString(Protocol.COMMAND, msg);
		message.setData(messageBundle);

		mUpdateHandler.sendMessage(message);
	}

	private synchronized void setSocket(Socket socket) {
		Log.d(TAG, "setSocket being called.");
		if (socket == null) {
			Log.d(TAG, "Setting a null socket.");
		}
		if (mSocket != null) {
			if (mSocket.isConnected()) {
				try {
					mSocket.close();
				} catch (IOException e) {
					// TODO(alexlucas): Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		mSocket = socket;
	}

	private Socket getSocket() {
		return mSocket;
	}

	private class ChatClient {

		private InetAddress mAddress;
		private int PORT;

		private final String CLIENT_TAG = "ChatClient";

		private Thread mSendThread;
		private ReceivingThread mRecThread;

		public ChatClient(InetAddress address, int port) {

			Log.d(CLIENT_TAG, "Creating chatClient");
			this.mAddress = address;
			this.PORT = port;

			mSendThread = new Thread(new SendingThread());
			mSendThread.start();
		}

		class SendingThread implements Runnable {

			BlockingQueue<String> mMessageQueue;
			private int QUEUE_CAPACITY = 10;

			public SendingThread() {
				mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
			}

			@Override
			public void run() {
				try {
					if (getSocket() == null) {
						setSocket(new Socket(mAddress, PORT));
						Log.d(CLIENT_TAG, "Client-side socket initialized.");

						Message message = new Message();
						message.what = Protocol.MESSAGE_TYPE_SERVER_STARTED;
						mUpdateHandler.sendMessage(message);

					} else {
						Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
					}

					mRecThread = new ReceivingThread();
					mRecThread.start();

				} catch (UnknownHostException e) {
					Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
				} catch (IOException e) {
					Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
				}

				while (true) {
					try {
						String msg = mMessageQueue.take();
						sendMessage(msg);
					} catch (InterruptedException ie) {
						Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
					}
				}
			}
		}

		class ReceivingThread extends Thread {

			@Override
			public void run() {

				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					while (!Thread.currentThread().interrupted()) {

						String messageStr = null;
						if (input.ready()) {
							messageStr = input.readLine();
							if (messageStr != null) {
								Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
								updateMessages(messageStr, false);
							} else {
								Log.d(CLIENT_TAG, "The nulls! The nulls!");
								break;
							}
						}
					}
					input.close();
					getSocket().close();
				} catch (IOException e) {
					Log.e(CLIENT_TAG, "Server loop error: ", e);

				}
			}
		}

		public void tearDown() {
			mRecThread.interrupt();
			mSendThread.interrupt();

		}

		public void sendMessage(String msg) {
			try {
				Socket socket = getSocket();
				if (socket == null) {
					Log.d(CLIENT_TAG, "Socket is null, wtf?");
				} else if (socket.getOutputStream() == null) {
					Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
				}

				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getSocket()
						.getOutputStream())), true);
				out.println(msg);
				out.flush();
				updateMessages(msg, true);
			} catch (UnknownHostException e) {
				Log.d(CLIENT_TAG, "Unknown Host", e);
			} catch (IOException e) {
				Log.d(CLIENT_TAG, "I/O Exception", e);
			} catch (Exception e) {
				Log.d(CLIENT_TAG, "Error3", e);
			}
			Log.d(CLIENT_TAG, "Client sent message: " + msg);
		}
	}
}
