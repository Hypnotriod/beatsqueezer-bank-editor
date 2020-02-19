
package com.hypnotriod.beatsqueezereditor.constants;

/**
 *
 * @author Ilya Pikin
 */
public class Strings {
    public static final String TITLE = "Beatsqueezer Bank Editor v1.0.3 - %s [%.1fMB]";
    
    public static final String ALERT_TITLE_ERROR            = "Error";
    public static final String ALERT_HEADER_ERROR           = "Ops, something went wrong!";
    public static final String ALERT_TITLE_INFO             = "Info";
    
    public static final String LOAD_SAMPLES                 = "Load samples";
    public static final String EXPORT_SAMPLES               = "Export samples";
    public static final String SAVE_BTSQ_FILE               = "Save bank";
    public static final String LOAD_BTSQ_FILE               = "Load bank";
    
    public static final String PAN_VALUE                    = "Panorama: %d%%";
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
    public static final String FILE_NAME_UNTITLED           = "Untitled";
    
    public static final String TOOLTIP_NOTE                 = "The next sample(s) loaded will be associated with this note name.\nThe note name will be auto incremented on each sample load.\nNote names are based on MIDI specification: where C0 (0) is the lowest note,\nG10 (127) is the highest note, and C5 (60) is the \"Middle C\"\nCheck for the alternative Note Names at \"Options\"->\"Note Names Display\".";
    public static final String TOOLTIP_NOTE_SHORT           = "Note names are based on MIDI specification: where C0 (0) is the lowest note,\nG10 (127) is the highest note, and C5 (60) is the \"Middle C\"\nCheck for the alternative Note Names at \"Options\"->\"Note Names Display\".";
    public static final String TOOLTIP_GROUP_ID             = "Only one instance of sample from the specific \"Cut Group\" (1 - 30) can be played simultaneously.\n \"self\" group means that only one instance of current sample can be played simultaneously.\n \"none\" group means that any number of instances of the current sample can be played simultaneously.";
    public static final String TOOLTIP_PITCH                = "The next sample(s) loaded will be pitched by semitones amount.";
    public static final String TOOLTIP_PITCH_STEP           = "By setting \"Pitch Step\" more than 1 semitone, program will\nautomatically generate missed pitched samples from the next sample(s) loaded.";
    public static final String TOOLTIP_NORMALIZE            = "The gain of next sample(s) loaded will be normalized up to dB amount\nby the peak normalization algorithm.";
    public static final String TOOLTIP_PANORAMA             = "Sample's panorama. \"-100%\" is full to the left and \"+100%\" is full to the right.\nCan only be used for mono samples.";
    public static final String TOOLTIP_DYNAMIC              = "When checked, the level of the sample will depend on the velocity.";
    public static final String TOOLTIP_DISABLE_NOTE_OFF     = "When checked, the sample will ignore the \"note off\" message and play to the end.\nIf \"One Shot\" and Loop Point are defined, sample goes to \"Loop trigger\" mode,\nwhen 1-st press will play sample and 2-nd will stop it.";
    public static final String TOOLTIP_LOOP                 = "When checked, sample will loop from the \"loop point\" to the end.\nRequired \"Sustain Loop\", \"Loop\" or \"Marker\" info in the wav file.";
    public static final String TOOLTIP_STEREO               = "When checked, the next sample(s) loaded will keep its stereo channels.";
    public static final String TOOLTIP_DEFAULT_SAMPLE       = "This sample will be played in full range of note velocity,\nif \"Piano\" or/and \"Forte\" samples are not exist.";
    public static final String TOOLTIP_PIANO_SAMPLE         = "If exists, this sample will be played for note velocity range of 1 - 60.\nOtherwise \"Default\" sample will be played.";
    public static final String TOOLTIP_FORTE_SAMPLE         = "If exists, this sample will be played for note velocity range of 110 - 127.\nOtherwise \"Default\" sample will be played.";
    public static final String TOOLTIP_DELETE_SAMPLE        = "Delete sample.";
    public static final String TOOLTIP_REFRESH              = "Refresh and sort samples list.";
    public static final String TOOLTIP_PLAY                 = "Play/Stop sample.";
    public static final String TOOLTIP_INCREASE_LOOP_START  = "Increase \"loop point\" position time.";
    public static final String TOOLTIP_DECREASE_LOOP_START  = "Decrease \"loop point\" position time.";
    public static final String TOOLTIP_LOOP_START_POSITION  = "\"Loop point\" position time.";
    
    public static final String[] NORMALIZE_DB_VALUES = {
        "no",
        "0 dB",
        "-1 dB",
        "-2 dB",
        "-3 dB",
        "-4 dB",
        "-5 dB",
        "-6 dB",
        "-7 dB",
        "-8 dB",
        "-9 dB",
        "-10 dB",
        "-11 dB",
        "-12 dB",
        "-13 dB",
        "-14 dB",
        "-15 dB",
        "-16 dB",
        "-17 dB",
        "-18 dB",
        "-19 dB",
        "-20 dB"
    };

    public static final String[] ENABLE_DISABLE = {
        "Check",
        "Uncheck"
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
        "One Shot",
        "Loop",
        "Note"
    };

    public static final String[] MENUES_OPTIONS = {
        "Note Names Display"
    };

    public static final String[] MENUES_NOTES_NAMES_DISPLAY = {
        "C5 as Middle C",
        "C4 as Middle C",
        "C3 as Middle C",
        "Numbers",
        "Percussion Map"
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
        "+5 octaves",};

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
        "+5 octaves",};

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
        "Velocity Timeshift",
        "Low Pass Envelope Amount",
        "Low Pass Mix",
        "Delay Panorama Spread",
        "Delay Feedback"
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
        "31",
        "32",
        "33",
        "34",
        "35",
        "36",
        "37",
        "38",
        "39",
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

    public static int getIndexOfStringInArray(String groupName, String[] stringsArray) {
        int i;
        for (i = 0; i < stringsArray.length; i++) {
            if (stringsArray[i].equals(groupName)) {
                break;
            }
        }
        return i;
    }
}
