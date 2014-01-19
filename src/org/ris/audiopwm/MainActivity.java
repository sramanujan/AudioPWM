package org.ris.audiopwm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
//import java.applet.Applet;
//import java.applet.AudioClip;

public class MainActivity extends Activity {
	
	ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    //TargetDataLine targetDataLine;
    //AudioInputStream audioInputStream;
    //SourceDataLine sourceDataLine;
    float frequency = 8000.0F;  //8000,11025,16000,22050,44100
    int samplesize = 16;
    private String myPath;
    //private long myChunkSize;
    // I made this public so that you can toss whatever you want in here
    // maybe a recorded buffer, maybe just whatever you want
    public byte[] myData;
    public byte[] chunkSize;
    public byte[] subChunkSize;
    public byte[] sampleRate;
    public byte[] byteRate;
    public byte[] dataSize;
    public byte[] format;
    public byte[] channels;
    public byte[] blockAlign;
    public byte[] bitsPerSample;
    
    
    public byte[] outData;
    public long outDataSize;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        chunkSize = new byte[4];
        subChunkSize = new byte[4];
        sampleRate = new byte[4];
        byteRate = new byte[4];
        dataSize = new byte[4];
        format = new byte[2];
        channels = new byte[2];
        blockAlign = new byte[2];
        bitsPerSample = new byte[2];
        outDataSize = 0;
        outData = null;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {        	
            public void onClick(View v) { // test button
            	//this is test for 5 seconds
            	//take x and y
            	resetData();
            	String X = ((EditText)findViewById(R.id.editText1)).getText().toString();
            	String Y = ((EditText)findViewById(R.id.editText2)).getText().toString();
            	addCoordinateForDuration(Float.parseFloat(X), Float.parseFloat(Y), 5);
            	
            	save();
            	
            	Toast.makeText(getApplicationContext(), "Wrote (" + X + "," + Y + ") data for " + 5 + "s. into out.wav on root folder!", Toast.LENGTH_LONG).show();
            }
        });
        
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
        	//this is play from file.
            public void onClick(View v) { // from file
            	String filename = ((EditText)findViewById(R.id.editText3)).getText().toString();
            	//open the txt file in /sdcard/filename
            	resetData();

            	File sdcard = Environment.getExternalStorageDirectory();
            	File file = new File(sdcard, filename);
            	
            	try {
            	    BufferedReader br = new BufferedReader(new FileReader(file));
            	    String line;
            	    String coords[] = new String[2];
            	    float x, y;
            	    while((line = br.readLine()) != null) {
            	    	coords = line.split(",");
            	    	x = Float.parseFloat(coords[0]);
            	    	y = Float.parseFloat(coords[1]);
            	    	addCoordinate(x,y);
            	    }
            	    br.close();
            	    save();
            	    Toast.makeText(getApplicationContext(), "Wrote entire file data from " + filename + " into out.wav on root folder!", Toast.LENGTH_LONG).show();
            	}
            	catch (IOException e) {
            	    Log.e("RADS", "Something bad happened");
            	}
            }
        });
    }
    
    public void resetData() {
    	outDataSize = 0;
        outData = null;
    }
    
    public void addCoordinateForDuration(float x, float y, float duration) {
    	//now split this into number of 30 ms intervals.
    	int intervals = (int)((duration*1000)/30);
    	for(int i = 0; i < intervals; i++) {
    		addCoordinate(x, y);
    	}
    }
    
    public void addCoordinate(float x, float y) {
    	long i;
    	
    	//make a copy of outData, now get a new string with length
    	byte[] data3 = outData;
    	outData = new byte[(int)outDataSize + 240];
    	for(i = 0; i < outDataSize; i ++) {
    		outData[(int)i] = data3[(int)i];
    	}
    	
    	//header - add 72 of full, and 8 of empty
    	for(i = 0; i < 72; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)128;
    	}
    	outDataSize += 72;
    	for(i = 0; i < 8; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)0;
    	}
    	outDataSize += 8;
    	
    	x = Math.max(0, Math.min(8, x));
    	//add x*8 of full, and remaining empty
    	for(i = 0; i < (int)x*8; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)128;
    	}
    	outDataSize += (int)x*8;
    	for(i = 0; i < 80 - (int)x*8; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)0;
    	}
    	outDataSize += 80 - (int)x*8;
    	
    	y = Math.max(0, Math.min(8, y));
    	//add y*8 of full, and remaining empty
    	for(i = 0; i < (int)y*8; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)128;
    	}
    	outDataSize += (int)y*8;
    	for(i = 0; i < 80 - (int)y*8; i++) {
    		outData[(int)outDataSize + (int)i] = (byte)0;
    	}
    	outDataSize += 80 - (int)y*8;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
/*
    public boolean read() {
        DataInputStream inFile = null;
        myData = null;
       
        try {
        	myPath = "/sdcard/test_mono_8000Hz_8bit_PCM.wav";
            inFile = new DataInputStream(new FileInputStream(myPath));

            System.out.println("Reading wav file...\n"); // for debugging only

            String chunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

            inFile.read(chunkSize); // read the ChunkSize

            String formatStr = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();
            // print what we've read so far
            System.out.println("chunkID:" + chunkID + " chunk1Size:" + bytesToLong(chunkSize) + " format:" + formatStr); // for debugging only

            String subChunk1ID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

            inFile.read(subChunkSize); // read the SubChunk1Size
            inFile.read(format); // read the audio format.  This should be 1 for PCM
            inFile.read(channels); // read the # of channels (1 or 2)
            inFile.read(sampleRate); // read the samplerate
            inFile.read(byteRate); // read the byterate
            inFile.read(blockAlign); // read the blockalign
            inFile.read(bitsPerSample); // read the bitspersample

            System.out.println("SubChunk1ID:" + subChunk1ID + " SubChunk1Size:" + bytesToLong(subChunkSize) + " AudioFormat:" + bytesToInt(format) + " Channels:" + bytesToInt(channels) + " SampleRate:" + bytesToLong(sampleRate));

            // read the data chunk header - reading this IS necessary, because not all wav files will have the data chunk here - for now, we're just assuming that the data chunk is here
            String dataChunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

            inFile.read(dataSize); // read the size of the data


            // read the data chunk
            myData = new byte[(int) bytesToLong(dataSize)];
            inFile.read(myData);

            // close the input stream
            inFile.close();
        } catch (Exception e) {
            return false;
        }

        return true; // this should probably be something more descriptive
    }
*/    
    // write out the wav file
    public boolean save() {
        try {
        	
            DataOutputStream outFile = new DataOutputStream(new FileOutputStream("/sdcard/out1.wav"));

            // write the wav file per the wav file format
            outFile.writeBytes("RIFF");                 // 00 - RIFF
            //36 + datasize
            //outFile.write(chunkSize, 0, 4);     // 04 - how big is the rest of this file?
            outFile.write(longToBytes(outDataSize + 36), 0, 4);     // 04 - how big is the rest of this file?
            outFile.writeBytes("WAVE");                 // 08 - WAVE
            outFile.writeBytes("fmt ");                 // 12 - fmt
            //16 subChunkSize
            //outFile.write(subChunkSize, 0, 4); // 16 - size of this chunk
            outFile.write(longToBytes(16), 0, 4); // 16 - size of this chunk
            //outFile.write(format, 0, 2);        // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            outFile.write(intToBytes(1), 0, 2);        // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            //outFile.write(channels, 0, 2);  // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
            outFile.write(intToBytes(1), 0, 2);  // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
            //8000 sampleRate
            //outFile.write(sampleRate, 0, 4);        // 24 - samples per second (numbers per second)
            outFile.write(longToBytes(8000), 0, 4);        // 24 - samples per second (numbers per second)
            //8000 byteRate
            //outFile.write(byteRate, 0, 4);      // 28 - bytes per second
            outFile.write(longToBytes(8000), 0, 4);      // 28 - bytes per second
            //1 blockAlign
            //outFile.write(blockAlign, 0, 2);    // 32 - # of bytes in one sample, for all channels
            outFile.write(intToBytes(1), 0, 2);    // 32 - # of bytes in one sample, for all channels
            //8 bits per sample
            //outFile.write(bitsPerSample, 0, 2); // 34 - how many bits in a sample(number)?  usually 16 or 24
            outFile.write(intToBytes(8), 0, 2); // 34 - how many bits in a sample(number)?  usually 16 or 24
            outFile.writeBytes("data");                 // 36 - data
            //outFile.write(dataSize, 0, 4);      // 40 - how big is this data chunk
            outFile.write(longToBytes(outDataSize), 0, 4);      // 40 - how big is this data chunk
            
            //outFile.write(myData);                      // 44 - the actual data itself - just a long string of numbers
            outFile.write(outData);                      // 44 - the actual data itself - just a long string of numbers
            
            outFile.close();
            
            //MediaPlayer mp = new MediaPlayer();

            // Set data source -
           // mp.setDataSource("/sdcard/out1.wav");

            // Play audio
           // mp.start();
            File f = new File("/sdcard/out1.wav");  //    
            MediaPlayer mp = MediaPlayer.create(this, Uri.fromFile(f));
            mp.start();
            /*
            MediaPlayer mpPlayProgram = new MediaPlayer();
            mpPlayProgram.setDataSource("/sdcard/out1.wav");
            mpPlayProgram.prepare();
            mpPlayProgram.start();
            mpPlayProgram.reset();
            mpPlayProgram.release();
            */
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }
    
     public static long bytesToLong(byte[] bytes) {
         if (bytes.length > 8) {
             //throw new Exception("byte should not be more than 8 bytes");
         }
         long r = 0;
         for (int i = bytes.length - 1; i >= 0; i--) {
             r = r << 8;
             r += bytes[i];
         }
         return r;
     }
     
     public static long bytesToInt(byte[] bytes) {
         if (bytes.length > 4) {
             //throw new Exception("byte should not be more than 8 bytes");
         }
         int r = 0;
         for (int i = bytes.length - 1; i >= 0; i--) {
             r = r << 8;
             r += bytes[i];
         }
         return r;
     }
     
     public static byte[] longToBytes(long data) {
    	 byte[] ret = new byte[4];
    	 int i = 0;
    	 while(i < 4) {
    		 ret[i++] = (byte)(data % (0xff + 1));
    		 data = data >> 8;
    	 }
    	 return ret;
     }
     
     public static byte[] intToBytes(long data) {
    	 byte[] ret = new byte[2];
    	 int i = 0;
    	 while(i < 2) {
    		 ret[i++] = (byte)(data % (0xff + 1));
    		 data = data >> 8;
    	 }
    	 return ret;
     }
}
