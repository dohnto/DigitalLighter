package com.example.digitallighterserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Connection {

	private Handler mUpdateHandler;
	private ChatServer mChatServer;
	private ChatClient mChatClient;

	private static final String TAG = "Connection";

	private ArrayList<Socket> mSocket;
	private int mPort = -1;

	public Connection(Handler handler) {
		mUpdateHandler = handler;
		mSocket = new ArrayList<Socket>();
		mChatServer = new ChatServer(handler);
	}

	public void tearDown() {
		mChatServer.tearDown();
		mChatClient.tearDown();
	}

	public void connectToServer() {
		mChatClient = new ChatClient();
	}

	public void sendMessage(String msg) {
		if (mChatClient != null) {
			mChatClient.broadcast(msg);
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

		if (local) {
			msg = "me: " + msg;
		} else {
			msg = "them: " + msg;
		}

		Bundle messageBundle = new Bundle();
		messageBundle.putString("msg", msg);

		Message message = new Message();
		message.setData(messageBundle);
		mUpdateHandler.sendMessage(message);

	}

	private synchronized void setSocket(Socket socket) {
		Log.d(TAG, "setSocket being called.");
		if (socket == null) {
			Log.d(TAG, "Setting a null socket.");
		}
		if (mSocket != null) {
			mSocket.add(socket);
		}

	}

	private ArrayList<Socket> getSockets() {
		return mSocket;
	}

	private class ChatServer {
		ServerSocket mServerSocket = null;
		Thread mThread = null;

		public ChatServer(Handler handler) {
			mThread = new Thread(new ServerThread());
			mThread.start();
		}

		public void tearDown() {
			mThread.interrupt();
			try {
				mServerSocket.close();
			} catch (IOException ioe) {
				Log.e(TAG, "Error when closing server socket.");
			}
		}

		class ServerThread implements Runnable {

			@Override
			public void run() {

				try {
					// Since discovery will happen via Nsd, we don't need to
					// care which port is
					// used. Just grab an available one and advertise it via
					// Nsd.
					mServerSocket = new ServerSocket(0);
					setLocalPort(mServerSocket.getLocalPort());

					while (!Thread.currentThread().isInterrupted()) {
						Log.d(TAG, "ServerSocket Created, awaiting connection");
						setSocket(mServerSocket.accept());

						Bundle messageBundle = new Bundle();
						messageBundle.putInt(Protocol.MESSAGE_TYPE, Protocol.MESSAGE_TYPE_USER_ADDED);
						Message message = new Message();
						message.setData(messageBundle);
						mUpdateHandler.sendMessage(message);

						Log.d(TAG, "Connected.");
						if (mChatClient == null) {
							connectToServer();
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "Error creating ServerSocket: ", e);
					e.printStackTrace();
				}
			}
		}
	}

	private class ChatClient {

		private final String CLIENT_TAG = "ChatClient-SeverSide";

		private Thread mSendThread;
		private Thread mRecThread;

		public ChatClient() {

			Log.d(CLIENT_TAG, "Creating chatClient");

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
				while (true) {
					try {
						String msg = mMessageQueue.take();
						broadcast(msg);
					} catch (InterruptedException ie) {
						Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
					}
				}
			}
		}

		public void tearDown() {
			try {
				for (Socket socket : getSockets()) {
					socket.close();
				}
			} catch (IOException ioe) {
				Log.e(CLIENT_TAG, "Error when closing server socket.");
			}
		}

		public void unicast(Socket receiver, String msg) {
			try {
				if (receiver == null) {
					Log.d(CLIENT_TAG, "Socket is null, wtf?");
				} else if (receiver.getOutputStream() == null) {
					Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
				} else {
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
							receiver.getOutputStream())), true);
					out.println(msg);
					out.flush();
					updateMessages(msg, true);
				}
			} catch (UnknownHostException e) {
				Log.d(CLIENT_TAG, "Unknown Host", e);
			} catch (IOException e) {
				Log.d(CLIENT_TAG, "I/O Exception", e);
			} catch (Exception e) {
				Log.d(CLIENT_TAG, "Error3", e);
			}
			Log.d(CLIENT_TAG, "Client sent message: " + msg);
		}

		public void broadcast(String msg) {
			ArrayList<Socket> sockets = getSockets();
			for (Socket socket : sockets) {
				unicast(socket, msg);
			}
		}
	}
}