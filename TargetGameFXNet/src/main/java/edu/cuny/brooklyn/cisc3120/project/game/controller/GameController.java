package edu.cuny.brooklyn.cisc3120.project.game.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cuny.brooklyn.cisc3120.project.game.TargetGameApp;
import edu.cuny.brooklyn.cisc3120.project.game.model.DecisionWrapper;
import edu.cuny.brooklyn.cisc3120.project.game.model.TargetGame;
import edu.cuny.brooklyn.cisc3120.project.game.net.StatusBroadcaster;
import edu.cuny.brooklyn.cisc3120.project.game.utils.I18n;
import edu.cuny.brooklyn.cisc3120.project.game.model.Shot;
import edu.cuny.brooklyn.cisc3120.project.game.model.DecisionWrapper.UserDecision;
import edu.cuny.brooklyn.cisc3120.project.game.model.GameStatistics;
import edu.cuny.brooklyn.cisc3120.project.game.model.GameStatistics.StatNameValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class GameController {
    private final static Logger LOGGER = LoggerFactory.getLogger(GameController.class);
    
    private final static String APP_TITLE = "CISC 3120 Fall 2017: TargetGame";

	private static final Shot Shot = null;
    
    @FXML
    private Canvas targetCanvas;

    @FXML
    private TextField xAimedAtTextField;

    @FXML
    private TextField yAimedAtTextField;

    @FXML
    private Button fireWeaponButton;

    @FXML
    private ComboBox<Locale> lcComboBox;
    
    @FXML
    private TableView<StatNameValue> gameStatTableView;
    
    @FXML
    private TableColumn<StatNameValue, String> tableViewStatName;
    
    @FXML
    private TableColumn<StatNameValue, String> tableViewStatValue;
    
    @FXML
    private VBox playersOnLineVbox;
    
    @FXML
    ListView list ;
    
    
    private TargetGame targetGame = new TargetGame();
    
    private Stage stage;
    
    private StatusBroadcaster statusBroadCaster;
    
    /*
     * Added to the program
     */
    
    private int maximumGuessThreshold = 9;  // 10 when counting from 0
    
    private int numOfCurrentGuesses = 0;
    
    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            LOGGER.debug("User clicked the X button on the stage.");
            exitGame(event);
        });
    }
    
    @FXML
    void initialize() throws IOException, URISyntaxException {
        LOGGER.debug("Initializing GameController.");
        setWeaponDisable(true);
        initializeI18n();
        gameStatTableView.setVisible(false);
        playersOnLineVbox.setVisible(false);
        statusBroadCaster = new StatusBroadcaster();
        statusBroadCaster.start();
    }
    
    @FXML
    void fireTheWeapon(ActionEvent event) {
        LOGGER.debug("Weapon fired!");
        int shotX = Integer.parseInt(xAimedAtTextField.getText());
        int shotY = Integer.parseInt(yAimedAtTextField.getText());
        Shot shot = new Shot(shotX, shotY);
        processShotAction(targetGame, shot);
    }
    
    @FXML
    void exitGame(ActionEvent event) {
        LOGGER.debug("calling exitGame(ActionEvent event).");
        exitGame((Event)event);
    }

    @FXML
    void newGame(ActionEvent event) throws IOException {
        LOGGER.debug("started new game.");
        lcComboBox.setDisable(true); // don't allow users to change locale when a game is in session
        addTarget(targetGame, targetCanvas);
        setWeaponDisable(false);
        gameStatTableView.setVisible(true);
        gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList());
        tableViewStatName.setCellValueFactory(new PropertyValueFactory<StatNameValue, String>(StatNameValue.COLUMN_NAME_TITLE));
        tableViewStatValue.setCellValueFactory(new PropertyValueFactory<StatNameValue, String>(StatNameValue.COLUMN_VALUE_TITLE));
        gameStatTableView.getColumns().set(0,  tableViewStatName);
        gameStatTableView.getColumns().set(1,  tableViewStatValue);
        
        //Players Online Added , may need improvements
        playersOnLineVbox.setVisible(true);
        String msg = statusBroadCaster.getStatusMessage();
        ObservableList<String> iPaddress = FXCollections.observableArrayList(msg );
        list = new ListView<String>();
        list.setItems(iPaddress);
        playersOnLineVbox.setVgrow(list, Priority.ALWAYS);
        playersOnLineVbox.getChildren().addAll(list);
      
      
        targetGame.getGameStatistics().setNumOfTargetsMade(
        		targetGame.getGameStatistics().getNumOfTargetsMade()+1);
        
        gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList());
        
        
    }

    @FXML
    void openGame(ActionEvent event) throws IOException {
        LOGGER.debug("openning a saved game: not implemented yet");
        /*
         * Load game data from file
         * Search for the game file specified by the user
         * If the file is not found, prompt message stating that no file found
         * Else, load the statistics to the game
         * reads from the file until no more content
         * set values to corresponding areas
         * The format in which the file is to be loaded
         * (numOfTargetsShot -> numOfShotsFired -> numOfTargetsMade -> 
         * numOfRoundsWon -> numOfRoundsPlayed -> accuracy)
         */
        
        
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File saveFile = fileChooser.showOpenDialog(stage);
        
       Scanner copy = new Scanner(saveFile);
        String store = copy.nextLine();
        String[] staticsload = store.split("\t");
       
        /*(numOfTargetsShot -> numOfShotsFired -> numOfTargetsMade -> 
        * numOfRoundsWon -> numOfRoundsPlayed -> accuracy)
        */
        this.newGame(event);
       targetGame.getGameStatistics().setNumOfTargetsShot(Integer.parseInt(staticsload[0]));
       targetGame.getGameStatistics().setNumOfShotsFired(Integer.parseInt(staticsload[1]));
       targetGame.getGameStatistics().setNumOfTargetsMade(Integer.parseInt(staticsload[2]));
       targetGame.getGameStatistics().setNumOfRoundsWon(Integer.parseInt(staticsload[3]));        
       targetGame.getGameStatistics().setNumOfRoundsPlayed(Integer.parseInt(staticsload[4]));
       targetGame.getGameStatistics().setAccuracy(Double.parseDouble(staticsload[5]));  
        
       gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList());
        
    }

    @FXML
    void saveTheGame(ActionEvent event) throws FileNotFoundException, IOException  {
        LOGGER.debug("saving the game: not implemented yet");
        
        // Sei: implemented in TargetGmae, which is given by the source code
        targetGame.saveTheGame(stage);
           
    }
    
    private void exitGame(Event event) {
        LOGGER.debug("calling exitGame(Event event).");
        if (targetGame.isGameStateChanged()) {
            UserDecision decision = NotificationHelper.askUserDecision(new DecisionWrapper(UserDecision.CancelPendingAction));
           
            switch (decision) {
            case CancelPendingAction:
                event.consume();
                break;
            case DiscardGame:
                statusBroadCaster.close();
                Platform.exit();
                break;
            case SaveGame:
                try {
                    targetGame.saveTheGame(stage);
                    LOGGER.debug(String.format("Saved the game at %s.", targetGame.getTheGameFile().getPath()));
                    statusBroadCaster.close();
                    Platform.exit();
                } catch (FileNotFoundException e) {
                    LOGGER.error(String.format("Cannot found the file %s while saving the game.",
                            targetGame.getTheGameFile().getPath()), e);
                    NotificationHelper.showFileNotFound(targetGame.getTheGameFile().getPath());
                } catch (IOException e) {
                    LOGGER.error(String.format("Cannot write to the file %s while saving the game.",
                            targetGame.getTheGameFile().getPath()), e);
                    NotificationHelper.showWritingError(targetGame.getTheGameFile().getPath());
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("User decision's value (%s) is unexpected", decision));
            }
        } else {
            statusBroadCaster.close();
            Platform.exit();
        }       
    }
    
    private void addTarget(TargetGame game, Canvas canvas) {
        game.setNewTarget();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double cellWidth = width / game.getGameBoard().getWidth();
        double cellHeight = height / game.getGameBoard().getHeight();
        double xPos = cellWidth * game.getTarget().getX();
        double yPos = cellHeight * game.getTarget().getY();
        GraphicsContext gc = targetCanvas.getGraphicsContext2D();
        gc.setFill(game.getTarget().getColor());
        gc.fillRect(xPos, yPos, cellWidth, cellHeight);
    }
    
    private void processShotAction(TargetGame gameState, Shot shot) {
    	
    	
        if (gameState.getTarget().isTargetShot(shot)) {
            Alert alert = new Alert(AlertType.INFORMATION
                    , I18n.getBundle().getString("uShotTarget"), ButtonType.CLOSE);
            alert.setTitle(APP_TITLE + ":" + I18n.getBundle().getString("targetShot"));
            alert.setHeaderText(I18n.getBundle().getString("greatShot"));
            alert.showAndWait();
            clearTarget();
            addTarget(gameState, targetCanvas);
            
            /*
             * Game statistic to be updated here
             */
            numOfCurrentGuesses = 0; //If target is hit, reset the numOfCurrentGuess to 0
            
            targetGame.getGameStatistics().setNumOfTargetsShot(
            		targetGame.getGameStatistics().getNumOfTargetsShot()+1);  //Updates the numOfTargetsShot
            
            targetGame.getGameStatistics().setNumOfShotsFired(
            		targetGame.getGameStatistics().getNumOfShotsFired()+1);   //Updates the numOfShotsFired
            
            targetGame.getGameStatistics().setNumOfTargetsMade(
            		targetGame.getGameStatistics().getNumOfTargetsMade()+1);   //Updates the numOfTargetsMade
            
            targetGame.getGameStatistics().setNumOfRoundsWon(
            		targetGame.getGameStatistics().getNumOfRoundsWon()+1);   //Updates the numOfRoundsWon
            
            targetGame.getGameStatistics().setNumOfRoundsPlayed(
            		targetGame.getGameStatistics().getNumOfRoundsPlayed()+1);   //Updates the numOfRoundsPlayed
            
            targetGame.getGameStatistics().updateAccuracy();   //Updates the accuracy
            
            gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList());   //Adds changes made to tableview

            
        } else {
            
            
            if(numOfCurrentGuesses == maximumGuessThreshold) {

            	//Alert message indicates when player loses
            	Alert alert = new Alert(AlertType.INFORMATION
                        , "Try again", ButtonType.CLOSE);
                alert.setTitle(APP_TITLE + ": Next round");
                alert.setHeaderText("You lose this round");                
                alert.showAndWait();
            	
                clearTarget();   //Clears target 
                addTarget(gameState, targetCanvas);   //Adds a new target
            	
            	numOfCurrentGuesses = 0;   //When the maximumGuessThreshold is reached, reset numOfCurrentGuesses to 0
            	
            	targetGame.getGameStatistics().setNumOfShotsFired(
                		targetGame.getGameStatistics().getNumOfShotsFired()+1);   //Updates the numOfShotsFired
            	
            	targetGame.getGameStatistics().setNumOfTargetsMade(
                		targetGame.getGameStatistics().getNumOfTargetsMade()+1);   //Updates the numOfTargetsMade
            	
            	targetGame.getGameStatistics().setNumOfRoundsPlayed(
            		targetGame.getGameStatistics().getNumOfRoundsPlayed()+1);   //Updates the numOfRoundsPlayed
            	
                targetGame.getGameStatistics().updateAccuracy();   //Updates the accuracy
            	
                gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList()); //Adds changes made to tableview
            }
            else {
            	Alert alert = new Alert(AlertType.INFORMATION
                    , I18n.getBundle().getString("uMissedTarget"), ButtonType.CLOSE);
            alert.setTitle(APP_TITLE + ":" + I18n.getBundle().getString("targetMissed"));
            alert.setHeaderText(I18n.getBundle().getString("lousyShooter"));                
            alert.showAndWait();

            numOfCurrentGuesses++;   //If shot misses, increment the numOfCurrentGuesses by 1
            
            targetGame.getGameStatistics().setNumOfShotsFired(
            		targetGame.getGameStatistics().getNumOfShotsFired()+1);   //Updates the numOfShotsFired
            
            targetGame.getGameStatistics().updateAccuracy();   //Updates the accuracy
            
            gameStatTableView.setItems(targetGame.getGameStatistics().toObservableList()); //Adds changes made to tableview
            }
            
           
        }
   }
    
    private void clearTarget() {
        double width = targetCanvas.getWidth();
        double height = targetCanvas.getHeight();
        double cellWidth = width / targetGame.getGameBoard().getWidth();
        double cellHeight = height / targetGame.getGameBoard().getHeight();
        double xPos = cellWidth * targetGame.getTarget().getX();
        double yPos = cellHeight * targetGame.getTarget().getY();
        
        GraphicsContext gc = targetCanvas.getGraphicsContext2D();
        gc.clearRect(xPos, yPos, cellWidth, cellHeight);
        
    }
    
    private void setWeaponDisable(boolean disabled) {
        xAimedAtTextField.setDisable(disabled);
        yAimedAtTextField.setDisable(disabled);
        fireWeaponButton.setDisable(disabled);
    }
    
    private void initializeI18n() throws IOException, URISyntaxException {
        List<Locale> lcList = I18n.getSupportedLocale();
        lcComboBox.getItems().addAll(lcList);
        Callback<ListView<Locale>, ListCell<Locale>> lcCellFactory = 
                new Callback<ListView<Locale>, ListCell<Locale>>() {

            @Override
            public ListCell<Locale> call(ListView<Locale> lv) {
                return new ListCell<Locale>() {
                    @Override
                    protected void updateItem(Locale lc, boolean empty) {
                        super.updateItem(lc, empty);
                        if (lc == null || empty) {
                            setText("");
                        } else {
                            setText(I18n.getDisplayLC(lc));
                        }
                    }
                };
            }
        };
        lcComboBox.setValue(I18n.getSelectedLocale());
        lcComboBox.setConverter(new StringConverter<Locale>() {

            @Override
            public Locale fromString(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String toString(Locale lc) {
                return I18n.getDisplayLC(lc);
            }
        });
        lcComboBox.setCellFactory(lcCellFactory);
        lcComboBox.valueProperty().addListener(
                (observedLocale, oldLocale, newLocale) -> {
                    LOGGER.debug(String.format("Change locale from %s to %s.", oldLocale, newLocale));
                    try {
                        LOGGER.debug("TODO: change language results to a new game. Need to handle it better.");
                        reLoadScene(stage, newLocale);
                    } catch (IOException e) {
                        LOGGER.error("failed to load locale specific scene.", e);
                    }
        });
    }
    
    
    private void reLoadScene(Stage stage, Locale locale) throws IOException {
        I18n.setSelectedLocale(locale);
        I18n.setBundle(ResourceBundle.getBundle(I18n.getBundleBaseName(), locale));
        FXMLLoader loader = new FXMLLoader(TargetGameApp.class.getResource(TargetGameApp.FXML_MAIN_SCENE)
                , I18n.getBundle());
        Parent pane = loader.load();
        
        StackPane viewHolder = (StackPane)stage.getScene().getRoot();

        viewHolder.getChildren().clear();
        viewHolder.getChildren().add(pane);
        
        GameController controller = loader.getController();
        controller.setStage(stage);
        stage.setTitle(I18n.getBundle().getString(TargetGameApp.APP_TITLE_KEY));
        
        LOGGER.debug(targetGame.getTarget() == null? "No target set yet.":targetGame.getTarget().toString());
    }
}
