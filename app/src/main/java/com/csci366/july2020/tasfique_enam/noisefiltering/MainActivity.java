package com.csci366.july2020.tasfique_enam.noisefiltering;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    ImageView btNoise, btDeNoise, btStop, audioPicture;

    MediaPlayer mediaPlayer;
    Runnable runnable;
    private final String fileName = "audio1.wav";
    private TextView mTextView;
    private WavInfo mWavInfo;
    private AudioTrack mAudioTrack;
    private boolean continuePlaying = false;
    ShortBuffer mSamples;
    int mNumSamples;
    short[] audioSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assigning Values
        btNoise = findViewById(R.id.buttonOriginal);
        btDeNoise = findViewById(R.id.bt_denoise);
        btStop = findViewById(R.id.bt_stop);
        audioPicture = findViewById(R.id.audio_picture);
        mTextView = findViewById(R.id.textViewInfo);
        audioSamples = readWavData();

        btNoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuePlaying = true;

                // Allocate ShortBuffer
                mSamples = ShortBuffer.allocate(mNumSamples);
                // put audio samples to ShortBuffer
                mSamples.put(audioSamples);
                playAudio();

            }
        });

        btDeNoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuePlaying = true;
                short[] filteredSamples = new short[mNumSamples];
                for(int i = 14; i < filteredSamples.length; i++) {
                    filteredSamples[i] = (short)((audioSamples[i] + audioSamples[i-1] + audioSamples[i-2] + audioSamples[i-3] + audioSamples[i-4] + audioSamples[i-5] + audioSamples[i-6] + audioSamples[i-7] + audioSamples[i-8] + audioSamples[i-9] +
                            audioSamples[i-10] + audioSamples[i-11] + audioSamples[i-12] + audioSamples[i-13] + audioSamples[i-14]) / 15);
                }

                // Allocate ShortBuffer
                mSamples = ShortBuffer.allocate(mNumSamples);
                // put audio samples to ShortBuffer
                mSamples.put(filteredSamples);
                playAudio();
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuePlaying = false;
            }
        });

    }

//    public void startPlaying(View view) {
//        continuePlaying = true;
//
//        // Allocate ShortBuffer
//        mSamples = ShortBuffer.allocate(mNumSamples);
//        // put audio samples to ShortBuffer
//        mSamples.put(audioSamples);
//        playAudio();
//    }

    public class WavInfo {
        AudioSpec spec;
        int size;

        public WavInfo(AudioSpec sp, int sz){
            spec = sp;
            size = sz;
        }

        int getSize() {
            return size;
        }

        public AudioSpec getSpec() {
            return spec;
        }

    }

    public class AudioSpec {
        int freq;
        byte channels;

        public AudioSpec(int f, byte chs){
            freq = f;
            channels = chs;
        }

        public int getRate(){
            return freq;
        }

        public byte getChannels() {
            return channels;
        }

    }

    public class LoadWav {
        private static final String RIFF_HEADER = "RIFF";
        private static final String WAVE_HEADER = "WAVE";
        private static final String FMT_HEADER = "fmt ";
        private static final String DATA_HEADER = "data";
        private static final int HEADER_SIZE = 44;
        private static final String CHARSET = "ASCII";
        private void checkFormat(boolean cond, String message){
            if (!cond) {
                Log.d(TAG, message);
            }
        }

        public WavInfo readHeader(InputStream wavStream) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());
            buffer.rewind();
            buffer.position(buffer.position() + 20);
            int format = buffer.getShort();
            checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM
            short channels = buffer.getShort();
            checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);
            int rate = buffer.getInt();
            checkFormat(rate <= 48000 && rate >= 8000, "Unsupported rate: " + rate);
            buffer.position(buffer.position() + 6);
            int bits = buffer.getShort();
            checkFormat(bits == 16, "Unsupported bits: " + bits);
            int dataSize = 0;
            while (buffer.getInt() != 0x61746164) { // "data" marker
                Log.d(TAG, "Skipping non-data chunk");
                int size = buffer.getInt();
                wavStream.skip(size);
                buffer.rewind();
                wavStream.read(buffer.array(), buffer.arrayOffset(), 8); buffer.rewind();
            }
            dataSize = buffer.getInt();
            checkFormat(dataSize > 0, "wrong data size: " + dataSize);
            return new WavInfo(new AudioSpec(rate, (byte)channels), dataSize);
        }

        public byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {
            byte[] data = new byte[info.getSize()];
            stream.read(data, 0, data.length);
            return data;
        }


    }

    private short[] readWavData() {
        // read WAV file
        InputStream in = getApplicationContext().getResources().openRawResource(R.raw.audio1);
        LoadWav loadWav = new LoadWav();
        int numOfChannels, samplingRate, numOfSamples;
        float lengthInSecond;
        byte[] byteData;
        short[] shortData;
        try {
            mWavInfo = loadWav.readHeader(in);       //read input file header
            numOfChannels = mWavInfo.getSpec().getChannels();
            samplingRate = mWavInfo.getSpec().getRate();
            numOfSamples = mWavInfo.getSize() / 2;       //each sample is 2 bytes (16-bit)
            mNumSamples = numOfSamples;
            lengthInSecond = (float) numOfSamples / (samplingRate * numOfChannels);
            byteData = loadWav.readWavPcm(mWavInfo, in);   //read samples
            shortData = new short[numOfSamples];
            // convert Audio data from 8-bit to 16-bit
            for (int i = 0; i<numOfSamples; i++) {
                short LSB = (short) byteData[2 * i];
                short MSB = (short) byteData[2 * i + 1];
                shortData[i] = (short) ( MSB << 8 | (0xFF & LSB) );
            }
            mTextView.setText(fileName + "\n" + "" +
                    "Number of Channels : " + numOfChannels + "\n" +
                    "Sampling Rate : " + samplingRate + " Hz" + "\n" +
                    "Number of Samples : " + numOfSamples + "\n" +
                    "Duration : " + lengthInSecond + " seconds");
            return shortData;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    void playAudio() {
        // run as thread to get better responsiveness
        new Thread(new Runnable() {
            @Override
            public void run() {
                // for mono 16 bits
                // Estimate minimum buffer size to store the audio samples that is going to play
                // buffer space is required for smooth playback
                int bufferSize = AudioTrack.getMinBufferSize(mWavInfo.spec.getRate(),
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // if the above fail, set buffer size to 2 times of sampling rate
                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = mWavInfo.spec.getRate() * 2;
                }

                // create AudioTrack object for audio playback
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        mWavInfo.spec.getRate(),
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();


                // create buffer - short (16-bit) data type
                short[] buffer = new short[bufferSize];
                mSamples.rewind();          //go to the beginning of mSamples
                int limit = mNumSamples;
                int totalWritten = 0;
                // Start to write samples to buffer
                while (mSamples.position() < limit && continuePlaying) {
                    int numSamplesLeft = limit - mSamples.position();
                    int samplesToWrite;
                    if (numSamplesLeft >= buffer.length) {
                        mSamples.get(buffer);               //Transfer data from mSamples to buffer upto length of buffer
                        samplesToWrite = buffer.length;
                    } else {
                        // fill up the extra buffer space with 0
                        for (int i = numSamplesLeft; i < buffer.length; i++) {
                            buffer[i] = 0;
                        }
                        mSamples.get(buffer, 0, numSamplesLeft);
                        samplesToWrite = numSamplesLeft;
                    }
                    totalWritten += samplesToWrite;
                    // write the audio samples to the audio device for playback
                    audioTrack.write(buffer, 0, samplesToWrite);
                }



                if (!continuePlaying) {
                    audioTrack.release();
                }

            }
        }).start();
    }
}