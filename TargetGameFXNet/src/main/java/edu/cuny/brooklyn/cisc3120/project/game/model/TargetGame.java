package edu.cuny.brooklyn.cisc3120.project.game.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class TargetGame {
    private static Logger LOGGER = LoggerFactory.getLogger(TargetGame.class);
    
    private final static int GAME_TARGET_AREA_WIDTH = 40;
    private final static int GAME_TARGET_AREA_HEIGHT = 40;
    private final static char TARGET_INDICATOR_ON_BOARD = 'X';
    
    private boolean gameStateChanged;
    
    private File theGameFile;
    private Target target;
    private GameBoard gameBoard;
    private Random rng;
    
    private GameStatistics gameStatistics;

    public TargetGame() {
        gameStateChanged = false;
        gameBoard = new GameBoard(GAME_TARGET_AREA_HEIGHT, GAME_TARGET_AREA_WIDTH);
        rng = new Random();
        target = null;
        gameStatistics = new GameStatistics();
    }
    
    public boolean isGameStateChanged() {
        return gameStateChanged;
    }

    public void setGameStateChanged(boolean gameStateChanged) {
        this.gameStateChanged = gameStateChanged;
    }

    public void saveTheGame(Window stage) throws FileNotFoundException, IOException {
        // TODO Auto-generated method stub
    	
    	/*
         * Save game data to a file
         * Search for the file first
         * If found, overwrite the file with new content
         * Else, create a new save file for the user
         * all output is directed to the save file
         * get the all the values of the statistics and write them to save file
         * only the values, not the label (int, doubles)
         * The format in which the file is to be saved in to avoid errors when loading
         * (numOfTargetsShot -> numOfShotsFired -> numOfTargetsMade -> 
         * numOfRoundsWon -> numOfRoundsPlayed -> accuracy)
         */
    	
    	 try{
        	 FileChooser fileChooser = new FileChooser();
            // Create new file
            String content =  this.getGameStatistics().getNumOfTargetsShot()+"\t"
            				+this.getGameStatistics().getNumOfShotsFired()+"\t"
            				+this.getGameStatistics().getNumOfTargetsMade()+"\t"
            				+this.getGameStatistics().getNumOfRoundsWon()+"\t"
            				+this.getGameStatistics().getNumOfRoundsPlayed()+"\t"
            				+this.getGameStatistics().getAccuracy();

            	
				File newFile =  fileChooser.showSaveDialog(stage);
            	FileWriter fw = new FileWriter(newFile);
                BufferedWriter bw = new BufferedWriter(fw);

                // Write in file
                bw.write(content);

                // Close connection
                bw.close();
            	
    	 }
        catch(Exception e){
            System.out.println(e);
        }
        		
        
        
    }

    public File getTheGameFile() {
        return theGameFile;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public Target getTarget() {
        return target;
    }
    
    public void setNewTarget() {
        target = getRandomTarget();
        addTargetToBoard(target);
    }

    private Target getRandomTarget() {
        int x = rng.nextInt(GAME_TARGET_AREA_WIDTH);
        int y = rng.nextInt(GAME_TARGET_AREA_HEIGHT);
        Target target = new Target(x, y);
        LOGGER.debug("Target: " + x + "," + y);
        return target;
    }

    private void addTargetToBoard(Target target) {
        gameBoard.setCell(target.getX(), target.getY(), TARGET_INDICATOR_ON_BOARD);
    }

    public GameStatistics getGameStatistics() {
        return gameStatistics;
    }    
}
