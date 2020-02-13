package com.hypnotriod.beatsqueezereditor.view.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseViewController;
import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.Groups;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.constants.Styles;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.model.dto.PlayEvent;
import com.hypnotriod.beatsqueezereditor.model.dto.SampleDragEvent;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.utility.ComboBoxUtil;
import com.hypnotriod.beatsqueezereditor.utility.StringUtils;
import com.hypnotriod.beatsqueezereditor.utility.TooltipUtil;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCell;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCellHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Ilya Pikin
 */
public class MainSceneViewController extends BaseViewController implements Initializable, SampleListCellHandler {

    public static final String ON_ADD_SAMPLES = "ON_ADD_SAMPLES";
    public static final String ON_EXPORT_SAMPLES = "ON_EXPORT_SAMPLES";
    public static final String ON_SAVE_BANK = "ON_SAVE_BANK";
    public static final String ON_LOAD_BANK = "ON_LOAD_BANK";

    public static final String ON_SAMPLE_DELETE = "ON_SAMPLE_DELETE";
    public static final String ON_SAMPLE_PLAY_STOP = "ON_SAMPLE_PLAY_STOP";
    public static final String ON_SAMPLE_DRAG = "ON_SAMPLE_DRAG";
    public static final String ON_SAMPLES_CLEAR = "ON_SAMPLES_CLEAR";

    public static final String ON_FILES_DRAG = "ON_FILES_DRAG";
    public static final String ON_NOTES_NAMES_DISPLAY_CHANGED = "ON_NOTES_NAMES_DISPLAY_CHANGED";
    public static final String ON_CURSOR_CHANGED = "ON_CURSOR_CHANGED";

    @FXML
    private ListView listView;
    @FXML
    private ToolBar toolBar;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuFilters;
    @FXML
    private Menu menuMacro;
    @FXML
    private Menu menuOptions;
    @FXML
    private VBox progressBox;
    @FXML
    private ComboBox cbPitch;
    @FXML
    private ComboBox cbPitchStep;
    @FXML
    private ComboBox cbNoteId;
    @FXML
    private ComboBox cbGroupId;
    @FXML
    private ComboBox cbNormalize;
    @FXML
    private CheckBox chbDynamic;
    @FXML
    private CheckBox chbDisableNoteOff;
    @FXML
    private CheckBox chbLoop;
    @FXML
    private CheckBox chbStereo;
    @FXML
    private Slider sliderPan;
    @FXML
    private Label labelsSiderValue;
    @FXML
    private Button btnSort;
    @FXML
    private Label labelPitch;
    @FXML
    private Label labelPitchStep;
    @FXML
    private Label labelNote;
    @FXML
    private Label labelCutGroup;
    @FXML
    private Label labelNormalize;

    private Menu noteNamesMenu;

    private HashMap<String, Sample> samples;
    private SampleOptions sampleOptions;
    private ArrayList<ComboBox<String>> cbsFilters;

    private boolean cbFiltersUpdateInProgress = false;
    private boolean dragAndDropInProgress = false;

    public void setSamples(HashMap<String, Sample> samples, SampleOptions sampleOptions) {
        this.samples = samples;
        this.sampleOptions = sampleOptions;

        refreshSelection();
        refreshFiltersValues();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initFiltersMenu();
        initMacroMenu();
        initOptionsMenu();
        initListView();
        initComboboxes();
        initListeners();
        initTooltips();
    }

    private void initOptionsMenu() {
        int i;
        Menu menu;
        RadioMenuItem radioMenuItem;
        ToggleGroup toggleGroup;
        for (i = 0; i < Strings.MENUES_OPTIONS.length; i++) {
            menu = new Menu(Strings.MENUES_OPTIONS[i]);
            menuOptions.getItems().add(menu);

            switch (i) {
                case 0:
                    toggleGroup = new ToggleGroup();
                    for (String value : Strings.MENUES_NOTES_NAMES_DISPLAY) {
                        radioMenuItem = new RadioMenuItem(value);
                        radioMenuItem.setToggleGroup(toggleGroup);
                        radioMenuItem.setOnAction(onMacroNotesNamesDisplayAction);
                        menu.getItems().add(radioMenuItem);
                        noteNamesMenu = menu;
                    }
                    break;
            }
        }
    }

    private void initMacroMenu() {
        int i;
        Menu menu;
        MenuItem menuItem;
        for (i = 0; i < Strings.MENUES_MACRO.length; i++) {
            menu = new Menu(Strings.MENUES_MACRO[i]);
            menuMacro.getItems().add(menu);

            switch (i) {
                case 0:
                    for (String value : Strings.MENUES_PAN) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroPanoramaAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 1:
                    for (String value : Groups.GROUPS_NAMES) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroGroupAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 2:
                    for (String value : Strings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroDynamicAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 3:
                    for (String value : Strings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroDisableNoteOffAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 4:
                    for (String value : Strings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroLoopAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 5:
                    for (String value : Strings.MENUES_NOTES_SHIFT_SEMITONES) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroNoteAction);
                        menu.getItems().add(menuItem);
                    }
                    break;
            }
        }
    }

    private void initFiltersMenu() {
        cbsFilters = new ArrayList<>();
        Menu menuItem;
        CustomMenuItem customMenuItem;
        ComboBox<String> comboBox;
        for (String menuName : Strings.MENUES_FILTERS) {
            menuItem = new Menu(menuName);
            menuFilters.getItems().add(menuItem);
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(Strings.FILTERS_VALUES);
            comboBox.getSelectionModel().selectedItemProperty().addListener(chbFilterChangeListener);
            cbsFilters.add(comboBox);
            customMenuItem = new CustomMenuItem(comboBox, false);
            menuItem.getItems().add(customMenuItem);
        }
    }

    public void refreshFiltersValues() {
        cbFiltersUpdateInProgress = true;
        for (int i = 0; i < sampleOptions.filtersValues.length; i++) {
            cbsFilters.get(i).getSelectionModel().select(sampleOptions.filtersValues[i] + 1);
        }
        cbFiltersUpdateInProgress = false;
    }

    private void setFiltersValues() {
        if (cbFiltersUpdateInProgress) {
            return;
        }
        for (int i = 0; i < sampleOptions.filtersValues.length; i++) {
            sampleOptions.filtersValues[i] = (byte) (cbsFilters.get(i).getSelectionModel().getSelectedIndex() - 1);
        }
    }

    private void initListView() {
        MainSceneViewController self = this;
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> listView) {
                return new SampleListCell(samples, sampleOptions, self);
            }
        });

        listView.setOnDragDropped(onListViewDragDropped);
        listView.setOnDragOver(onListViewDragOver);
        listView.setOnDragEntered(onListViewDragEntered);
        listView.setOnDragExited(onListViewDragExited);
        listView.setFocusTraversable(false);
    }

    private void initComboboxes() {
        cbNoteId.getItems().addAll((Object[]) Notes.NOTES_NAMES_C5);
        cbGroupId.getItems().addAll((Object[]) Groups.GROUPS_NAMES);
        cbNormalize.getItems().addAll((Object[]) Strings.NORMALIZE_DB_VALUES);
        cbPitch.getItems().addAll((Object[]) Strings.MENUES_PITCH_SEMITONES);
        cbPitchStep.getItems().addAll((Object[]) Strings.MENUES_PITCH_STEP_SEMITONES);

        cbPitch.getSelectionModel().select(Strings.MENUES_PITCH_SEMITONES.length / 2);
        cbPitchStep.getSelectionModel().select(0);
    }

    private void initListeners() {
        cbPitch.getSelectionModel().selectedItemProperty().addListener(cbPitchChangeListener);
        cbPitchStep.getSelectionModel().selectedItemProperty().addListener(cbPitchStepListener);
        cbNoteId.getSelectionModel().selectedItemProperty().addListener(cbNoteChangeListener);
        cbGroupId.getSelectionModel().selectedItemProperty().addListener(cbGroupChangeListener);
        cbNormalize.getSelectionModel().selectedItemProperty().addListener(cbNormalizeChangeListener);
        
        ComboBoxUtil.provideScrollOnDropDown(cbPitch);
        ComboBoxUtil.provideScrollOnDropDown(cbPitchStep);
        ComboBoxUtil.provideScrollOnDropDown(cbNoteId);
        ComboBoxUtil.provideScrollOnDropDown(cbGroupId);
        ComboBoxUtil.provideScrollOnDropDown(cbNormalize);
        
        chbDynamic.selectedProperty().addListener(chbDynamicChangeListener);
        chbDisableNoteOff.selectedProperty().addListener(chbDisableNoteOffChangeListener);
        chbLoop.selectedProperty().addListener(chbLoopChangeListener);
        chbStereo.selectedProperty().addListener(chbStereoChangeListener);
        sliderPan.valueProperty().addListener(sliderPanChangeListener);
    }

    public void refreshSelection() {
        cbNoteId.getSelectionModel().select(this.sampleOptions.noteId);
        cbGroupId.getSelectionModel().select(this.sampleOptions.groupId);
        cbNormalize.getSelectionModel().select(this.sampleOptions.normalizeIndex);
        chbDynamic.setSelected(this.sampleOptions.isDynamic);
        chbDisableNoteOff.setSelected(this.sampleOptions.playThrough);
        chbLoop.setSelected(this.sampleOptions.loopEnabled);
        chbStereo.setSelected(this.sampleOptions.stereo);
        sliderPan.setValue(this.sampleOptions.panorama);
        updateLabelSliderValue(this.sampleOptions.panorama);
    }

    private void initTooltips() {
        cbPitch.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PITCH));
        cbPitchStep.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PITCH_STEP));
        cbNoteId.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NOTE));
        cbGroupId.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_GROUP_ID));
        cbNormalize.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NORMALIZE));
        sliderPan.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PANORAMA));
        chbDynamic.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DYNAMIC));
        chbDisableNoteOff.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DISABLE_NOTE_OFF));
        chbLoop.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_LOOP));
        chbStereo.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_STEREO));

        labelPitch.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PITCH));
        labelPitchStep.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PITCH_STEP));
        labelNote.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NOTE));
        labelCutGroup.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_GROUP_ID));
        labelNormalize.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NORMALIZE));
        labelsSiderValue.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PANORAMA));

        btnSort.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_REFRESH));
    }

    public void showLoading() {
        listView.setDisable(true);
        toolBar.setDisable(true);
        menuBar.setDisable(true);
        progressBox.setVisible(true);
    }

    public void hideLoading() {
        listView.setDisable(false);
        toolBar.setDisable(false);
        menuBar.setDisable(false);
        progressBox.setVisible(false);
    }

    public void setNoteNamesSelectionIndex(int index) {
        if (noteNamesMenu.getItems().size() > index) {
            ((RadioMenuItem) noteNamesMenu.getItems().get(index)).setSelected(true);
        }
    }

    public void updateNoteNamesDisplay() {
        int itemIndex = cbNoteId.getSelectionModel().getSelectedIndex();
        cbNoteId.getItems().clear();
        cbNoteId.getItems().addAll((Object[]) sampleOptions.noteNamesDisplay);
        cbNoteId.getSelectionModel().select(itemIndex);
    }

    public void refreshListView(boolean recreate, boolean sort) {
        if (recreate) {
            ArrayList<String> arrayList = new ArrayList<>();
            ObservableList observableList = FXCollections.observableArrayList();

            this.samples.entrySet().forEach((entry) -> {
                arrayList.add(entry.getKey());
            });

            observableList.setAll(arrayList);
            listView.getItems().clear();
            listView.setItems(observableList);
        }

        if (sort) {
            listView.getItems().sort(listViewComparator);
        }
        btnSort.setDisable(this.samples.isEmpty());

        listView.refresh();
    }

    Comparator<String> listViewComparator = new Comparator<String>() {
        @Override
        public int compare(String key1, String key2) {
            int index1 = samples.get(key1).noteId;
            int index2 = samples.get(key2).noteId;
            if (index1 < index2) {
                return -1;
            } else if (index1 > index2) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    Comparator<Sample> samplesComparator = new Comparator<Sample>() {
        @Override
        public int compare(Sample key1, Sample key2) {
            int index1 = key1.noteId;
            int index2 = key2.noteId;
            if (index1 < index2) {
                return -1;
            } else if (index1 > index2) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private EventHandler onListViewDragOver = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.consume();
                event.acceptTransferModes(TransferMode.MOVE);
            } else {
                event.consume();
            }
        }
    };

    private EventHandler onListViewDragEntered = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            dragAndDropInProgress = true;
            listView.setStyle(Styles.BORDER_HIGHLIGHT);
            event.consume();
        }
    };

    private EventHandler onListViewDragExited = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            dragAndDropInProgress = false;
            listView.setStyle(null);
            event.consume();
        }
    };

    private EventHandler onListViewDragDropped = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            listView.setStyle(null);
            dragAndDropInProgress = false;
            sendToView(ON_FILES_DRAG, event);
        }
    };

    @FXML
    private void handleSortButtonClicked(MouseEvent event) {
        refreshListView(false, true);
    }

    private void updateLabelSliderValue(long value) {
        labelsSiderValue.setText(String.format(Strings.PAN_VALUE, value));
    }

    private void applyStereoPan(float range, int minSize) {
        ArrayList<Sample> sortedSamples = new ArrayList<>();
        for (Map.Entry<String, Sample> entry : this.samples.entrySet()) {
            sortedSamples.add(entry.getValue());
        }
        Collections.sort(sortedSamples, samplesComparator);

        if (sortedSamples.size() >= minSize) {
            float position = Config.PANORAMA_MIN_VALUE * range;
            float step = (position * -2) / (sortedSamples.size() - 1);

            for (Sample sample : sortedSamples) {
                sample.panorama = (int) position;
                position += step;
            }
        }
    }

    EventHandler<ActionEvent> onMacroNotesNamesDisplayAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            sendToView(ON_NOTES_NAMES_DISPLAY_CHANGED, ((MenuItem) event.getSource()).getText());
        }
    };

    EventHandler<ActionEvent> onMacroPanoramaAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem) event.getSource()).getText(), Strings.MENUES_PAN);
            switch (itemIndex) {
                case 0:
                    applyStereoPan(1.0f, 2);
                    break;
                case 1:
                    applyStereoPan(0.75f, 2);
                    break;
                case 2:
                    applyStereoPan(0.50f, 2);
                    break;
                case 3:
                    applyStereoPan(0.25f, 2);
                    break;
                case 4:
                    applyStereoPan(0.0f, 1);
                    break;
            }
            refreshListView(false, false);
        }
    };

    EventHandler<ActionEvent> onMacroGroupAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem) event.getSource()).getText(), Groups.GROUPS_NAMES);
            for (Map.Entry<String, Sample> entry : samples.entrySet()) {
                entry.getValue().groupId = itemIndex;
            }
            refreshListView(false, false);
        }
    };

    EventHandler<ActionEvent> onMacroDynamicAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem) event.getSource()).getText(), Strings.ENABLE_DISABLE);
            for (Map.Entry<String, Sample> entry : samples.entrySet()) {
                entry.getValue().dynamic = (itemIndex == 0);
            }
            refreshListView(false, false);
        }
    };

    EventHandler<ActionEvent> onMacroDisableNoteOffAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem) event.getSource()).getText(), Strings.ENABLE_DISABLE);
            for (Map.Entry<String, Sample> entry : samples.entrySet()) {
                entry.getValue().disableNoteOff = (itemIndex == 0);
            }
            refreshListView(false, false);
        }
    };

    EventHandler<ActionEvent> onMacroLoopAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem) event.getSource()).getText(), Strings.ENABLE_DISABLE);
            for (Map.Entry<String, Sample> entry : samples.entrySet()) {
                if (entry.getValue().loop != null) {
                    entry.getValue().isLoopEnabled = (itemIndex == 0);
                }
            }
            refreshListView(false, false);
        }
    };

    EventHandler<ActionEvent> onMacroNoteAction = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            String text = ((MenuItem) event.getSource()).getText();
            int pitch1Index = Strings.MENUES_NOTES_SHIFT_SEMITONES.length / 2;
            int pitch = StringUtils.getIndexOfStringInArray(text, Strings.MENUES_NOTES_SHIFT_SEMITONES) - pitch1Index;

            if (pitch >= 0) {
                pitch++;
            }

            if (pitch < -Notes.SEMITONES_IN_OCTAVE_NUM) {
                pitch = (-Notes.SEMITONES_IN_OCTAVE_NUM - pitch + 1) * -Notes.SEMITONES_IN_OCTAVE_NUM;
            } else if (pitch > Notes.SEMITONES_IN_OCTAVE_NUM) {
                pitch = (pitch - Notes.SEMITONES_IN_OCTAVE_NUM + 1) * Notes.SEMITONES_IN_OCTAVE_NUM;
            }

            for (Map.Entry<String, Sample> entry : samples.entrySet()) {
                entry.getValue().noteId += pitch;
                if (entry.getValue().noteId < 0) {
                    entry.getValue().noteId = 0;
                } else if (entry.getValue().noteId >= Notes.NOTE_NAMES_NUMBER) {
                    entry.getValue().noteId = (Notes.NOTE_NAMES_NUMBER - 1);
                }
            }
            refreshListView(false, false);
        }
    };

    ChangeListener<String> cbPitchChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            int pitch0Index = Strings.MENUES_PITCH_SEMITONES.length / 2;
            int pitch = StringUtils.getIndexOfStringInArray(newValue, Strings.MENUES_PITCH_SEMITONES) - pitch0Index;

            if (pitch < -Notes.SEMITONES_IN_OCTAVE_NUM) {
                pitch = (-Notes.SEMITONES_IN_OCTAVE_NUM - pitch + 1) * -Notes.SEMITONES_IN_OCTAVE_NUM;
            } else if (pitch > Notes.SEMITONES_IN_OCTAVE_NUM) {
                pitch = (pitch - Notes.SEMITONES_IN_OCTAVE_NUM + 1) * Notes.SEMITONES_IN_OCTAVE_NUM;
            }

            sampleOptions.pitch = pitch;
        }
    };

    ChangeListener<String> cbPitchStepListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            int step = StringUtils.getIndexOfStringInArray(newValue, Strings.MENUES_PITCH_STEP_SEMITONES) + 1;
            if (step >= Notes.SEMITONES_IN_OCTAVE_NUM) {
                step = ((step % Notes.SEMITONES_IN_OCTAVE_NUM) + 1) * Notes.SEMITONES_IN_OCTAVE_NUM;
            }
            sampleOptions.pitchStep = step;
        }
    };

    ChangeListener<String> chbFilterChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            setFiltersValues();
        }
    };

    ChangeListener<String> cbNoteChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            if (newValue != null) {
                sampleOptions.noteId = Strings.getIndexOfStringInArray(newValue, sampleOptions.noteNamesDisplay);
            }
        }
    };

    ChangeListener<String> cbGroupChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            sampleOptions.groupId = Strings.getIndexOfStringInArray(newValue, Groups.GROUPS_NAMES);
        }
    };

    ChangeListener<String> cbNormalizeChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            sampleOptions.normalizeIndex = Strings.getIndexOfStringInArray(newValue, Strings.NORMALIZE_DB_VALUES);
        }
    };

    ChangeListener<Boolean> chbDynamicChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleOptions.isDynamic = newValue;
        }
    };

    ChangeListener<Boolean> chbDisableNoteOffChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleOptions.playThrough = newValue;
        }
    };

    ChangeListener<Boolean> chbLoopChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleOptions.loopEnabled = newValue;
        }
    };

    ChangeListener<Boolean> chbStereoChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleOptions.stereo = newValue;
        }
    };

    ChangeListener<Number> sliderPanChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> selected, Number oldValue, Number newValue) {
            sampleOptions.panorama = newValue.longValue();
            updateLabelSliderValue(newValue.longValue());
        }
    };

    @Override
    public void onSampleListCellDelete(String id) {
        sendToView(ON_SAMPLE_DELETE, id);
    }

    @Override
    public void onSampleListCellPlayStop(Sample sample, double position) {
        sendToView(ON_SAMPLE_PLAY_STOP, new PlayEvent(sample, position));
    }

    @Override
    public void onSampleListDragEntered() {
        dragAndDropInProgress = true;
        listView.setStyle(null);
    }

    @Override
    public void onSampleListDragExited() {
        if (dragAndDropInProgress) {
            listView.setStyle(Styles.BORDER_HIGHLIGHT);
        }
    }

    @Override
    public void onSampleListCellFileDragged(Sample sample, DragEvent event) {
        dragAndDropInProgress = false;
        sendToView(ON_SAMPLE_DRAG, new SampleDragEvent(sample, event));
    }
    
    @Override
    public void onCursorChange(Cursor cursor) {
        sendToView(ON_CURSOR_CHANGED, cursor);
    }

    @FXML
    private void handleMenuItemAddSamplesClicked(ActionEvent event) {
        sendToView(ON_ADD_SAMPLES, null);
    }

    @FXML
    private void handleMenuItemClearSamplesClicked(ActionEvent event) {
        sendToView(ON_SAMPLES_CLEAR, null);
    }

    @FXML
    private void handleMenuItemExportSamplesClicked(ActionEvent event) {
        sendToView(ON_EXPORT_SAMPLES, null);
    }

    @FXML
    private void handleMenuItemloadBankClicked(ActionEvent event) {
        sendToView(ON_LOAD_BANK, null);
    }

    @FXML
    private void handleMenuItemSaveBankClicked(ActionEvent event) {
        sendToView(ON_SAVE_BANK, null);
    }
}
