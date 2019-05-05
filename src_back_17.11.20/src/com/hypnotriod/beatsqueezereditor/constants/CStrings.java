
package com.hypnotriod.beatsqueezereditor.constants;

/**
 *
 * @author Илья
 */
public class CStrings {
    public static final String TITLE = "Beatsqueezer bank editor v0.9.6";
    
    public static final String ALERT_TITLE_ERROR            = "Error";
    public static final String ALERT_HEADER_ERROR           = "Ops, something went wrong!";
    public static final String ALERT_TITLE_INFO             = "Info";
    
    public static final String LOAD_SAMPLES                 = "Load samples";
    public static final String EXPORT_SAMPLES               = "Export samples";
    public static final String SAVE_BTSQ_FILE               = "Save bank";
    public static final String LOAD_BTSQ_FILE               = "Load bank";
    
    public static final String PAN_VALUE                    = "Pan: %d";
    public static final String SAMPLE_NAME                  = "%s_%03d_%s";
    public static final String STOP                         = "Stop";
    public static final String PLAY                         = "Play";
    public static final String SAMPLES_DATA_SIZE_INFO       = "Samples data size is: %f MB";
    public static final String AUDIO_OUTPUT_DEVICE_PROBLEM  = "Audio output device problem!\n\n%s";
    public static final String LOOP_TIME_ERROR              = "%s should have more than 10 milliseconds of loop time!";
    public static final String NOTE_IS_DUBLICATED           = "Note %s is dublicated!";
    public static final String TRUNCATED_SAMPLE_ERROR       = "All samples with sustain loop will be truncated by the end of the loop!";
    public static final String WAV_FILE_ERROR               = "%s\n\nPlease use PCM 16 or 24 bit file format only.";
    public static final String OUT_OF_MEMORY_ERROR          = "Out of memory!";
    public static final String DEFAULT_SAMPLE_NOT_FOUND     = "\"Default\" sample does not found for\n\n%s";
    public static final String NO_SAMPLES                   = "No samples to export!";
    public static final String NOT_A_BANK                   = "Selected bank does not correspond to version format #%s";
    public static final String BANK_IS_EMPTY                = "Bank is empty!";
    public static final String SAMPLE_PITCHED               = " +%d semitone(s) pitch";
    public static final String PIANO_SAMPLE_NAME_PROMT      = "Add \"_p\" to the end of your sample name: \"%s_p\"\nOr drag sample here.";
    public static final String FORTE_SAMPLE_NAME_PROMT      = "Add \"_f\" to the end of your sample name: \"%s_f\"\nOr drag sample here.";
    
    public static final String[] ENABLE_DISABLE = {
        "Enable",
        "Disable"
    };
    
    public static final String[] MENUES_PAN = {
        "Stereo 100%",
        "Stereo 75%",
        "Stereo 50%",
        "Stereo 25%",
        "To center"
    };
    
    public static final String[] MENUES_MACRO = {
        "Panorama",
        "Cut Group",
        "Dynamic",
        "Disable Note Off",
        "Loop",
        "Note"
    };
    
    public static final String[] MENUES_NOTES_SHIFT_SEMITONES = {
        "-5 octaves",
        "-4 octaves",
        "-3 octaves",
        "-2 octaves",
        "-1 octave",
        "-11 semitones",
        "-10 semitones",
        "-9 semitones",
        "-8 semitones",
        "-7 semitones",
        "-6 semitones",
        "-5 semitones",
        "-4 semitones",
        "-3 semitones",
        "-2 semitones",
        "-1 semitone",
        "+1 semitone",
        "+2 semitones",
        "+3 semitones",
        "+4 semitones",
        "+5 semitones",
        "+6 semitones",
        "+7 semitones",
        "+8 semitones",
        "+9 semitones",
        "+10 semitones",
        "+11 semitones",
        "+1 octave",
        "+2 octaves",
        "+3 octaves",
        "+4 octaves",
        "+5 octaves",
    };
    
    public static final String[] MENUES_PITCH_SEMITONES = {
        "-5 octaves",
        "-4 octaves",
        "-3 octaves",
        "-2 octaves",
        "-1 octave",
        "-11 semitones",
        "-10 semitones",
        "-9 semitones",
        "-8 semitones",
        "-7 semitones",
        "-6 semitones",
        "-5 semitones",
        "-4 semitones",
        "-3 semitones",
        "-2 semitones",
        "-1 semitone",
        "no pitch",
        "+1 semitone",
        "+2 semitones",
        "+3 semitones",
        "+4 semitones",
        "+5 semitones",
        "+6 semitones",
        "+7 semitones",
        "+8 semitones",
        "+9 semitones",
        "+10 semitones",
        "+11 semitones",
        "+1 octave",
        "+2 octaves",
        "+3 octaves",
        "+4 octaves",
        "+5 octaves",
    };
    
    public static final String[] MENUES_PITCH_STEP_SEMITONES = {
        "1 semitone",
        "2 semitones",
        "3 semitones",
        "4 semitones",
        "5 semitones",
        "6 semitones",
        "7 semitones",
        "8 semitones",
        "9 semitones",
        "10 semitones",
        "11 semitones",
        "1 octave",
        "2 octaves",
        "3 octaves",
        "4 octaves",
        "5 octaves",
        "6 octaves",
        "7 octaves",
        "8 octaves",
        "9 octaves",
        "10 octaves"
    };
    
    public static final String[] MENUES_FILTERS = {
        "Low Pass Cut Off",
        "Low Pass Resonance",
        "Ring Modulation Frequency",
        "Ring Modulation Value",
        "Flanger Frequency",
        "Flanger Value",
        "Rotary Vibrato Frequency",
        "Rotary Vibrato Value",
        "Limiter Threshold",
        "Bitcrusher Value",
        "Glitch Shift",
        "Glitch Length",
        "Delay Hi Pass Value",
        "Delay Low Pass Value",
        "Delay Value",
        "Delay Time",
        "Attack",
        "Decay",
        "Sustain",
        "Release",
        "Low Pass Attack",
        "Low Pass Decay",
        "Low Pass Sustain",
        "Low Pass Release",
        "Velocity Attack",
        "Velocity Timeshift"
    };
    public static final String[] FILTERS_VALUES = {
        "not specified",
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
        "30",
        "40",
        "41",
        "42",
        "43",
        "44",
        "45",
        "46",
        "47",
        "48",
        "49",
        "50",
        "51",
        "52",
        "53",
        "54",
        "55",
        "56",
        "57",
        "58",
        "59",
        "60",
        "61",
        "62",
        "63",
        "64",
        "65",
        "66",
        "67",
        "68",
        "69",
        "70",
        "71",
        "72",
        "73",
        "74",
        "75",
        "76",
        "77",
        "78",
        "79",
        "80",
        "81",
        "82",
        "83",
        "84",
        "85",
        "86",
        "87",
        "88",
        "89",
        "90",
        "91",
        "92",
        "93",
        "94",
        "95",
        "96",
        "97",
        "98",
        "99",
        "100",
        "101",
        "102",
        "103",
        "104",
        "105",
        "106",
        "107",
        "108",
        "109",
        "110",
        "111",
        "112",
        "113",
        "114",
        "115",
        "116",
        "117",
        "118",
        "119",
        "120",
        "121",
        "122",
        "123",
        "124",
        "125",
        "126",
        "127"
    };
}
