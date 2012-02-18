package com.kapin.diceroller;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/*
 * This program is an Android application that functions as a dice roller. When the user clicks a button or shakes his or her device it will "roll the dice".
 */

public class DiceRollerActivity extends Activity implements SensorEventListener{
	
	/* Global Variables
	 ***********************************************************************************
	 * private final int DEFAULT_DICE_SELECTION: The default index for the diceSpinner
	
	 * private TextView mRollResult: Displays the the sum of all the current dice rolls
	 * private TextView mLine: Displays the individual dice rolls
	 * private TextView mTotal: Displays the header for mRollResult
	 * private TextView mResult: Displays the header for mLine
	 * private Button mButton: Button that when clicked rolls the dice for the app
	 * private SensorManager mSensorManager: Manages the detection of the accelerometer
	 * private Sensor mAccelerometer: Detects when the phone is shook for dice rolls
	 * private float mAccel: Acceleration of the device minus gravity
	 * private float mAccelCurrent: Current Acceleration of the device with gravy
	 * private float mAccelLast: Last detected acceleration with gravity
	 * private int: mNumOfDice: Number of dice the user has selected to roll
	 * private int: mNumOfSides: Number of sides for the dice that the user wants to roll
	 * private long lastUpdate: Tracks when the accelerometer was last updated
	 * private MediaPlayer mp: Plays a dice rolling sound effect when dice are rolled
	 */
	
	private final int DEFAULT_DICE_SELECTION = 2;
	
	private TextView mRollResult,mLine, mTotal, mResult; 
	private Button mButton;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;	
	private int mNumOfDice; 
	private int mNumOfSides;
	private long lastUpdate = -1;
	private MediaPlayer mp;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mNumOfDice=0;
        mNumOfSides=0;
        
        mp = MediaPlayer.create(this,R.raw.dice);
        
        prepNumSpinner();
        prepDiceSpinner();
        prepTextViews();
        prepAccelerometer();
        prepButton();     
    }
	
	/*
	 * Instantiates and prepares the functions of the mButton
	 */
	private void prepButton(){
		mButton = (Button)findViewById(R.id.button1);
		mButton.setOnClickListener(new View.OnClickListener() { 
			
			public void onClick(View v) { //when the button is pressed the dice are rolled
				rollDice();				
			}
		});
	}
	
	/*
	 * Instantiates the accelerometer
	 */
	private void prepAccelerometer(){
		 mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mAccel = 0.00f;
			mAccelCurrent = SensorManager.GRAVITY_EARTH;
			mAccelLast = mAccelCurrent;
	}
	
	/*
	 * Instantiates the TextViews for displaying text on screen.
	 */
	private void prepTextViews(){
		mRollResult = (TextView)this.findViewById(R.id.result);
        mLine=(TextView)this.findViewById(R.id.line1);
        mTotal=(TextView)this.findViewById(R.id.total);
        mResult=(TextView)this.findViewById(R.id.dice_result);
	}
	
	/*
	 * Instantiates the spinner that lets the user select the number of dice
	 */
	private void prepNumSpinner(){
		Spinner numberSpinner = (Spinner)findViewById(R.id.numberSpinner);
        ArrayAdapter<CharSequence> numberAdapter = ArrayAdapter.createFromResource(
        		this, R.array.numbers_array,android.R.layout.simple_spinner_item);//loads the spinner options from Strings.xml file
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        numberSpinner.setAdapter(numberAdapter);
        numberSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
	
	/*
	 * Instantiates the spinner that lets the user select the number of sides the dice has 
	 */
	private void prepDiceSpinner(){
		Spinner diceSpinner = (Spinner)findViewById(R.id.diceSpinner);
        ArrayAdapter<CharSequence> diceAdapter = ArrayAdapter.createFromResource(
        		this, R.array.dice_array,android.R.layout.simple_spinner_item); //loads the spinner options from Strings.xml
        diceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diceSpinner.setAdapter(diceAdapter);
        diceSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener1());
        diceSpinner.setSelection(DEFAULT_DICE_SELECTION);
	}
	
	/*
	 * Processes the dice roll and displays the result on screen
	 */
	private void rollDice()
	{
		int diceTotal=0;
		String diceRolls="";
		for(int i = 0;i<mNumOfDice;++i){
			int temp = diceRoll();
			diceTotal+=temp;
			diceRolls+=(temp+"");
			if(i<mNumOfDice-1)
			{
				diceRolls+=" ";
			}
		}
		mLine.setText(diceRolls); // sets the text of mLine to contain all the dice rolls in format (n1 n2 n3 n4...)
		mRollResult.setText(diceTotal+"");//sets the text of mRollResult to the sum of the individual dice rolls
		mTotal.setVisibility(0);
		mResult.setVisibility(0);		  
		mp.start(); //plays dice rolling sound effect
		
	}
	
	/*
	 * Override: Register the SensorManager when the application is resumed
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume(){
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
	/*
	 * Override: Unregister the SensorManager when paused
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause(){
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	/*
	 * Unregister the SensorManager when application is exited
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop(){
		super.onStop();
		mSensorManager.unregisterListener(this);
	}
	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy){
	}
	/*
	 * Returns a dice roll based off of mNumOfSides
	 */
	private int diceRoll(){
		return ((int)(Math.random()*mNumOfSides+1));
	}
	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	public void onSensorChanged(SensorEvent event){
		long curTime=System.currentTimeMillis();	
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float)Math.sqrt((double)(x*x+y*y+z*z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel*0.98f+delta; //low-cut filter
		if(mAccel>4.0 &&(curTime-lastUpdate)>400)
		{
			rollDice();
			lastUpdate=curTime;
		}
		
	}
	/*
	 * Listener that updates mNumOfDice when the user selects and item from the Number of Dice spinner
	 */
	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) throws NumberFormatException{
			mNumOfDice=Integer.parseInt(parent.getSelectedItem().toString());
			
		}
		
		public void onNothingSelected(AdapterView<?> parent){
			//Do nothing.
		}
}
	/*
	 * Listener that updates mNumOfSides when the user selects and item from the Number of Sides spinner
	 */
	public class MyOnItemSelectedListener1 implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) throws NumberFormatException{
			mNumOfSides=Integer.parseInt(parent.getSelectedItem().toString());
		}
		
		public void onNothingSelected(AdapterView<?> parent){
			//Do nothing.
		}
}
}