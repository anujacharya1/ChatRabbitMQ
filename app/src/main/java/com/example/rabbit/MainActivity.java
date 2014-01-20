package com.example.rabbit;

import java.io.UnsupportedEncodingException;


//
//import com.example.rabbit.ChatAppActivity.send;
//import com.rabbitmq.client.AMQP.Connection;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MessageConsumer mConsumer;
	private TextView mOutput;
	private String QUEUE_NAME = "";
	private String EXCHANGE_NAME = "logs";
	private String message = "";
	private String name = "";
	private final String SERVER = "YOUR SERVER";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		Toast.makeText(MainActivity.this, "RabbitMQ Chat Service!",
//				Toast.LENGTH_LONG).show();

		final EditText etv1 = (EditText) findViewById(R.id.out3);
	
		etv1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					name = etv1.getText().toString();
					etv1.setText("");
					etv1.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});

		final EditText etv = (EditText) findViewById(R.id.out2);
		etv.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					message = name + ": " + etv.getText().toString();
					new send().execute(message);
					etv.setText("");
					return true;
				}
				return false;
			}
		});

		// The output TextView we'll use to display messages
		mOutput = (TextView) findViewById(R.id.output);

		// Create the consumer
		mConsumer = new MessageConsumer(SERVER, "logs", "fanout");
		
		// Connect to broker
        new consumerconnect().execute();
//		mConsumer.connectToRabbitMQ();
		
		// register for messages

        Log.i(TAG,"After mConsumer.connectToRabbitMQ()");

		mConsumer.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {

			public void onReceiveMessage(byte[] message) {
				String text = "";
				try {
					text = new String(message, "UTF8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				mOutput.append("\n" + text);
				Log.i(TAG,"Text received "+ text);
			}
		});

	}

	private class send extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... Message) {
			try {

				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(SERVER);
				Connection connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
				
				//String queueName = channel.queueDeclare().getQueue();
//		       channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

                StringBuffer tempstr = new StringBuffer();

                for (String msg : Message){
                    tempstr.append(msg);
                }

//				channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null,
//						tempstr.getBytes());
				
				channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null,
                        tempstr.toString().getBytes());
				channel.close();
				connection.close();

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			return null;
		}
	}

    private class consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
                // Connect to broker
                mConsumer.connectToRabbitMQ();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }


	@Override
	protected void onResume() {
		super.onPause();
		mConsumer.connectToRabbitMQ();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mConsumer.dispose();
	}
}