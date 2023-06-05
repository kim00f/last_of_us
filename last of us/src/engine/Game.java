package engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import exceptions.InvalidTargetException;
import exceptions.MovementException;
import exceptions.NoAvailableResourcesException;
import exceptions.NotEnoughActionsException;
import model.characters.*;
import model.characters.Character;
import model.collectibles.Collectible;
import model.collectibles.Supply;
import model.collectibles.Vaccine;
import model.world.Cell;
import model.world.CharacterCell;
import model.world.CollectibleCell;
import model.world.TrapCell;

import java.awt.Point;

public class Game {
	public static ArrayList<Hero> availableHeroes = new ArrayList<Hero>();;
	public static ArrayList<Hero> heroes = new ArrayList<Hero>(); 
	public static ArrayList<Zombie> zombies= new ArrayList<Zombie>();
	public static Cell [][] map = new Cell[15][15];
	public static void loadHeroes(String filePath) throws Exception {	
			BufferedReader file = new BufferedReader(new FileReader(filePath));
			String line = "";
			while((line = file.readLine()) != null) {
				String[] hero = line.split(",");
				switch(hero[1]) {
				case "FIGH" : availableHeroes.add(new Fighter(hero[0],Integer.parseInt(hero[2]),Integer.parseInt(hero[4]),Integer.parseInt(hero[3])));
					break;
				case "EXP": availableHeroes.add(new Explorer(hero[0],Integer.parseInt(hero[2]),Integer.parseInt(hero[4]),Integer.parseInt(hero[3])));
					break;
				case "MED": availableHeroes.add(new Medic(hero[0],Integer.parseInt(hero[2]),Integer.parseInt(hero[4]),Integer.parseInt(hero[3])));
					break;
				}
			}
			file.close();
	}
	public static boolean mapFull() {
		for(int i = 0;i<15;i++)
			for(int j = 0;j<15;j++)
				if(map[i][j] instanceof CharacterCell && ((CharacterCell)map[i][j]).getCharacter()==null)
					return false;
		return true;
	}
	//get a Random Point
	public static Point randPoint(){
	    int x ;
	    int y ;
	    do {
	    	x = (int)  (Math.random() * 15);
	 	    y = (int) (Math.random() * 15);
		}while(!((Game.map[x][y] instanceof CharacterCell) && ((CharacterCell)Game.map[x][y]).getCharacter()==null));
	    return new Point(x,y);
	}
	
	//spawn a Zombie in a random location
	public static void spawnZombie() {
		Zombie newZombie = new Zombie();
		Game.zombies.add(newZombie);
		Point p = Game.randPoint();
		newZombie.setLocation(p);
		((CharacterCell)Game.map[p.x][p.y]).setCharacter(newZombie);
		
	}
	//check if point outofbounds
	public static boolean outOfBounds(Point z) {
		return	z.x<0 || z.y<0 || z.x>14 || z.y>14;
	}
	
	public static void startGame(Hero h) {
		//Initializing Map 
		for(int i = 0;i<15;i++) 
			for(int j = 0;j<15;j++) 
					map[i][j]= new CharacterCell();
		
		//Adding the hero
		availableHeroes.remove(h);
		heroes.add(h);
		h.setLocation(new Point(0,0));
		map[0][0]=new CharacterCell(h);
		
		
		//Spawning Vaccines/Supplies/Traps
		
		for(int i = 0;i<15;i++){
			Point p = randPoint();
			if(i<5)
				map[p.x][p.y] = new CollectibleCell(new Vaccine());
			else if(i<10)
				map[p.x][p.y] = new CollectibleCell(new Supply());
			else if(i<15)
				map[p.x][p.y] = new TrapCell();
		}
		//Spawning Zombies
		for(int i = 0;i<10;i++) {
			spawnZombie();
		}
		
		//Visiblity
		h.setVisibilityBlock();
			
		
	}
	
	//Checking Map For Vaccines
	public static boolean checkMap() {
		for(int i = 0;i<15;i++)
			for(int j = 0;j<15;j++)
				if(map[i][j] instanceof CollectibleCell && ((CollectibleCell)map[i][j]).getCollectible() instanceof Vaccine)
					return false;
		return true;
	}
	
	//Checking Heroes Inventory For Vaccines
	public static boolean checkHeroInv() {
		for(Hero i : heroes)
			if(!i.getVaccineInventory().isEmpty())
				return false;
		return true;
	}
	
	//Winning if Used ALL vaccines and heroes>=5
	public static boolean checkWin() {
		return heroes.size()>=5 && checkMap() && checkHeroInv();
	}
	
	//Losing if no heroes or Used All Vaccines and heroes<5 or no more available heroes to add
	public static boolean checkGameOver() {
		return heroes.isEmpty() || (checkMap() && checkHeroInv()) ;
	}
	
	public static void endTurn() throws NotEnoughActionsException, InvalidTargetException {
		//Zombies Attacking Adjacent Heroes
		
		for(int k = 0;k<zombies.size();k++) {
				Zombie i = zombies.get(k);
				i.attack();	
				if(!zombies.contains(i))
					k--;
		}
		
		//Setting Visibility to false
		for(int i = 0;i<15;i++) 
			for(int j = 0;j<15;j++)
					map[i][j].setVisible(false);
			
		//Reseting Heroes and Visible Cells
		for(Hero i : heroes) {
			i.setTarget(null);
			i.setActionsAvailable(i.getMaxActions());
			i.setSpecialAction(false);
			i.setVisibilityBlock();
		}
		
		//Spawning a Zombie in a random location
		if(!mapFull())
			spawnZombie();
				
		
	}
}