
package com.hypnotriod.beatsqueezereditor.constants;

/**
 *
 * @author Илья
 */
public class CConfig {
    
    public static final int APP_MIN_WIDTH = 600;
    public static final int APP_MAX_WIDTH = 600;
    public static final int APP_MIN_HEIGHT = 300;
    
    public static final String WAVE_FILE_BROWSE_NAME        = "wave file";
    public static final String WAVE_FILE_BROWSE_FILTER_WAV  = "*.wav";
    public static final String WAVE_FILE_BROWSE_FILTER_WAVE = "*.wave";
    public static final String BANK_FILE_BROWSE_NAME        = "Beatsqueezer bank file";
    public static final String BANK_FILE_BROWSE_FILTER      = "*.btsqbank";
    public static final String BANK_FILE_EXTENSION          = ".btsqbank";
    public static final String WAVE_FILE_EXTENSION          = ".wave";
    public static final String WAV_FILE_EXTENSION           = ".wav";
    public static final String EXT_DEFAULT                  = "";
    public static final String EXT_P                        = "_p";
    public static final String EXT_F                        = "_f";

    public static final int  KNOBS_NUM 				= 26;
    public static final int  GROUPS_NUM 			= 32;
    public static final int  SAMPLE_RATE 			= 44100;
    public static final int  BIT_RATE 				= 16;
    public static final int  BYTES_PER_SAMPLE			= 2;
    public static final int  CHANNELS_NUM 			= 1;
    public static final int  BLOCK_SIZE 			= 1024;
    public static final int  DATA_START_INDEX_V1        	= 2048;
    public static final int  DATA_START_INDEX 			= 4096;
    public static final int  CONFIG_BLOCK_SIZE 			= 4096;
    public static final int  CONFIG_HEADER_CHUNK_SIZE           = 18;
    public static final int  CONFIG_KNOBS_CHUNK_SIZE            = 40;
    public static final int  CONFIG_SAMPLE_CHUNK_SIZE           = 14;
    public static final int  CONFIG_PF_START_INDEX        	= 2048;
    public static final int  CONFIG_PF_SAMPLE_CHUNK_SIZE        = 16;
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
