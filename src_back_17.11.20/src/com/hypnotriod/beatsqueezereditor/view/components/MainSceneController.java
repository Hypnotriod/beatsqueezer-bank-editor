
package com.hypnotriod.beatsqueezereditor.view.components;

import com.hypnotriod.beatsqueezereditor.base.BaseViewController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CGroups;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.model.vo.OptionsVO;
import com.hypnotriod.beatsqueezereditor.model.vo.PlayEventVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleDragEventVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Илья
 */
public class MainSceneController extends BaseViewController implements Initializable , ISampleListCellHandler
{
    public static final String ON_ADD_SAMPLES = "ON_ADD_SAMPLES";
    public static final String ON_EXPORT_SAMPLES = "ON_EXPORT_SAMPLES";
    public static final String ON_SAVE_BANK = "ON_SAVE_BANK";
    public static final String ON_LOAD_BANK = "ON_LOAD_BANK";
    
    public static final String ON_SAMPLE_DELETE = "ON_SAMPLE_DELETE";
    public static final String ON_SAMPLE_PLAY   = "ON_SAMPLE_PLAY";
    public static final String ON_SAMPLE_DRAG   = "ON_SAMPLE_DRAG";
    public static final String ON_SAMPLES_CLEAR = "ON_SAMPLES_CLEAR";
    
    @FXML
    private ListView listView;
    @FXML
    private ToolBar  toolBar;
    @FXML
    private MenuBar  menuBar;
    @FXML
    private Menu     menuPitch; 
    @FXML
    private Menu     menuPitchStep; 
    @FXML
    private Menu     menuFilters; 
    @FXML
    private Menu     menuMacro;
    @FXML
    private VBox     progressBox;
    @FXML
    private ComboBox cbNoteID;
    @FXML
    private ComboBox cbGroupID;
    @FXML
    private CheckBox chbDynamic;
    @FXML
    private CheckBox chbDisableNoteOff;
    @FXML
    private CheckBox chbLoop;
    @FXML
    private CheckBox chbStereo;
    @FXML
    private Slider   sliderPan;
    @FXML
    private Label    labelsSiderValue;
    @FXML
    private Button   btnSort;
    
    private HashMap<String, SampleVO> sampleVOs;
    private OptionsVO optionsVO;
    private ArrayList<ComboBox<String>> cbsFilters;

    public void setSampleVOs(HashMap<String, SampleVO> sampleVOs, OptionsVO optionsVO) {
        this.sampleVOs = sampleVOs;
        this.optionsVO = optionsVO;
        
        refreshSelection();
        refreshFiltersValues();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        initPitchMenu();
        initPitchStepMenu();
        initFiltersMenu();
        initMacroMenu();
        initListView();
        initComboboxes();
        initListeners();
    } 
    
    private void initMacroMenu()
    {
        int i;
        Menu menu;
        MenuItem menuItem;
        for(i = 0; i < CStrings.MENUES_MACRO.length; i++)
        {
            menu = new Menu(CStrings.MENUES_MACRO[i]);
            menuMacro.getItems().add(menu);
            
            switch(i)
            {
                case 0:
                    for(String value : CStrings.MENUES_PAN) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroPanorama);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 1:
                    for(String value : CGroups.GROUPS_NAMES) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroGroup);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 2:
                    for(String value : CStrings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroDynamic);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 3:
                    for(String value : CStrings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroDisableNoteOff);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 4:
                    for(String value : CStrings.ENABLE_DISABLE) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroLoop);
                        menu.getItems().add(menuItem);
                    }
                    break;
                case 5:
                    for(String value : CStrings.MENUES_NOTES_SHIFT_SEMITONES) {
                        menuItem = new MenuItem(value);
                        menuItem.setOnAction(onMacroNote);
                        menu.getItems().add(menuItem);
                    }
                    break;
            }
        }
    }
    
    private void initFiltersMenu()
    {
        cbsFilters = new ArrayList<>();
        Menu menuItem;
        CustomMenuItem customMenuItem; 
        ComboBox<String> comboBox;
        for(String menuName : CStrings.MENUES_FILTERS)
        {
            menuItem = new Menu(menuName);
            menuFilters.getItems().add(menuItem);
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(CStrings.FILTERS_VALUES);
            comboBox.getSelectionModel().selectedItemProperty().addListener(chbFilterChangeListener);
            cbsFilters.add(comboBox);
            customMenuItem = new CustomMenuItem(comboBox, false);
            menuItem.getItems().add(customMenuItem);
        }
    }
    
    private void initPitchMenu()
    {
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem radioItem;
        
        for(String menuName : CStrings.MENUES_PITCH_SEMITONES)
        {
            radioItem = new RadioMenuItem(menuName);
            radioItem.setToggleGroup(toggleGroup);
            radioItem.setOnAction(onPitchRadioItemClicked);
            menuPitch.getItems().add(radioItem);
        }
        ((RadioMenuItem)menuPitch.getItems().get(CStrings.MENUES_PITCH_SEMITONES.length / 2)).setSelected(true);
    }
    
    private void initPitchStepMenu()
    {
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem radioItem;
        
        for(String menuName : CStrings.MENUES_PITCH_STEP_SEMITONES)
        {
            radioItem = new RadioMenuItem(menuName);
            radioItem.setToggleGroup(toggleGroup);
            radioItem.setOnAction(onPitchStepRadioItemClicked);
            menuPitchStep.getItems().add(radioItem);
        }
        ((RadioMenuItem)menuPitchStep.getItems().get(0)).setSelected(true);
    }

    private boolean cbFiltersUpdateInProgress = false;
    public void refreshFiltersValues()
    {
        cbFiltersUpdateInProgress = true;
        for(int i = 0; i < optionsVO.filtersValues.length; i++) {
            cbsFilters.get(i).getSelectionModel().select(optionsVO.filtersValues[i] + 1);
        }
        cbFiltersUpdateInProgress = false;
    }
    
    private void setFiltersValues()
    {
        if(cbFiltersUpdateInProgress) return;
        for(int i = 0; i < optionsVO.filtersValues.length; i++) {
            optionsVO.filtersValues[i] = (byte)(cbsFilters.get(i).getSelectionModel().getSelectedIndex() - 1);
        }
    }
    
    private void initListView()
    {
        MainSceneController self = this;
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> listView) {
                return new SampleListCell(sampleVOs, self);
            }
        });
        listView.setFocusTraversable( false );
    }
    
    private void initComboboxes()
    {
        cbNoteID.getItems().addAll((Object[]) CNotes.NOTES_NAMES);
        cbGroupID.getItems().addAll((Object[]) CGroups.GROUPS_NAMES);
    }
    
    private void initListeners()
    {
        cbNoteID.getSelectionModel().selectedItemProperty().addListener(cbNoteChangeListener);
        cbGroupID.getSelectionModel().selectedItemProperty().addListener(cbGroupChangeListener);
        chbDynamic.selectedProperty().addListener(chbDynamicChangeListener);
        chbDisableNoteOff.selectedProperty().addListener(chbDisableNoteOffChangeListener);
        chbLoop.selectedProperty().addListener(chbLoopChangeListener);
        chbStereo.selectedProperty().addListener(chbStereoChangeListener);
        sliderPan.valueProperty().addListener(sliderPanChangeListener);
    }
    
    public void refreshSelection()
    {
        cbNoteID.getSelectionModel().select(this.optionsVO.noteID);
        cbGroupID.getSelectionModel().select(this.optionsVO.groupID);
        chbDynamic.setSelected(this.optionsVO.isDynamic);
        chbDisableNoteOff.setSelected(this.optionsVO.playThrough);
        chbLoop.setSelected(this.optionsVO.loopEnabled);
        chbStereo.setSelected(this.optionsVO.stereo);
        sliderPan.setValue(this.optionsVO.panorama);
        updateLabelSliderValue(this.optionsVO.panorama);
    }
    
    public void showLoading()
    {
        listView.setDisable(true);
        toolBar.setDisable(true);
        menuBar.setDisable(true);
        progressBox.setVisible(true);
    }
    
    public void hideLoading()
    {
        listView.setDisable(false);
        toolBar.setDisable(false);
        menuBar.setDisable(false);
        progressBox.setVisible(false);
    }
    
    public void refreshListView(boolean recreate, boolean sort)
    {
        if(recreate)
        {
            ArrayList<String> arrayList = new ArrayList<>();
            ObservableList observableList = FXCollections.observableArrayList();

            for (Map.Entry<String, SampleVO> entry : this.sampleVOs.entrySet()) {
                arrayList.add(entry.getKey());
            }

            observableList.setAll(arrayList);
            listView.getItems().clear();
            listView.setItems(observableList);
        }
        
        if(sort) listView.getItems().sort(listViewComparator);
        btnSort.setDisable(this.sampleVOs.isEmpty());
        
        listView.refresh();
    }
    
    Comparator<String> listViewComparator = new Comparator<String>() {
        @Override
        public int compare(String key1, String key2) {
            int index1 = sampleVOs.get(key1).noteID;
            int index2 = sampleVOs.get(key2).noteID;
            if(index1 < index2)
                return -1;
            else if(index1 > index2)
                return 1;
            else 
                return 0;
        }
    };
    
    Comparator<SampleVO> sampleVOsComparator = new Comparator<SampleVO>() {
        @Override
        public int compare(SampleVO key1, SampleVO key2) {
            int index1 = key1.noteID;
            int index2 = key2.noteID;
            if(index1 < index2)
                return -1;
            else if(index1 > index2)
                return 1;
            else 
                return 0;
        }
    };
    
    @FXML
    private void handleSortButtonClicked(MouseEvent event) {
        refreshListView(false, true);
    }
    
    private void updateLabelSliderValue(long value)
    {
        labelsSiderValue.setText(String.format(CStrings.PAN_VALUE, value));
    }
    
    private void applyStereoPan(float range, int minSize)
    {
        ArrayList<SampleVO> sortedSampleVOs = new ArrayList<>();
        for (Map.Entry<String, SampleVO> entry : this.sampleVOs.entrySet()) {
            sortedSampleVOs.add(entry.getValue());
        }
        Collections.sort(sortedSampleVOs, sampleVOsComparator);
        
        if(sortedSampleVOs.size() >= minSize)
        {
            float position = CConfig.PANORAMA_MIN_VALUE * range;
            float step = (position * -2) / (sortedSampleVOs.size() - 1);

            for(SampleVO sampleVO : sortedSampleVOs)
            {
                sampleVO.panorama = (int)position;
                position += step;
            }
        }
    }
    
    EventHandler<ActionEvent> onMacroPanorama = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem)event.getSource()).getText(), CStrings.MENUES_PAN);
            switch(itemIndex) {
                case 0: applyStereoPan(1.0f,  2); break;
                case 1: applyStereoPan(0.75f, 2); break;
                case 2: applyStereoPan(0.50f, 2); break;
                case 3: applyStereoPan(0.25f, 2); break;
                case 4: applyStereoPan(0.0f,  1); break;
            }
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onMacroGroup = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem)event.getSource()).getText(), CGroups.GROUPS_NAMES);
            for(Map.Entry<String, SampleVO> entry : sampleVOs.entrySet())
                entry.getValue().groupID = itemIndex;
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onMacroDynamic = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem)event.getSource()).getText(), CStrings.ENABLE_DISABLE);
            for(Map.Entry<String, SampleVO> entry : sampleVOs.entrySet())
                entry.getValue().dynamic = (itemIndex == 0);
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onMacroDisableNoteOff = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem)event.getSource()).getText(), CStrings.ENABLE_DISABLE);
            for(Map.Entry<String, SampleVO> entry : sampleVOs.entrySet())
                entry.getValue().disableNoteOff = (itemIndex == 0);
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onMacroLoop = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int itemIndex = StringUtils.getIndexOfStringInArray(((MenuItem)event.getSource()).getText(), CStrings.ENABLE_DISABLE);
            for(Map.Entry<String, SampleVO> entry : sampleVOs.entrySet())
                if(entry.getValue().loop != null)
                    entry.getValue().loopEnabled = (itemIndex == 0);
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onMacroNote = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            String text = ((MenuItem)event.getSource()).getText();
            int pitch1Index = CStrings.MENUES_NOTES_SHIFT_SEMITONES.length / 2;
            int pitch = StringUtils.getIndexOfStringInArray(text, CStrings.MENUES_NOTES_SHIFT_SEMITONES) - pitch1Index;
            
            if(pitch >= 0) pitch++;
            
            if(pitch < -CNotes.SEMITONES_IN_OCTAVE_NUM)
                pitch = (-CNotes.SEMITONES_IN_OCTAVE_NUM - pitch + 1) * -CNotes.SEMITONES_IN_OCTAVE_NUM;
            else if(pitch > CNotes.SEMITONES_IN_OCTAVE_NUM)
                pitch = (pitch - CNotes.SEMITONES_IN_OCTAVE_NUM + 1) * CNotes.SEMITONES_IN_OCTAVE_NUM;
            
            for(Map.Entry<String, SampleVO> entry : sampleVOs.entrySet()) {
                entry.getValue().noteID += pitch;
                if(entry.getValue().noteID < 0) 
                    entry.getValue().noteID = 0;
                else if(entry.getValue().noteID >= CNotes.NOTES_NAMES.length)
                    entry.getValue().noteID = (CNotes.NOTES_NAMES.length - 1);  
            }
            refreshListView(false, false);
        }
    };
    
    EventHandler<ActionEvent> onPitchRadioItemClicked = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            String text = ((RadioMenuItem)event.getSource()).getText();
            int pitch0Index = CStrings.MENUES_PITCH_SEMITONES.length / 2;
            int pitch = StringUtils.getIndexOfStringInArray(text, CStrings.MENUES_PITCH_SEMITONES) - pitch0Index;
            
            if(pitch < -CNotes.SEMITONES_IN_OCTAVE_NUM)
                pitch = (-CNotes.SEMITONES_IN_OCTAVE_NUM - pitch + 1) * -CNotes.SEMITONES_IN_OCTAVE_NUM;
            else if(pitch > CNotes.SEMITONES_IN_OCTAVE_NUM)
                pitch = (pitch - CNotes.SEMITONES_IN_OCTAVE_NUM + 1) * CNotes.SEMITONES_IN_OCTAVE_NUM;

            optionsVO.pitch = pitch;
        }
    };
    
    EventHandler<ActionEvent> onPitchStepRadioItemClicked = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            String text = ((RadioMenuItem)event.getSource()).getText();
            int step = StringUtils.getIndexOfStringInArray(text, CStrings.MENUES_PITCH_STEP_SEMITONES) + 1;
            if(step >= CNotes.SEMITONES_IN_OCTAVE_NUM) step = ((step % CNotes.SEMITONES_IN_OCTAVE_NUM) + 1) * CNotes.SEMITONES_IN_OCTAVE_NUM;
            optionsVO.pitchStep = step;
        }
    };
    
    ChangeListener<String> chbFilterChangeListener = new ChangeListener<String>(){
        @Override public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            setFiltersValues();
        }
    };
    
    ChangeListener<String> cbNoteChangeListener = new ChangeListener<String>(){
        @Override public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            optionsVO.noteID = CNotes.getIndexOfNote(newValue);
        }
    };
    
    ChangeListener<String> cbGroupChangeListener = new ChangeListener<String>(){
        @Override public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            optionsVO.groupID = CGroups.getIndexOfGroup(newValue);
        }
    };
    
    ChangeListener<Boolean> chbDynamicChangeListener = new ChangeListener<Boolean>(){
        @Override public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            optionsVO.isDynamic = newValue;
        }
    }; 
    
    ChangeListener<Boolean> chbDisableNoteOffChangeListener = new ChangeListener<Boolean>(){
        @Override public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            optionsVO.playThrough = newValue;
        }
    }; 
    
    ChangeListener<Boolean> chbLoopChangeListener = new ChangeListener<Boolean>(){
        @Override public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            optionsVO.loopEnabled = newValue;
        }
    };
    
    ChangeListener<Boolean> chbStereoChangeListener = new ChangeListener<Boolean>(){
        @Override public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            optionsVO.stereo = newValue;
        }
    };
    
    ChangeListener<Number> sliderPanChangeListener = new ChangeListener<Number>(){
        @Override public void changed(ObservableValue<? extends Number> selected, Number oldValue, Number newValue) {
            optionsVO.panorama = newValue.longValue();
            updateLabelSliderValue(newValue.longValue());
        }
    };

    @Override
    public void onSampleListCellDelete(String id) {
        sendToView(ON_SAMPLE_DELETE, id);
    }
    
    @Override
    public void onSampleListCellPlay(SampleVO sampleVO, double position) {
        sendToView(ON_SAMPLE_PLAY, new PlayEventVO(sampleVO, position));
    }
    
    @Override
    public void onSampleListCellFileDragged(SampleVO sampleVO, DragEvent event) {
        sendToView(ON_SAMPLE_DRAG, new SampleDragEventVO(sampleVO, event));
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
