package com.hypnotriod.beatsqueezereditor.constants;

/**
 *
 * @author Ilya Pikin
 */
public class Config {
    
    public static final int  KNOBS_NUM 				= 30;
    public static final int  GROUPS_NUM 			= 32;
    public static final int  SAMPLE_RATE 			= 44100;
    public static final int  BIT_RATE 				= 16;
    public static final int  BYTES_PER_SAMPLE			= 2;
    public static final int  CHANNELS_NUM 			= 1;
    public static final int  BLOCK_SIZE 			= 1024;
    public static final int  DATA_START_INDEX_V1        	= 2048;
    public static final int  DATA_START_INDEX 			= 4096;
    public static final int  HEADER_CHUNK_SIZE                  = 18;
    public static final int  KNOBS_CHUNK_SIZE                   = 40;
    public static final int  SAMPLE_CHUNK_SIZE                  = 14;
    public static final int  PIANO_FORTE_START_INDEX        	= 2048;
    public static final int  PIANO_FORTE_SAMPLE_CHUNK_SIZE      = 16;
    public static final int  PANORAMA_MIN_VALUE 		= -100;
    public static final int  PANORAMA_MAX_VALUE 		= 100;
    public static final int  FADE_OUT_MS_MAX_VALUE 		= 10000;
    public static final int  MIN_LOOP_LENGTH_SAMPLES		= 256;
    public static final int  MIN_LOOP_LENGTH_BYTES		= (MIN_LOOP_LENGTH_SAMPLES * BYTES_PER_SAMPLE);
    public static final long HEADER_FIRST_CHUNK 		= 0x42545351; // BTSQ
    public static final long HEADER_SECOND_CHUNK 		= 0x42414e4b; // BANK
    public static final int  VERSION			 	= 0x0001;
    public static final int  HEADER_SIZE 			= 8;
    public static final int  VERSION_SIZE 			= 2;
    public static final int  RESAMPLER_BUFFER_SIZE 		= 2048;
    public static final int  RESAMPLER_HIGH_QUALITY 		= 60;
    public static final int  RESAMPLER_LOW_QUALITY 		= 1;
}
