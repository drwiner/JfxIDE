package tmw.me.com.ide;

import com.jfoenix.controls.JFXCheckBox;
import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import tmw.me.com.Resources;
import tmw.me.com.betterfx.Console;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.fileTreeView.FileTreeView;
import tmw.me.com.ide.images.Images;
import tmw.me.com.ide.tools.builders.tooltip.ToolTipBuilder;
import tmw.me.com.ide.tools.tabPane.ComponentTab;
import tmw.me.com.ide.tools.tabPane.ComponentTabPane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is the container that puts together all the different Components.
 * <p>For more information on particular components see:</p>
 * <ul>
 *     <li>File View: {@link FileTreeView}</li>
 *     <li>Text Editor: {@link IntegratedTextEditor}</li>
 *     <li>Language Powers: {@link LanguageSupport}</li>
 * </ul>
 */
public class Ide extends AnchorPane {

    public static final Image WINDOW_ICON = new Image(Images.get("icon.png"));
    public static final String STYLE_SHEET = Resources.getExternalForm("ide/styles/main.css");

    private final ComponentTabPane tabPane = new ComponentTabPane();
    private final SplitPane tabPanesHorizontal = new SplitPane(tabPane);

    private final SVGPath playSvg = new SVGPath();
    private final Button playButton = new Button("", playSvg);
    private final ChoiceBox<ComponentTab<IntegratedTextEditor>> runSelector = new ChoiceBox<>();
    private final JFXCheckBox autoSelect = new JFXCheckBox();
    private final HBox topBox = new HBox(autoSelect, runSelector, playButton);
    private final Button runTabButton = new Button("Run");
    private final Button projectTabButton = new Button("P\nr\no\nj\ne\nc\nt");
    private final HBox bottomBox = new HBox(runTabButton);
    private final VBox sideBox = new VBox(projectTabButton);

    private final AnchorPane leftTab = new AnchorPane();
    private final AnchorPane rightTab = new AnchorPane(tabPanesHorizontal, topBox);

    private final Console runConsole = new Console();
    private final VBox consoleAnchor = new VBox();
    private final SplitPane horizontalSplitPane = new SplitPane(leftTab, rightTab);

    private final AnchorPane bottomTab = new AnchorPane();
    private final AnchorPane topTab = new AnchorPane(horizontalSplitPane);

    private final SplitPane verticalSplitPane = new SplitPane(topTab, bottomTab);

    private final MenuBar menuBar = new MenuBar();

    private final Pane emptyPopupPane = new Pane();
    private final VBox insidePopup = new VBox();
    private final BorderPane popupPane = new BorderPane();

    private final Label prompt = new Label();
    private final TextField inputBox = new TextField();
    private final HBox textInputBox = new HBox(prompt, inputBox);

    private final Label confirmText = new Label();
    private final Button confirm = new Button("Confirm");
    private final HBox confirmBox = new HBox(confirmText, confirm);

    private final AnchorPane projectViewAnchorPane = new AnchorPane();
    private FileTreeView projectView;

    private final VBox notificationPane = new VBox();

    public Ide() {

        SplitPane.setResizableWithParent(bottomTab, false);
        SplitPane.setResizableWithParent(leftTab, false);

        // Booleans
        runSelector.setDisable(true);
        projectTabButton.setVisible(false);
        popupPane.setVisible(false);
        playSvg.setPickOnBounds(true);
        consoleAnchor.setFillWidth(true);

        // Alignments
        topBox.setAlignment(Pos.CENTER_LEFT);
        insidePopup.setAlignment(Pos.CENTER);
        projectTabButton.setTextAlignment(TextAlignment.CENTER);

        // Style
        popupPane.getStyleClass().add("popup-shower");
        playSvg.getStyleClass().add("circle-highlight-background");
        playButton.getStyleClass().add("transparent-background");
        runSelector.getStyleClass().add("run-selector");
        textInputBox.getStyleClass().add("popup-item");
        confirmBox.getStyleClass().add("popup-item");
        autoSelect.getStyleClass().add("auto-select");
        tabPanesHorizontal.getStyleClass().add("dark-split-pane");

        confirmText.getStyleClass().addAll("white-text", "xl-title");
        confirm.getStyleClass().addAll("white-text", "l-title");

        prompt.getStyleClass().addAll("white-text", "xl-title");
        inputBox.getStyleClass().addAll("white-text", "l-title");

        this.getStyleClass().add("ide");
        this.getStylesheets().add(STYLE_SHEET);

        // Colors
        playSvg.setFill(Color.LIGHTGREEN);
        runConsole.setMainBg(Color.valueOf("#1c2532"));

        // Fonts
        runConsole.setFont(new Font("Terminal", 16));

        // Void method calls
        runConsole.disableInput();
        makeTabButton(runTabButton, consoleAnchor, bottomTab, verticalSplitPane, 0.8);
        makeTabButton(projectTabButton, projectViewAnchorPane, leftTab, horizontalSplitPane, 0.2);

        // Size
        bottomTab.setMaxHeight(0);
        leftTab.setMaxWidth(0);
        emptyPopupPane.setMinHeight(250);

        // Other Values
        playSvg.setContent("M 0 0 L 0 18.9 L 13.5 9.45 L 0 0");
        verticalSplitPane.setOrientation(Orientation.VERTICAL);
        projectTabButton.setLineSpacing(-5);
        topBox.setFillHeight(true);
        topBox.setSpacing(8);
        confirmBox.setSpacing(10);
        textInputBox.setSpacing(10);
        tabPane.setHorizontal(tabPanesHorizontal);

        // Children
        consoleAnchor.getChildren().add(runConsole);
        popupPane.setCenter(insidePopup);
        this.getChildren().addAll(verticalSplitPane, bottomBox, sideBox, menuBar, notificationPane, popupPane);

        // Layout
        AnchorPane.setTopAnchor(notificationPane, 13D);
        AnchorPane.setRightAnchor(notificationPane, 13D);

        AnchorPane.setTopAnchor(tabPanesHorizontal, 0D);
        AnchorPane.setBottomAnchor(tabPanesHorizontal, 0D);
        AnchorPane.setRightAnchor(tabPanesHorizontal, 0D);
        AnchorPane.setLeftAnchor(tabPanesHorizontal, 0D);

        AnchorPane.setTopAnchor(verticalSplitPane, 26D);
        AnchorPane.setBottomAnchor(verticalSplitPane, 35D);
        AnchorPane.setRightAnchor(verticalSplitPane, 8D);
        AnchorPane.setLeftAnchor(verticalSplitPane, 14D);
        AnchorPane.setTopAnchor(horizontalSplitPane, 0D);
        AnchorPane.setBottomAnchor(horizontalSplitPane, 0D);
        AnchorPane.setRightAnchor(horizontalSplitPane, 0D);
        AnchorPane.setLeftAnchor(horizontalSplitPane, 0D);

        AnchorPane.setTopAnchor(topBox, 2D);
        AnchorPane.setRightAnchor(topBox, 2D);
        AnchorPane.setBottomAnchor(bottomBox, 11D);
        AnchorPane.setLeftAnchor(bottomBox, 14D);

        AnchorPane.setTopAnchor(sideBox, 26D);
        AnchorPane.setLeftAnchor(sideBox, 2D);

        AnchorPane.setTopAnchor(popupPane, 0D);
        AnchorPane.setRightAnchor(popupPane, 0D);
        AnchorPane.setBottomAnchor(popupPane, 0D);
        AnchorPane.setLeftAnchor(popupPane, 0D);

        AnchorPane.setTopAnchor(menuBar, 0D);
        AnchorPane.setLeftAnchor(menuBar, 0D);
        AnchorPane.setRightAnchor(menuBar, 0D);

        // Listeners
        heightProperty().addListener((observableValue, number, t1) -> emptyPopupPane.setMinHeight(t1.doubleValue() / 5));
        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, tab, t1) -> {
            if (getScene() != null && t1 instanceof ComponentTab && getScene().getWindow() instanceof Stage) {
                ComponentTab<?> componentTab = (ComponentTab<?>) t1;
                Stage stage = (Stage) getScene().getWindow();
                if (stage.getTitle() != null) {
                    if (!stage.getTitle().contains("-")) {
                        stage.setTitle("Untitled - ");
                    }
                    stage.setTitle(stage.getTitle().split("-")[0] + "- " + componentTab.getLabel().getText());
                }
            }
            if (t1 instanceof ComponentTab && runSelector.isDisable()) {
                runSelector.getSelectionModel().select((ComponentTab<IntegratedTextEditor>) t1);
            }
        });
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while (change.next()) {
                for (Tab componentTab : change.getRemoved()) {
                    assert componentTab instanceof ComponentTab;
                    runSelector.getItems().remove(componentTab);
                }
                for (Tab componentTab : change.getAddedSubList()) {
                    assert componentTab instanceof ComponentTab;
                    runSelector.getItems().add((ComponentTab<IntegratedTextEditor>) componentTab);
                }
            }
        });
        bottomTab.getChildren().addListener((ListChangeListener<Node>) change -> {
            if (bottomTab.getChildren().isEmpty()) {
                bottomTab.setMaxHeight(0);
            } else {
                bottomTab.setMaxHeight(Integer.MAX_VALUE);
            }
        });
        leftTab.getChildren().addListener((ListChangeListener<Node>) change -> {
            if (leftTab.getChildren().isEmpty()) {
                leftTab.setMaxWidth(0);
            } else {
                leftTab.setMaxWidth(Integer.MAX_VALUE);
            }
        });
        sceneProperty().addListener((observableValue, scene, t1) -> {
            if (t1 != null) {
                t1.windowProperty().addListener((observableValue1, window, t11) -> {
                    if (t11 instanceof Stage) {
                        ((Stage) t11).getIcons().add(WINDOW_ICON);
                    }
                });
            }
        });
        verticalSplitPane.getItems().addListener((ListChangeListener<Node>) change -> {
            System.out.println(verticalSplitPane.getItems());
            horizontalSplitPane.toFront();
            System.out.println(verticalSplitPane.getItems());
        });

        // Events
        runSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ComponentTab<IntegratedTextEditor> integratedTextEditorComponentTab) {
                return integratedTextEditorComponentTab != null ? integratedTextEditorComponentTab.getLabel().getText() : "";
            }

            @Override
            public ComponentTab<IntegratedTextEditor> fromString(String s) {
                return null;
            }
        });
        playSvg.setOnMousePressed(mouseEvent -> {
            ComponentTab<IntegratedTextEditor> tab = runSelector.getSelectionModel().getSelectedItem();
            tab.getValue().getLanguage().runCalled(tab.getValue(), this);
        });
        autoSelect.setOnAction(actionEvent -> {
            runSelector.setDisable(!autoSelect.isSelected());
            if (!autoSelect.isSelected()) {
                runSelector.getSelectionModel().select((ComponentTab<IntegratedTextEditor>) tabPane.getSelectionModel().getSelectedItem());
            }
        });
        rightTab.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().getFiles() != null && !dragEvent.getDragboard().getFiles().isEmpty()) {
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        rightTab.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().getFiles() != null && !dragEvent.getDragboard().getFiles().isEmpty()) {
                List<File> files = dragEvent.getDragboard().getFiles();
                for (File file : files) {
                    if (file != null) {
                        if (file.isDirectory()) {
                            loadFile(file);
                            break;
                        } else {
                            tabPane.getTabs().add(getNewEditorTab(file));
                        }
                    }
                }
            }
        });


        // Context Menu
        Menu fileMenu = new Menu("File");
        Menu newMenu = new Menu("New");
        MenuItem newProject = new MenuItem("New Project");
        MenuItem newFile = new MenuItem("New File");
        MenuItem newAddon = new MenuItem("New Addon");
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        Menu openMenu = new Menu("Open");
        MenuItem openFile = new MenuItem("Open File");
        MenuItem openFolder = new MenuItem("Open Folder");
        MenuItem openProject = new MenuItem("Open Project");
        MenuItem openAddon = new MenuItem("Open Addon");
        openMenu.getItems().addAll(openProject, openFile, openFolder, openAddon);
        newMenu.getItems().addAll(newProject, newFile, newAddon);
        fileMenu.getItems().addAll(newMenu, openMenu);

        Menu tabMenu = new Menu("Tab");
        MenuItem save = new MenuItem("Save");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        MenuItem close = new MenuItem("Close");
        close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
        MenuItem openInNewWindow = new MenuItem("Open in New Window");
        openInNewWindow.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        tabMenu.getItems().addAll(save, close, openInNewWindow);

        menuBar.getMenus().addAll(fileMenu, tabMenu);
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> tabMenu.setDisable(change.getList().isEmpty()));
        // Menu event handling
        newProject.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new Ide()));
            stage.setTitle("Untitled Project - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
            stage.show();
            Window thisWindow = this.getScene().getWindow();
            stage.setWidth(thisWindow.getWidth());
            stage.setHeight(thisWindow.getHeight());
            if (thisWindow instanceof Stage) {
                stage.setMaximized(((Stage) thisWindow).isMaximized());
            }
        });
        newFile.setOnAction(actionEvent -> {
            ComponentTab<IntegratedTextEditor> newTab = getNewEditorTab(null);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        });
        newAddon.setOnAction(actionEvent -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(this.getScene().getWindow());
            if (file != null) {
                File[] listFiles = file.listFiles();
                if (listFiles == null || listFiles.length == 0 || LanguageAddon.verifyDir(file)) {
                    try {
                        LanguageAddon.createAtDir(file);
                        loadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        openAddon.setOnAction(newAddon.getOnAction());
        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                loadFile(file);
            }
        });
        openFolder.setOnAction(actionEvent -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(this.getScene().getWindow());
            if (file != null) {
                loadFile(file);
            }
        });
        openProject.setOnAction(actionEvent -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(this.getScene().getWindow());
            if (file != null) {
                Stage stage = new Stage();
                Ide newIde = new Ide();
                stage.setScene(new Scene(newIde));
                stage.setTitle(file.isDirectory() ? file.getName() : "Untitled Project - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
                stage.show();
                Window thisWindow = this.getScene().getWindow();
                stage.setWidth(thisWindow.getWidth());
                stage.setHeight(thisWindow.getHeight());
                if (thisWindow instanceof Stage) {
                    stage.setMaximized(((Stage) thisWindow).isMaximized());
                }
                newIde.loadFile(file);
            }
        });

        save.setOnAction(actionEvent -> {
            ComponentTab<?> selectedTab = tabPane.getSelectedTab();
            if (selectedTab != null && selectedTab.getValue() instanceof IntegratedTextEditor && selectedTab.getFile() != null && selectedTab.getFile().exists()) {
                IntegratedTextEditor selectedTextEditor = (IntegratedTextEditor) selectedTab.getValue();
                try {
                    FileWriter fileWriter = new FileWriter(selectedTab.getFile());
                    fileWriter.write(selectedTextEditor.getText());
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        close.setOnAction(actionEvent -> {
            ComponentTab<?> componentTab = tabPane.getSelectedTab();
            if (componentTab != null && componentTab.getFile() != null) {
                tabPane.getTabs().remove(componentTab);
            }
        });
        openInNewWindow.setOnAction(actionEvent -> {
            ComponentTab<?> componentTab = tabPane.getSelectedTab();
            if (componentTab != null) {
                tabPane.getTabs().remove(componentTab);
                Ide ide = new Ide();
                Stage stage = new Stage();
                stage.setScene(new Scene(ide));
                stage.show();
                stage.setHeight(500);
                stage.setWidth(800);
                if (componentTab.getFile() != null) {
                    ide.loadFile(componentTab.getFile());
                } else {
                    String text = ((IntegratedTextEditor) componentTab.getValue()).getText();
                    IntegratedTextEditor newIntegratedTextEditor = new IntegratedTextEditor();
                    newIntegratedTextEditor.replaceText(text);
                    ComponentTab<IntegratedTextEditor> dupedTab = new ComponentTab<>(componentTab.getLabel().getText(), newIntegratedTextEditor);
                    ide.getTabPane().getTabs().add(dupedTab);
                }
            }
        });


        // Tooltips
        autoSelect.setTooltip(
                ToolTipBuilder.create().setHeader("Toggle Auto Select").setMainText("Toggles whether or not the run tab selector\nautomatically switches to the selected tab.").build()
        );
        playButton.setTooltip(
                ToolTipBuilder.create().setHeader("Run Button").setMainText("Runs the tab selected in the run tab selector").build()
        );
        runSelector.setTooltip(
                ToolTipBuilder.create().setHeader("Run Tab Selector").setMainText("The selected tab will be ran\nwhen the Run Button is pressed.").build()
        );
        runTabButton.setTooltip(
                ToolTipBuilder.create().setHeader("Run Tab").setMainText("Toggles the view of the Run Tab Console.").build()
        );
        projectTabButton.setTooltip(
                ToolTipBuilder.create().setHeader("Project Tab").setMainText("Toggles the view of the File Tree.").build()
        );


    }

    public Ide(File file) {
        this();
        if (getScene() != null) {
            if (getScene().getWindow() != null) {
                loadFile(file);
            } else {
                getScene().windowProperty().addListener((observableValue1, window, t11) -> {
                    if (t11 != null && window == null) {
                        loadFile(file);
                    }
                });
            }
        } else {
            sceneProperty().addListener((observableValue, scene, t1) -> {
                if (t1 != null && scene == null) {
                    t1.windowProperty().addListener((observableValue1, window, t11) -> {
                        if (t11 != null && window == null) {
                            loadFile(file);
                        }
                    });
                }
            });
        }
    }

    public void loadFile(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                projectView = new FileTreeView(file, this);
            } else {
                projectView = new FileTreeView(file.getParentFile(), this);
            }
            if (this.getScene().getWindow() instanceof Stage) {
                ((Stage) getScene().getWindow()).setTitle(projectView.getFileRoot().getName() + " - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
            }
            projectTabButton.setVisible(true);
            projectViewAnchorPane.getChildren().add(projectView);
            AnchorPane.setTopAnchor(projectView, 0D);
            AnchorPane.setBottomAnchor(projectView, 0D);
            AnchorPane.setRightAnchor(projectView, 0D);
            AnchorPane.setLeftAnchor(projectView, 0D);
            if (!file.isDirectory()) {
                Tab newTab = getNewEditorTab(file);
                tabPane.getTabs().add(newTab);
                tabPane.getSelectionModel().select(newTab);
            }
        }
    }

    public HBox getTopBox() {
        return topBox;
    }

    public ComponentTabPane getTabPane() {
        return tabPane;
    }

    public SplitPane getVerticalSplitPane() {
        return verticalSplitPane;
    }

    public HBox getBottomBox() {
        return bottomBox;
    }

    public void println(Object print) {
        print(print + "\n");
    }

    public void print(Object print) {
        runConsole.addText("" + print, false);
    }

    private void makeTabButton(Button button, Region putInTab, AnchorPane tab, SplitPane divider, double newDividerSpot) {
        tab.getStyleClass().add("darker-background");
        tab.setMinHeight(0);
        button.setMnemonicParsing(true);
        button.setOnAction(actionEvent -> {
            if (button.getAccessibleText() != null && button.getAccessibleText().equals("ACTIVATED")) {
                button.setAccessibleText("");
                tab.getChildren().remove(putInTab);
                button.getStyleClass().remove("active-button");
            } else {
                button.setAccessibleText("ACTIVATED");
                tab.getChildren().clear();
                tab.getChildren().add(putInTab);
                AnchorPane.setTopAnchor(putInTab, 0D);
                AnchorPane.setBottomAnchor(putInTab, 0D);
                AnchorPane.setRightAnchor(putInTab, 0D);
                AnchorPane.setLeftAnchor(putInTab, 0D);
                divider.setDividerPositions(newDividerSpot);
                button.getStyleClass().add("active-button");
            }
        });
    }

    public void showPopupForText(String prompt, String defaultText, Consumer<String> gotten) {
        this.prompt.setText(prompt);
        this.inputBox.setText(defaultText);
        this.inputBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                gotten.accept(this.inputBox.getText());
                hidePopup();
            }
        });
        insidePopup.getChildren().clear();
        insidePopup.getChildren().add(textInputBox);
        textInputBox.setAlignment(Pos.CENTER);
        showPopup();
        this.inputBox.requestFocus();
    }

    public void showConfirmation(String confirmText, Consumer<Boolean> gotten) {
        showConfirmation(confirmText, "Confirm", gotten);
    }

    public void showConfirmation(String confirmText, String prompt, Consumer<Boolean> gotten) {
        this.confirmText.setText(confirmText);
        this.confirm.setText(prompt);
        confirm.setOnAction(actionEvent -> {
            gotten.accept(true);
            hidePopup();
        });
        insidePopup.getChildren().clear();
        insidePopup.getChildren().add(confirmBox);
        confirmBox.setAlignment(Pos.CENTER);
        showPopup();
        this.confirm.requestFocus();
    }

    public void showPopup() {
        popupPane.setOpacity(0);
        popupPane.setVisible(true);
        insidePopup.getChildren().add(emptyPopupPane);
        FadeTransition fadeIn = new FadeTransition(new Duration(200), popupPane);
        fadeIn.setToValue(1);
        fadeIn.play();
        popupPane.setOnMousePressed(mouseEvent -> hidePopup());
    }

    public void hidePopup() {
        FadeTransition fadeOut = new FadeTransition(new Duration(100), popupPane);
        fadeOut.setToValue(0);
        fadeOut.play();
        fadeOut.setOnFinished(actionEvent -> popupPane.setVisible(false));
    }

    public static ComponentTab<IntegratedTextEditor> getNewEditorTab(File file) {
        String fileName = file != null && !file.isDirectory() ? file.getName() : "Untitled";
        IntegratedTextEditor integratedTextEditor = new IntegratedTextEditor(LanguageSupport.getLanguageFromFile(file));

        if (file != null) {
            String result = "";
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] fileContent = new byte[(int) file.length()];
                fileInputStream.read(fileContent);
                result = new String(fileContent);
            } catch (IOException e) {
                System.err.println("Invalid File: " + file.getPath());
            }
            integratedTextEditor.replaceText(result);
        }
        ComponentTab<IntegratedTextEditor> componentTab = new ComponentTab<>(fileName, integratedTextEditor);
        componentTab.setFile(file);
        componentTab.setMainNode(integratedTextEditor);
        return componentTab;
    }

    public static String readFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileContent = new byte[(int) file.length()];
            fileInputStream.read(fileContent);
            return new String(fileContent);
        } catch (IOException e) {
            System.err.println("Invalid File: " + file.getPath());
        }
        return "Wasn't able to read file: " + file;
    }

    public SVGPath getPlaySvg() {
        return playSvg;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public ChoiceBox<ComponentTab<IntegratedTextEditor>> getRunSelector() {
        return runSelector;
    }

    public JFXCheckBox getAutoSelect() {
        return autoSelect;
    }

    public Button getRunTabButton() {
        return runTabButton;
    }

    public Button getProjectTabButton() {
        return projectTabButton;
    }

    public VBox getSideBox() {
        return sideBox;
    }

    public AnchorPane getLeftTab() {
        return leftTab;
    }

    public AnchorPane getRightTab() {
        return rightTab;
    }

    public Console getRunConsole() {
        return runConsole;
    }

    public VBox getConsoleAnchor() {
        return consoleAnchor;
    }

    public SplitPane getHorizontalSplitPane() {
        return horizontalSplitPane;
    }

    public AnchorPane getBottomTab() {
        return bottomTab;
    }

    public AnchorPane getTopTab() {
        return topTab;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Pane getEmptyPopupPane() {
        return emptyPopupPane;
    }

    public VBox getInsidePopup() {
        return insidePopup;
    }

    public BorderPane getPopupPane() {
        return popupPane;
    }

    public Label getPrompt() {
        return prompt;
    }

    public TextField getInputBox() {
        return inputBox;
    }

    public HBox getTextInputBox() {
        return textInputBox;
    }

    public Label getConfirmText() {
        return confirmText;
    }

    public Button getConfirm() {
        return confirm;
    }

    public HBox getConfirmBox() {
        return confirmBox;
    }

    public AnchorPane getProjectViewAnchorPane() {
        return projectViewAnchorPane;
    }

    public FileTreeView getProjectView() {
        return projectView;
    }

    public VBox getNotificationPane() {
        return notificationPane;
    }

    public void addEditorTab() {
        addEditorTab(null);
    }

    public void addEditorTab(File file) {
        tabPane.getTabs().add(getNewEditorTab(file));
    }
}