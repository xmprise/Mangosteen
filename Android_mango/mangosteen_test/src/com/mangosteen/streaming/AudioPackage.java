package com.mangosteen.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.mangosteen.streaming.HlsActivity.Encoding;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;
import android.util.Log;

public class AudioPackage implements Runnable {
	private String Rootdir = "/mnt/sdcard/jetty/webapps/Mangosteen/hls/temp";
	public int buffersize = 0;
	private final int RECORDER_BPP = 16;
	private final int RECORDER_SAMPLERATE = 0xac44;
	private final int WAVE_CHANNEL_MONO = 1;
	private final int HEADER_SIZE = 0x2c;
	private long NextTime;
	private int file_num=0, plustime = 2000;
	private Encoding EncodingThread;
	AudioRecord audiorecord;
	Thread audiorecordingThread;
	Boolean record = false;

	public AudioPackage(long time, Encoding enc) {
		NextTime = time - time%1000 + 2000;
		EncodingThread = enc;
	}

	public byte[] getFileHeader(int mAudioLen) {
		byte[] header = new byte[HEADER_SIZE];
		int totalDataLen = mAudioLen + 40;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * WAVE_CHANNEL_MONO / 8;
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = (byte) 1; // format = 1 (PCM占쏙옙占�
		header[21] = 0;
		header[22] = WAVE_CHANNEL_MONO;
		header[23] = 0;
		header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);
		header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);
		header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);
		header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) RECORDER_BPP * WAVE_CHANNEL_MONO / 8; // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (mAudioLen & 0xff);
		header[41] = (byte) ((mAudioLen >> 8) & 0xff);
		header[42] = (byte) ((mAudioLen >> 16) & 0xff);
		header[43] = (byte) ((mAudioLen >> 24) & 0xff);
		return header;
	}
	
	public void audioWrite() {
		byte[] data = new byte[buffersize];
		byte[] buffer = new byte[buffersize];
		try {

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(Rootdir + "/audio_temp_" + file_num + ".temp"));
			int read = 0;

			while (record) {
				if(NextTime <= System.currentTimeMillis())
				{
					read = audiorecord.read(data, 0, buffersize);
					if (audiorecord.ERROR_INVALID_OPERATION != read) {
						if (NextTime + plustime <= System.currentTimeMillis()) {
							final int file_number = file_num;
							final long time = 0;
							final Encoding enc = EncodingThread;
							bos.close();
							Thread thread = new Thread(new Runnable() {
								public void run() {
									Log.e("HlsActivity", "audiothread start");
									AudioPackage taudio = new AudioPackage(
											time, enc);
									int read = 0;
									byte[] buffer = new byte[buffersize];
									File tempfile = new File(Rootdir
											+ "/audio_temp_" + file_number
											+ ".temp");
									BufferedInputStream bio;
									try {
										bio = new BufferedInputStream(
												new FileInputStream(tempfile));
										BufferedOutputStream bos = new BufferedOutputStream(
												new FileOutputStream(Rootdir
														+ "/audio_"
														+ file_number + ".wav"));
										bos.write(taudio
												.getFileHeader((int) tempfile
														.length()));
										while ((read = bio.read(buffer)) != -1) {
											bos.write(buffer);
										}
										bos.flush();
										bos.close();
										bio.close();
										Log.e("HlsActivity", "audiothread end");
										Message msg = new Message();
										msg.arg1 = 1;
										EncodingThread.hhandler
												.sendMessage(msg);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Log.e("HlsActivity", "audiothread exception");
										e.printStackTrace();
									}
								}
							});
							thread.start();
							NextTime += plustime;
							file_num++;
							bos = new BufferedOutputStream(
									new FileOutputStream(Rootdir
											+ "/audio_temp_" + file_num
											+ ".temp"));
						}
						try {
							bos.write(data);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			}
			bos.flush();
			
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stoprecording() {
		record = false;
		audiorecordingThread.interrupt();
		audiorecord.stop();
		audiorecord.release();
	}

	public void run() {
		// TODO Auto-generated method stub

		record = true;
		buffersize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		audiorecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, buffersize);

		try {
			audiorecord.startRecording();
		} catch (Exception e) {
			e.printStackTrace();
		}
		audiorecordingThread = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				audioWrite();

			}
		});
		audiorecordingThread.start();
	}
}
