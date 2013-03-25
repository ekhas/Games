package kw.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.JOptionPane;


/**
 *
 * @author Ken Wu @ New York
 */
public class Game extends Observable {
	public static final int VERYEASY = 1;
    public static final int EASY = 2;
	public static final int MEDIUM = 3;
	public static final int HARD = 4;
	public static final int VERYHARD = 5;
	public static final int EXPERT = 6;
	
	private int[][] solution;       // Generated solution.
    private int[][] game;           // Generated game with user input.
    private boolean[][] check;      // Holder for checking validity of game.
    private int selectedNumber;     // Selected number by user.
    private boolean help;           // Help turned on or off.
	private Set<Solution> m_calculatedSols;	// store the calculated solution

	public int m_level;
	private boolean m_inUndoRedo;
	private Stack m_undoStack;
	private Stack m_redoStack;
	
    public Set<Solution> getCalculatedSols() {
		return m_calculatedSols;
	}

	/**
     * Constructor
     */
    public Game() {
        this(MEDIUM);
    }
    
	/**
     * Constructor
     */
    public Game(int level) {
        newGame();
        check = new boolean[9][9];
        help = true;
    }

    
    /**
     * Generates a new Sudoku game.<br />
     * All observers will be notified, update action: new game.
     */
    public void newGame() {
        solution = generateSolution(new int[9][9], 0);
        game = generateGame(copy(solution));
        
        setChanged();
        notifyObservers(UpdateAction.NEW_GAME);
    }

    public String getLevelString() {
    	String returnStr = "";
        switch(this.m_level) {
    	case VERYEASY:
    		returnStr = "Very easy";
    		break;
    	case EASY:
    		returnStr = "Easy";
    		break;
    	case MEDIUM:
    		returnStr = "Medium";
    		break;	
    	case HARD:
    		returnStr = "Hard";
    		break;
    	case VERYHARD:
    		returnStr = "Very hard";
    		break;	
    	default:	// EXPERT
    		returnStr = "Expert";
    		break;	
        }
        return returnStr;
	}

	/**
     * Checks user input agains the solution and puts it into a check matrix.<br />
     * All observers will be notified, update action: check.
     */
    public void checkGame() {
        selectedNumber = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                check[y][x] = game[y][x] == solution[y][x];
        }
        setChanged();
        notifyObservers(UpdateAction.CHECK);
    }

    /**
     * Sets help turned on or off.<br />
     * All observers will be notified, update action: help.
     * 
     * @param help True for help on, false for help off.
     */
    public void setHelp(boolean help) {
        this.help = help;
        setChanged();
        notifyObservers(UpdateAction.HELP);
    }

    /**
     * Sets selected number to user input.<br />
     * All observers will be notified, update action: selected number.
     *
     * @param selectedNumber    Number selected by user.
     */
    public void setSelectedNumber(int selectedNumber) {
        this.selectedNumber = selectedNumber;
        setChanged();
        notifyObservers(UpdateAction.SELECTED_NUMBER);
    }

    /**
     * Returns number selected user.
     *
     * @return  Number selected by user.
     */
    public int getSelectedNumber() {
        return selectedNumber;
    }

    /**
     * Returns whether help is turned on or off.
     *
     * @return True if help is turned on, false if help is turned off.
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns whether selected number is candidate at given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if selected number on given position is candidate,
     *              false otherwise.
     */
    public boolean isSelectedNumberCandidate(int x, int y) {
        return game[y][x] == 0 && isPossibleX(game, -1, y, selectedNumber)
                && isPossibleY(game, x, -1, selectedNumber) && isPossibleBlock(game, x, y, selectedNumber, false);
    }

    /**
     * Sets given number on given position in the game.
     *
     * @param x         The x position in the game.
     * @param y         The y position in the game.
     * @param number    The number to be set.
     */
    public void setNumber(int x, int y, int number) {
        game[y][x] = number;
    }

    /**
     * Returns number of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      Number of given position.
     */
    public int getNumber(int x, int y) {
        return game[y][x];
    }

    /**
     * Returns whether user input is valid of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if user input of given position is valid, false
     *              otherwise.
     */
    public boolean isCheckValid(int x, int y) {
        return check[y][x];
    }

    /**
     * Returns whether given number is candidate on x axis for given game.
     *
     * @param game      Game to check.
     * @param x         Position of x axis to check., -1 if ignorance
     * @param y         Position of y axis to check.
     * @param number    Number to check.
     * @return          True if number is candidate on x axis, false otherwise.
     */
    public static boolean isPossibleX(int[][] game, int xx, int y, int number) {
        for (int x = 0; x < 9; x++) {
            if (game[y][x] == number) {
            	if(xx == -1 || xx != x)
            		return false;
            }
                
        }
        return true;
    }

    /**
     * Returns whether given number is candidate on y axis for given game.
     *
     * @param game      Game to check.
     * @param x         Position of y axis to check.
     * @param y         Position of y axis to check. -1 if ignorance
     * @param number    Number to check.
     * @return          True if number is candidate on y axis, false otherwise.
     */
    public static boolean isPossibleY(int[][] game, int x, int yy, int number) {
        for (int y = 0; y < 9; y++) {
            if (game[y][x] == number) {
            	if(yy == -1 || yy != y)
            		return false;

            }
        }
        return true;
    }

    /**
     * Returns whether given number is candidate in block for given game.
     *
     * @param game      	Game to check.
     * @param x         	Position of number on x axis in game to check.
     * @param y        	 	Position of number on y axis in game to check.
     * @param number   	 	Number to check.
     * @param hasToUnique   True if the number has to be unique in the block.
     * @return          True if number is candidate in block, false otherwise.
     */
    public static boolean isPossibleBlock(int[][] game, int x, int y, int number, boolean hasToUnique) {
        int x1 = x < 3 ? 0 : x < 6 ? 3 : 6;
        int y1 = y < 3 ? 0 : y < 6 ? 3 : 6;
        for (int yy = y1; yy < y1 + 3; yy++) {
            for (int xx = x1; xx < x1 + 3; xx++) {
                if (game[yy][xx] == number) {
                	if(!hasToUnique || (x!=xx || y!=yy))
                		return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns next posible number from list for given position or -1 when list
     * is empty.
     *
     * @param game      Game to check.
     * @param x         X position in game.
     * @param y         Y position in game.
     * @param numbers   List of remaining numbers.
     * @return          Next possible number for position in game or -1 when
     *                  list is empty.
     */
    private int getNextPossibleNumber(int[][] game, int x, int y, List<Integer> numbers) {
        while (numbers.size() > 0) {
            int number = numbers.remove(0);
            if (isPossibleX(game, -1, y, number) && isPossibleY(game, x, -1, number) && isPossibleBlock(game, x, y, number, false))
                return number;
        }
        return -1;
    }

    /**
     * Generates Sudoku game solution.
     *
     * @param game      Game to fill, user should pass 'new int[9][9]'.
     * @param index     Current index, user should pass 0.
     * @return          Sudoku game solution.
     */
    private int[][] generateSolution(int[][] game, int index) {
        if (index > 80)
            return game;

        int x = index % 9;
        int y = index / 9;

        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 1; i <= 9; i++) numbers.add(i);
        Collections.shuffle(numbers);

        while (numbers.size() > 0) {
            int number = getNextPossibleNumber(game, x, y, numbers);
            if (number == -1)
                return null;

            game[y][x] = number;
            int[][] tmpGame = generateSolution(game, index + 1);
            if (tmpGame != null)
                return tmpGame;
            game[y][x] = 0;
        }

        return null;
    }

    /**
     * Generates Sudoku game from solution.
     *
     * @param game      Game to be generated, user should pass a solution.
     * @return          Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game) {
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < 81; i++)
            positions.add(i);
        Collections.shuffle(positions);
        
        //Now i am going to remove the position based on the level of the game, the easier the game, the more i am going to remove
        int numDelete;
        switch(this.m_level) {
        	case VERYEASY:
        		numDelete = 75;
        		break;
        	case EASY:
        		numDelete = 65;
        		break;
        	case MEDIUM:
        		numDelete = 45;
        		break;	
        	case HARD:
        		numDelete = 38;
        		break;
        	case VERYHARD:
        		numDelete = 33;
        		break;	
        	default:	// EXPERT
        		numDelete = 28;
        		break;	
        }
        for(int i=0; i<numDelete; i++) {
        	positions.remove(0);
        }
        return generateGame(game, positions);
    }

    /**
     * Generates Sudoku game from solution, user should use the other
     * generateGame method. This method simple removes a number at a position.
     * If the game isn't anymore valid after this action, the game will be
     * brought back to previous state.
     *
     * @param game          Game to be generated.
     * @param positions     List of remaining positions to clear.
     * @return              Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game, List<Integer> positions) {
        while (positions.size() > 0) {
            int position = positions.remove(0);
            int x = position % 9;
            int y = position / 9;
            int temp = game[y][x];
            game[y][x] = 0;

            /*
            if (!isValid(game))
                game[y][x] = temp;
            */
        }

        return game;
    }

    /**
     * Checks whether given game is valid.
     *
     * @param game      Game to check.
     * @return          True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game) {
        return isValid(game, 0, new int[] { 0 });
    }

    
    /**
     * Checks whether given game is valid, user should use the other isValid
     * method. There may only be one solution.
     *
     * @param game                  Game to check.
     * @param index                 Current index to check.
     * @param numberOfSolutions     Number of found solutions. Int[] instead of
     *                              int because of pass by reference.
     * @return                      True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game, int index, int[] numberOfSolutions) {
        if (index > 80)
            return ++numberOfSolutions[0] == 1;

        int x = index % 9;
        int y = index / 9;
        
       
        
        if (game[y][x] == 0) {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 1; i <= 9; i++)
                numbers.add(i);

            while (numbers.size() > 0) {
                int number = getNextPossibleNumber(game, x, y, numbers);
                if (number == -1)
                    break;
                game[y][x] = number;

                if (!isValid(game, index + 1, numberOfSolutions)) {
                    game[y][x] = 0;
                    return false;
                }
                game[y][x] = 0;
            }
        } else if (!isValid(game, index + 1, numberOfSolutions))
            return false;

        return true;
    }

    /**
     * Copies a game.
     *
     * @param game      Game to be copied.
     * @return          Copy of given game.
     */
    private int[][] copy(int[][] game) {
        int[][] copy = new int[9][9];
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                copy[y][x] = game[y][x];
        }
        return copy;
    }

    
    public int[][] generateFinalSolution() {
    	return this.generateFinalSolution(this.game);
    }
    
    public Set<Solution> generateFinalSolutions() {
    	this.m_calculatedSols = this.generateFinalSolutions(this.game);
    	return m_calculatedSols;
    }
    
    /**
     * Generate a final solution and display it.
     *
     */		
	public int[][] generateFinalSolution(int[][] game) {
		int[][] cGame = copy(game); 
		if(!isThisGameStillValid(cGame)) {
			return null;
		}
		return generateFinalSolutionHelper(cGame, 0, 0);
	}
	
    /**
     * Generate a final solution and display it.
     *
     */		
	public Set<Solution> generateFinalSolutions(int[][] game) {
		int[][] cGame = copy(game); 
		if(!isThisGameStillValid(cGame)) {
			return null;
		}
		Set<Solution> finSols = new HashSet<Solution>();
		generateFinalSolutionsHelper(cGame, 0, 0, finSols);
		return finSols;
	}
	
	private boolean isThereAnyZeroInTheBoards(int[][] cGame) {
		
		for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
            	if(cGame[y][x] == 0) {
			        return true;
            	}
            }
		}
		return false;
	}
	
	public static boolean isThisGameASolution(Solution sol) {
		return isThisGameASolution(sol.getSolution());
	}
	
	public static boolean isThisGameASolution(int[][] cGame) {
		for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
            	if(cGame[y][x] == 0) {
            		return false;
            	}
            }
		}
		return isThisGameStillValid(cGame);
	}
	
	public static boolean isThisGameStillValid(int[][] cGame) {
		
		for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
            	if(cGame[y][x] != 0) {
			        if (!isPossibleX(cGame, x, y, cGame[y][x]) || !isPossibleY(cGame, x, y, cGame[y][x]) || !isPossibleBlock(cGame, x, y, cGame[y][x], true)) {
			        	return false;
			        }
            	}
            }
		}
		return true;
	}

	/*
	 * @return          The return value has no much meaning.
	 */
	private boolean generateFinalSolutionsHelper(int[][] cGame, int sX, int sY, Set<Solution> sols) {
		for (int y = sY; y < 9; y++) {
            for (int x = sX; x < 9; x++) {
            	if(cGame[y][x] == 0) {
                    List<Integer> numbers = new ArrayList<Integer>();
                    for (int i = 1; i <= 9; i++) numbers.add(i);
                    while (numbers.size() > 0) {
                        int number = getNextPossibleNumber(cGame, x, y, numbers);
                        if (number == -1) {
                        	return false;
                        }
                        cGame[y][x] = number;
                        int xx = x; int yy = y;
                        if(x + 1 < 9) {
                        	xx = x + 1;
                        } else if (y + 1 < 9) {
                        	xx = 0;
                        	yy = y + 1;
                        } else {
                        	//Now i check if this is a done game with no 0 in each column
                        	
                        	if(!isThereAnyZeroInTheBoards(cGame)) {
                        		int [][] cpy = this.copy(cGame);
                        		Solution s = new Solution(cpy);
                        		sols.add(s);
                        		return true;
                        	} else {
                        		//System.out.println("DEW HELLO");
                        		return false;
                        	}
                        	
                        	
                        }
                        boolean valided = generateFinalSolutionsHelper(cGame, xx, yy, sols);
                        
                        if(!valided) {
                        	cGame[y][x] = 0;
                        } else {
                        	//System.out.println("true");
                        	if(!isThereAnyZeroInTheBoards(cGame)) {
                        		int [][] cpy = this.copy(cGame);
                        		Solution s = new Solution(cpy);
                        		sols.add(s);
                        		cGame[y][x] = 0;
                        	} else {
                            	                        		
                        	}
                        }
                    }
                    
                    if(cGame[y][x] == 0) {
                    	return false;
                    } 
            	} else {
            		if(x + 1 >= 9) {
            			//The last column
            			sX = 0;	//Reset the init column
            			sY = 0;	//Reset the init column
            		}
            	}
            }
        }
		/*
		if(!isThereAnyZeroInTheBoards(cGame)) {
			System.out.println("ooooops1");     
			return true;
		} else {
			System.out.println("ooooops2");     
			return false;
		}
		*/
		return true;
	}
	
	/*
	 * @return          A single solution of the game (while it can have multipile solutions.
	 */
	private int[][] generateFinalSolutionHelper(int[][] cGame, int sX, int sY) {
		for (int y = sY; y < 9; y++) {
            for (int x = sX; x < 9; x++) {
            	if(cGame[y][x] == 0) {
                    List<Integer> numbers = new ArrayList<Integer>();
                    for (int i = 1; i <= 9; i++) numbers.add(i);
                    while (numbers.size() > 0) {
                        int number = getNextPossibleNumber(cGame, x, y, numbers);
                        if (number == -1) {
                        	return null;
                        }
                        cGame[y][x] = number;
                        int xx = x; int yy = y;
                        if(x + 1 < 9) {
                        	xx = x + 1;
                        } else if (y + 1 < 9) {
                        	xx = 0;
                        	yy = y + 1;
                        } else {
                        	//Now i check if this is a done game with no 0 in each column
                        	if(!isThereAnyZeroInTheBoards(cGame))
                        		return cGame;
                        	else
                        		return null;
                        }
                        int[][] tGame = generateFinalSolutionHelper(cGame, xx, yy);
                        if (tGame != null) {
                            return tGame;
                        }
                        cGame[y][x] = 0;
                    }
                    
                    if(cGame[y][x] == 0) {
                    	return null;
                    } 
            	} else {
            		if(x + 1 >= 9) {
            			//The last column
            			sX = 0;	//Reset the init column
            		}
            	}
            }
        }
		return cGame;
	}

    /*
     * Prints given game to console. Used for debug.
     *
     * @param game  Game to be printed.
     */
    public static void print(int[][] game) {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                System.out.print("" + game[y][x]);
            //System.out.println();
        }
        System.out.println();
    }
    
    public static void print(Solution game) {
        print(game.getSolution());
    }

	public void showSolution() {
		m_calculatedSols = generateFinalSolutions();
    	if(m_calculatedSols == null || m_calculatedSols.size() == 0) {
    		int response = JOptionPane.showConfirmDialog(null, "Sorry, no solution on what you entered!!!\n"
    			    + "Press Yes to start a new game or No to continue", "Ooops",
    		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    		if (response == JOptionPane.NO_OPTION) {
    		      
    		    } else if (response == JOptionPane.YES_OPTION) {
    		    	newGame();
    		    } else if (response == JOptionPane.CLOSED_OPTION) {
    		      
    		    }
    		
    	} else {
            setChanged();
            notifyObservers(UpdateAction.SHOW_SOLUTIONS);
    		
    	}
		
	}

	public static void print(Set<Solution> sols) {
		int i=1;
		for (Solution sol: sols) {
			System.out.println("Solution "+i+++": ");
			print(sol);
			System.out.println("");
		}
	}

	public void setGameLevel(int level) {
		m_level = level;
		newGame();
	}


}