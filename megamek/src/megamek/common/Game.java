/*
 * MegaMek - Copyright (C) 2000-2003 Ben Mazur (bmazur@sev.org)
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License as published by the Free 
 *  Software Foundation; either version 2 of the License, or (at your option) 
 *  any later version.
 * 
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 *  for more details.
 */

package megamek.common;

//import java.util.*;
import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.Map;
import com.sun.java.util.collections.Collections;
import com.sun.java.util.collections.Iterator;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import java.io.*;

import megamek.common.actions.*;

/**
 * The game class is the root of all data about the game in progress.
 * Both the Client and the Server should have one of these objects and it
 * is their job to keep it synched.
 */
public class Game implements Serializable
{
    public static final int PHASE_UNKNOWN           = -1;
    public static final int PHASE_LOUNGE            = 1;
    public static final int PHASE_SELECTION         = 2;
    public static final int PHASE_EXCHANGE          = 3;
    public static final int PHASE_DEPLOYMENT        = 12;  // reorder someday
    public static final int PHASE_INITIATIVE        = 4;
    public static final int PHASE_MOVEMENT          = 5;
    public static final int PHASE_MOVEMENT_REPORT   = 6;
    public static final int PHASE_FIRING            = 7;
    public static final int PHASE_FIRING_REPORT     = 8;
    public static final int PHASE_PHYSICAL          = 9;
    public static final int PHASE_END               = 10;
    public static final int PHASE_VICTORY           = 11;
    public static final int PHASE_DEPLOY_MINEFIELDS = 13;
    public static final int PHASE_STARTING_SCENARIO = 14;
    /**
     * The number of Infantry platoons that have to move for every Mek
     * or Vehicle, if the "inf_move_multi" option is selected.
     */
    public static final int INF_MOVE_MULTI          = 3;

    /**
     * Define constants to describe the condition a
     * unit was in when it wass removed from the game.
     */
    
    private GameOptions options = new GameOptions();

    public Board board = new Board();
    
    private Vector entities = new Vector();
    private Hashtable entityIds = new Hashtable();
    
    /** Track entities removed from the game (probably by death) */
    private Vector vOutOfGame = new Vector();

    private Vector players = new Vector();
    private Vector teams   = new Vector(); // DES

    private Hashtable playerIds = new Hashtable();
    
    /** have the entities been deployed? */
    private boolean deploymentComplete = false;

    /** how's the weather? */
    private int windDirection;
    private String stringWindDirection;
    
    /** what round is it? */
    private int roundCount = 0;
    
    /** The current turn list */
    private Vector turnVector = new Vector();
    private int turnIndex = 0;
    
    /** The present phase */
    private int phase = PHASE_UNKNOWN;

    /** The past phase */
    private int lastPhase = PHASE_UNKNOWN;

    // phase state
    private Vector actions = new Vector();
    private Vector pendingCharges = new Vector();
    private Vector pilotRolls = new Vector();
    
    // reports
    private StringBuffer roundReport = new StringBuffer();
    private StringBuffer phaseReport = new StringBuffer();
    
    private boolean forceVictory = false;
    private int victoryPlayerId = Player.PLAYER_NONE;
    private int victoryTeam = Player.TEAM_NONE;

    private Hashtable deploymentTable = new Hashtable();
    private int lastDeploymentRound = 0;
  
    // Settings for the LOS tool.
    private boolean mechInFirstHex = true;
    private boolean mechInSecondHex = true;

    private Map minefields = Collections.synchronizedMap(new HashMap());
    private Vector vibrabombs = new Vector();
    /**
     * Constructor
     */
    public Game() {
        ;
    }
    
    // If it's a mech in the first hex used by the LOS tool
    public boolean getMechInFirst() {
      return mechInFirstHex;
    }

    // If it's a mech in the second hex used by the LOS tool
    public boolean getMechInSecond() {
      return mechInSecondHex;
    }

    // If it's a mech in the first hex used by the LOS tool
    public void setMechInFirst(boolean mech) {
      mechInFirstHex = mech;
    }

    // If it's a mech in the second hex used by the LOS tool
    public void setMechInSecond(boolean mech) {
      mechInSecondHex = mech;
    }

  public boolean containsMinefield(Coords coords) {
    return minefields.containsKey(coords);
  }
    
  public Vector getMinefields(Coords coords) {
      Vector mfs = (Vector) minefields.get(coords);
      if (mfs == null) {
        return new Vector();
      }
    return mfs;
  }
    
  public int getNbrMinefields(Coords coords) {
      Vector mfs = (Vector) minefields.get(coords);
      if (mfs == null) {
        return 0;
      }
    
    return mfs.size();
  }
    
    /**
     * Get the coordinates of all mined hexes in the game.
     *
     * @return  an <code>Enumeration</code> of the <code>Coords</code>
     *          containing minefilds.  This will not be <code>null</code>.
     */
    public Enumeration getMinedCoords() {
        // Create an anonymous inner class to wrap the minefiled's
        // key set's iterator in an Enumeration.
        return new Enumeration() {
                private Iterator iter = 
                    Game.this.minefields.keySet().iterator();
                public boolean hasMoreElements() {
                    return iter.hasNext();
                }
                public Object nextElement() {
                    return iter.next();
                }
            };
    }
    public void addMinefield(Minefield mf) {
      Vector mfs = (Vector) minefields.get(mf.getCoords());
      if (mfs == null) {
        mfs = new Vector();
        mfs.addElement(mf);
        minefields.put(mf.getCoords(), mfs);
        return;
      }
      mfs.addElement(mf);
    }
    
    public void removeMinefield(Minefield mf) {
      Vector mfs = (Vector) minefields.get(mf.getCoords());
      if (mfs == null) {
        return;
      }
      
      Enumeration e = mfs.elements();
      while (e.hasMoreElements()) {
        Minefield mftemp = (Minefield) e.nextElement();
        if (mftemp.equals(mf)) {
          mfs.removeElement(mftemp);
          break;
        }
      }
      if (mfs.isEmpty()) {
        minefields.remove(mf.getCoords());
      }
  }
  
  public void clearMinefields() {
    minefields.clear();
  }
    
    public Vector getVibrabombs() {
      return vibrabombs;
    }
    
    public void addVibrabomb(Minefield mf) {
      vibrabombs.addElement(mf);
    }
    
    public void removeVibrabomb(Minefield mf) {
      vibrabombs.removeElement(mf);
  }
    
    public boolean containsVibrabomb(Minefield mf) {
      return vibrabombs.contains(mf);
    }
    
    public GameOptions getOptions() {
        return options;
    }
    
    public void setOptions(GameOptions options) {
        this.options = options;
    }
    
    
    public Board getBoard() {
        return board;
    }
    

    /**
     * Return an enumeration of teams in the game
     */
    public Enumeration getTeams() {
  return teams.elements();
    }

    /** Return the teams vector */
    public Vector getTeamsVector() {
  return teams;
    }

    /**
     * Return an enumeration of player in the game
     */
    public Enumeration getPlayers() {
        return players.elements();
    }
    
    /**
     * Return the players vector
     */
    public Vector getPlayersVector() {
        return players;
    }
    
    /**
     * Return the current number of active players in the game.
     */
    public int getNoOfPlayers() {
        return players.size();
    }
    
    /**
     * Returns the individual player assigned the id parameter.
     */
    public Player getPlayer(int id) {
        return (Player)playerIds.get(new Integer(id));
    }
    
    public void addPlayer(int id, Player player) {
        player.setGame(this);
        players.addElement(player);
        playerIds.put(new Integer(id), player);
    }
  
    public void setPlayer(int id, Player player) {
        final Player oldPlayer = getPlayer(id);
        player.setGame(this);
        players.setElementAt(player, players.indexOf(oldPlayer));
        playerIds.put(new Integer(id), player);
    }
  
    public void removePlayer(int id) {
        players.removeElement(getPlayer(id));
        playerIds.remove(new Integer(id));
    }
  
    /**
     * Returns the number of entities owned by the player, regardless of
     * their status, as long as they are in the game.
     */
    public int getEntitiesOwnedBy(Player player) {
        int count = 0;
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if (entity.getOwner().equals(player)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns the number of non-destroyed entityes owned by the player
     */
    public int getLiveEntitiesOwnedBy(Player player) {
        int count = 0;
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if (entity.getOwner().equals(player) && !entity.isDestroyed()) {
                count++;
            }
        }
        return count;
    }    
  
    /**
     * Returns the number of non-destroyed deployed entityes owned by the player
     */
    public int getLiveDeployedEntitiesOwnedBy(Player player) {
        int count = 0;
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if (entity.getOwner().equals(player) && !entity.isDestroyed() && entity.isDeployed()) {
                count++;
            }
        }
        return count;
    }    
  
    /**
     * Get a vector of entity objects that are "acceptable" to attack with this entity
     */
    public Vector getValidTargets(Entity entity) {
        Vector ents = new Vector();

        boolean friendlyFire = getOptions().booleanOption("friendly_fire");
        
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            Entity otherEntity = (Entity)i.nextElement();

            // Even if friendly fire is acceptable, do not shoot yourself
            // Enemy units not on the board can not be shot.
            if ( (null != otherEntity.getPosition()) &&
                 ( entity.isEnemyOf(otherEntity) || 
                   (friendlyFire && entity.getId() != otherEntity.getId()) ) ) {
                ents.addElement( otherEntity );
            }
        }
        
        return ents;
    }
    
    /**
     * Returns true if this phase has turns.  If false, the phase is simply
     * waiting for everybody to declare "done".
     */
    public boolean phaseHasTurns(int phase) {
        switch (phase) {
            case PHASE_DEPLOY_MINEFIELDS :
            case PHASE_DEPLOYMENT :
            case PHASE_MOVEMENT :
            case PHASE_FIRING :
            case PHASE_PHYSICAL :
                return true;
            default :
                return false;
        }
    }
    
    /**
     * Returns the current GameTurn object
     */
    public GameTurn getTurn() {
        if (turnIndex < 0 || turnIndex >= turnVector.size()) {
            return null;
        }
        return (GameTurn)turnVector.elementAt(turnIndex);
    }
  
    /** Changes to the next turn, returning it. */
    public GameTurn changeToNextTurn() {
        turnIndex++;  
        return getTurn();
    }
    
    /** Resets the turn index to -1 (awaiting first turn) */
    public void resetTurnIndex() {
        turnIndex = -1;
    }
    
    /** Returns true if there is a turn after the current one */
    public boolean hasMoreTurns() {
        return turnVector.size() > (turnIndex + 1);
    }
    
    /** Inserts a turn that will come directly after the current one */
    public void insertNextTurn(GameTurn turn) {
  turnVector.insertElementAt(turn, turnIndex + 1);
    }

    /** Returns an Enumeration of the current turn list */
    public Enumeration getTurns() {
        return turnVector.elements();
    }
    
    /** Returns the current turn index */
    public int getTurnIndex() {
        return turnIndex;
    }
  
    /** Sets the current turn index */
    public void setTurnIndex(int turnIndex) {
        this.turnIndex = turnIndex;
    }
  
    /** Returns the current turn vector */
    public Vector getTurnVector() {
        return turnVector;
    }
  
    /** Sets the current turn vector */
    public void setTurnVector(Vector turnVector) {
        this.turnVector = turnVector;
    }
  
    public int getPhase() {
        return phase;
    }
    
    public void setPhase(int phase) {
        this.phase = phase;
    }
    
    public int getLastPhase() {
        return lastPhase;
    }
    
    public void setLastPhase(int lastPhase) {
        this.lastPhase = lastPhase;
    }
    
    public void setDeploymentComplete(boolean deploymentComplete) {
      this.deploymentComplete = deploymentComplete;
    }
    
    public boolean isDeploymentComplete() {
      return deploymentComplete;
    }

  /**
   * Sets up up the hashtable of who deploys when
   */
    public void setupRoundDeployment() {
      deploymentTable = new Hashtable();
      
      for ( int i = 0; i < entities.size(); i++ ) {
        Entity ent = (Entity)entities.elementAt(i);
        
        Vector roundVec = (Vector)deploymentTable.get(new Integer(ent.getDeployRound()));
        
        if ( null == roundVec ) {
          roundVec = new Vector();
          deploymentTable.put(new Integer(ent.getDeployRound()), roundVec);
        }
        
        roundVec.addElement(ent);
        lastDeploymentRound = Math.max(lastDeploymentRound, ent.getDeployRound());
      }
    }
    
  /**
   * Checks to see if we've past our deployment completion
   */
    public void checkForCompleteDeployment() {
      setDeploymentComplete(lastDeploymentRound < getRoundCount());
    }
   
   /**
    * Check to see if we should deploy this round
    */
    public boolean shouldDeployThisRound() {
      return shouldDeployForRound(getRoundCount());
    }

    public boolean shouldDeployForRound(int round) {
      Vector vec = getEntitiesToDeployForRound(round);

      return ( ((null == vec) || (vec.size() == 0)) ? false : true);
    }
   /**
    * Returns a vector of entities to deploy this round
    */
    private Vector getEntitiesToDeployThisRound() {
      return getEntitiesToDeployForRound(getRoundCount());
    }
    
    private Vector getEntitiesToDeployForRound(int round) {
      return (Vector)deploymentTable.get(new Integer(round));
    }
    
   /**
    * Clear this round from this list of entities to deploy
    */
    public void clearDeploymentThisRound() {
      deploymentTable.remove(new Integer(getRoundCount()));
    }
    
   /**
    * Returns a vector of entities that have not yet deployed
    */
    public Vector getUndeployedEntities() {
      Vector entList = new Vector();
      Enumeration enum = deploymentTable.elements();
      
      while ( enum.hasMoreElements() ) {
        Vector vecTemp = (Vector)enum.nextElement();
        
        for ( int i = 0; i < vecTemp.size(); i++ ) {
          entList.addElement(vecTemp.elementAt(i));
        }
      }
      
      return entList;
    }
    
    /**
     * Returns an enumeration of all the entites in the game.
     */
    public Enumeration getEntities() {
        return entities.elements();
    }
    
    /**
     * Returns the actual vector for the entities
     */
    public Vector getEntitiesVector() {
        return entities;
    }
    
    public void setEntitiesVector(Vector entities) {
        this.entities = entities;
        reindexEntities();
    }

    /**
     * Returns the actual vector for the out-of-game entities
     */
    public Vector getOutOfGameEntitiesVector() {
        return vOutOfGame;
    }

    /**
     * Swap out the current list of dead (or fled) units for a new one.
     *
     * @param   vOutOfGame - the new <code>Vector</code> of dead or fled units.
     *          This value should <em>not</em> be <code>null</code>.
     * @throws  <code>IllegalArgumentException</code> if the new list is
     *          <code>null</code>.
     */
    public void setOutOfGameEntitiesVector(Vector vOutOfGame) {
        if ( null == vOutOfGame ) {
            throw new IllegalArgumentException
                ( "New out-of-game list is null." );
        }
        this.vOutOfGame = vOutOfGame;
        // Add these entities to the game.
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            entity.setGame(this);
        }
    }

    /**
     * Returns a <code>Vector</code> containing the <code>Entity</code>s
     * that are in the same C3 network as the passed-in unit.  The output
     * will contain the passed-in unit, if the unit has a C3 computer.  If
     * the unit has no C3 computer, the output will be empty (but it will
     * never be <code>null</code>).
     *
     * @param   entity - the <code>Entity</code> whose C3 network co-
     *          members is required.  This value may be <code>null</code>.
     * @return  a <code>Vector</code> that will contain all other
     *          <code>Entity</code>s that are in the same C3 network
     *          as the passed-in unit.  This <code>Vector</code> may
     *          be empty, but it will not be <code>null</code>.
     * @see     #getC3SubNetworkMembers( Entity )
     */
    public Vector getC3NetworkMembers( Entity entity ){
        Vector members = new Vector();

        // Does the unit have a C3 computer?
        if ( entity != null && (entity.hasC3() || entity.hasC3i()) ) {

            // Walk throught the entities in the game, and add all
            // members of the C3 network to the output Vector.
            Enumeration units = entities.elements();
            while ( units.hasMoreElements() ) {
                Entity unit = (Entity) units.nextElement();
                if ( entity.equals(unit) || entity.onSameC3NetworkAs(unit) ) {
                    members.addElement( unit );
                }
            }

        } // End entity-has-C3

        return members;
    }

    /**
     * Returns a <code>Vector</code> containing the <code>Entity</code>s
     * that are in the C3 sub-network under the passed-in unit.  The output
     * will contain the passed-in unit, if the unit has a C3 computer.  If
     * the unit has no C3 computer, the output will be empty (but it will
     * never be <code>null</code>).  If the passed-in unit is a company
     * commander or a member of a C3i network, this call is the same as
     * <code>getC3NetworkMembers</code>.
     *
     * @param   entity - the <code>Entity</code> whose C3 network sub-
     *          members is required.  This value may be <code>null</code>.
     * @return  a <code>Vector</code> that will contain all other
     *          <code>Entity</code>s that are in the same C3 network
     *          under the passed-in unit.  This <code>Vector</code> may
     *          be empty, but it will not be <code>null</code>.
     * @see     #getC3NetworkMembers( Entity )
     */
    public Vector getC3SubNetworkMembers( Entity entity ){

        // Handle null, C3i, and company commander units.
        if ( entity == null || entity.hasC3i() || entity.C3MasterIs(entity) ) {
            return getC3NetworkMembers( entity );
        }

        Vector members = new Vector();

        // Does the unit have a C3 computer?
        if ( entity.hasC3() ) {

            // Walk throught the entities in the game, and add all
            // sub-members of the C3 network to the output Vector.
            Enumeration units = entities.elements();
            while ( units.hasMoreElements() ) {
                Entity unit = (Entity) units.nextElement();
                if ( entity.equals(unit) || unit.C3MasterIs(entity) ) {
                    members.addElement( unit );
                }
            }

        } // End entity-has-C3

        return members;
    }

    /**
     * Returns a <code>Hashtable</code> that maps the <code>Coords</code>
     * of each unit in this <code>Game</code> to a <code>Vector</code>
     * of <code>Entity</code>s at that positions.  Units that have no
     * position (e.g. loaded units) will not be in the map.
     *
     * @return  a <code>Hashtable</code> that maps the <code>Coords</code>
     *          positions or each unit in the game to a <code>Vector</code>
     *          of <code>Entity</code>s at that position.
     */
    public Hashtable getPositionMap() {
        Hashtable positionMap = new Hashtable();
        Vector atPos = null;

        // Walk through the entities in this game.
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();

            // Get the vector for this entity's position.
            final Coords coords = entity.getPosition();
            if ( coords != null ) {
                atPos = (Vector) positionMap.get( coords );

                // If this is the first entity at this position,
                // create the vector and add it to the map.
                if ( atPos == null ) {
                    atPos = new Vector();
                    positionMap.put( coords, atPos );
                }

                // Add the entity to the vector for this position.
                atPos.addElement( entity );

            }
        } // Handle the next entity.

        // Return the map.
        return positionMap;                
    }

    /**
     * Returns an enumeration of salvagable entities.
     */
    public Enumeration getGraveyardEntities() {
        Vector graveyard = new Vector();
        
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if ( entity.getRemovalCondition() == Entity.REMOVE_SALVAGEABLE ||
                 entity.getRemovalCondition() == Entity.REMOVE_EJECTED ) {
                graveyard.addElement(entity);
            }
        }
        
        return graveyard.elements();
    }

    /**
     * Returns an enumeration of wrecked entities.
     */
    public Enumeration getWreckedEntities() {
        Vector wrecks = new Vector();
        
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if ( entity.getRemovalCondition() == Entity.REMOVE_SALVAGEABLE ||
                 entity.getRemovalCondition() == Entity.REMOVE_EJECTED ||
                 entity.getRemovalCondition() == Entity.REMOVE_DEVASTATED ) {
                wrecks.addElement(entity);
            }
        }
        
        return wrecks.elements();
    }

    /**
     * Returns an enumeration of entities that have retreated
     */
    public Enumeration getRetreatedEntities() {
        Vector sanctuary = new Vector();
        
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if ( entity.getRemovalCondition() == Entity.REMOVE_IN_RETREAT ||
                 entity.getRemovalCondition() == Entity.REMOVE_PUSHED ) {
                sanctuary.addElement(entity);
            }
        }
        
        return sanctuary.elements();
    }

    /**
     * Returns an enumeration of entities that were utterly destroyed
     */
    public Enumeration getDevastatedEntities() {
        Vector smithereens = new Vector();
        
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            if (entity.getRemovalCondition() == Entity.REMOVE_DEVASTATED) {
                smithereens.addElement(entity);
            }
        }
        
        return smithereens.elements();
    }
    
    /**
     * Return the current number of entities in the game.
     */
    public int getNoOfEntities() {
        return entities.size();
    }
    
    /**
     * Returns the appropriate target for this game given a type and id
     */
    public Targetable getTarget(int nType, int nID) {
        switch (nType) {
            case Targetable.TYPE_ENTITY :
                return getEntity(nID);
            case Targetable.TYPE_HEX_CLEAR :
            case Targetable.TYPE_HEX_IGNITE :
            case Targetable.TYPE_MINEFIELD_DELIVER :
                return new HexTarget(HexTarget.idToCoords(nID), board, nType);
            case Targetable.TYPE_BUILDING :
            case Targetable.TYPE_BLDG_IGNITE :
                return new BuildingTarget
                    ( BuildingTarget.idToCoords(nID), board, nType );
            case Targetable.TYPE_MINEFIELD_CLEAR :
                return new MinefieldTarget(MinefieldTarget.idToCoords(nID), board);
            default :
                return null;
        }
    }
    
    /**
     * Returns the entity with the given id number, if any.
     */

    public Entity getEntity(int id) {
        return (Entity)entityIds.get(new Integer(id));
    }
 
    public void addEntity(int id, Entity entity) {
        entity.setGame(this);
        entities.addElement(entity);
        entityIds.put(new Integer(id), entity);
    }
    
    public void setEntity(int id, Entity entity) {
        final Entity oldEntity = getEntity(id);
        entity.setGame(this);
        if (oldEntity == null) {
            entities.addElement(entity);
        } else {
            entities.setElementAt(entity, entities.indexOf(oldEntity));
        }
        entityIds.put(new Integer(id), entity);
    }
    
    /**
     * Returns true if an entity with the specified id number exists in this
     * game.
     */
    public boolean hasEntity(int entityId) {
        return entityIds.containsKey(new Integer(entityId));
    }
  

    /**
     * Remove an entity from the master list.  If we can't find that entity,
     * (probably due to double-blind) ignore it.
     */
    public void removeEntity( int id, int condition ) {
        Entity toRemove = getEntity(id);
        if (toRemove == null) {
            System.err.println("Game#removeEntity: could not find entity to remove");
            return;
        }
        
        entities.removeElement(toRemove);
        entityIds.remove(new Integer(id));
        
        toRemove.setRemovalCondition(condition);
        
        // do not keep never-joined entities
        if (vOutOfGame != null && condition != Entity.REMOVE_NEVER_JOINED) {
            vOutOfGame.addElement(toRemove);
        }
        
        //We also need to remove it from the list of things to be deployed... 
        //we might still be in this list if we never joined the game
          if ( deploymentTable.size() > 0 ) {
            Enumeration enum = deploymentTable.elements();
            
            while ( enum.hasMoreElements() ) {
              Vector vec = (Vector)enum.nextElement();
              
              for ( int i = vec.size() - 1; i >= 0; i-- ) {
                Entity en = (Entity)vec.elementAt(i);
                
                if ( en.getId() == id ) 
                  vec.removeElementAt(i);
              }
            }
          }
    }
    
    /**
     * Resets this game by removing all entities.
     */
    public void reset() {
        roundCount = 0;
        
        entities.removeAllElements();
        entityIds.clear();

        vOutOfGame.removeAllElements();
        
        resetActions();
        resetCharges();
        resetPSRs();
        removeMinefields();
        
        forceVictory = false;
        victoryPlayerId = Player.PLAYER_NONE;
        victoryTeam = Player.TEAM_NONE;
    }
    
    private void removeMinefields() {
        minefields.clear();
        vibrabombs.removeAllElements();

    Enumeration players = getPlayers();
    while (players.hasMoreElements()) {
      Player player = (Player) players.nextElement();
            player.removeMinefields();
    }
    }
    
    /**
     * Checks to see if we have an entity in the master list, and if so,
     * returns its id number.  Otherwise returns -1.
     */
    public int getEntityID(Entity entity) {
        for (Enumeration i = entityIds.keys();i.hasMoreElements();) {
            Integer key = (Integer)i.nextElement();
            if (entityIds.get(key) == entity) return key.intValue();
        }
        return -1;
    }
        
    /**
     * Regenerates the entities by id hashtable by going thru all entities
     * in the Vector
     */
    private void reindexEntities() {
        entityIds.clear();
        if ( entities != null ) {
            // Add these entities to the game.
            for (Enumeration i = entities.elements(); i.hasMoreElements();) {
                final Entity entity = (Entity)i.nextElement();
                entityIds.put(new Integer(entity.getId()), entity);
                entity.setGame(this);
            }
        }
    }
    
    /**
     * @deprecated use getFirstEntity instead
     */
    public Entity getEntity(Coords c) {
        return getFirstEntity(c);
    }
    
    /**
     * Returns the first entity at the given coordinate, if any.  Only returns
     * targetable (non-dead) entities.
     * 
     * @param c the coordinates to search at
     */
    public Entity getFirstEntity(Coords c) {
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            if (c.equals(entity.getPosition()) && entity.isTargetable()) {
                return entity;
            }
        }
        return null;
    }
    
    /**
     * Returns an Enumeration of the active entities at the given coordinates.
     */
    public Enumeration getEntities(Coords c) {
        Vector vector = new Vector();

        // Only build the list if the coords are off the board.
        if ( this.board.contains(c) ) {
            for (Enumeration i = entities.elements(); i.hasMoreElements();) {
                final Entity entity = (Entity)i.nextElement();
                if (c.equals(entity.getPosition()) && entity.isTargetable()) {
                    vector.addElement(entity);
                }
            }
        }

        return vector.elements();
    }

    /**
     * Moves an entity into the graveyard so it stops getting sent
     * out every phase.
     */
    public void moveToGraveyard(int id) {
        this.removeEntity( id, Entity.REMOVE_SALVAGEABLE );
    }
     
    /**
     * See if the <code>Entity</code> with the given ID is out of the game.
     *
     * @param id - the ID of the <code>Entity</code> to be checked.
     * @return  <code>true</code> if the <code>Entity</code> is in the
     *    graveyard, <code>false</code> otherwise.
     */
    public boolean isOutOfGame( int id ) {
        for (Enumeration i = vOutOfGame.elements(); i.hasMoreElements();) {
            Entity entity = (Entity)i.nextElement();
            
            if (entity.getId() == id) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * See if the <code>Entity</code> is out of the game.
     *
     * @param entity - the <code>Entity</code> to be checked.
     * @return  <code>true</code> if the <code>Entity</code> is in the
     *    graveyard, <code>false</code> otherwise.
     */
    public boolean isOutOfGame( Entity entity ) {
        return isOutOfGame(entity.getId());
    }
    
    /**
     * Returns the first entity that can act in the present turn, or null if
     * none can.
     */
    public Entity getFirstEntity() {
        return getFirstEntity(getTurn());
    }
    
    /**
     * Returns the first entity that can act in the specified turn, or null if
     * none can.33
     */
    public Entity getFirstEntity(GameTurn turn) {
        return getEntity(getFirstEntityNum(getTurn()));
    }
    
    /**
     * Returns the id of the first entity that can act in the current turn,
     * or -1 if none can.
     */
    public int getFirstEntityNum() {
        return getFirstEntityNum(getTurn());
    }
    
    /**
     * Returns the id of the first entity that can act in the specified turn,
     * or -1 if none can.
     */
    public int getFirstEntityNum(GameTurn turn) {
        if (turn == null) {
            return -1;
        }
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            
            if (turn.isValidEntity(entity, this)) {
                return entity.getId();
            }
        }
        return -1;
    }
    
    /**
     * Returns the next selectable entity that can act this turn,
     * or null if none can.
     * 
     * @param start the index number to start at
     */
    public Entity getNextEntity(int start) {
        return getEntity(getNextEntityNum(getTurn(), start));
    }

    public int getNextEntityNum(int start) {
        return getNextEntityNum(getTurn(), start);
    }

    /**
     * Returns the entity id of the next entity that can move during the 
     * specified
     * 
     * @param turn the turn to use
     * @param start the entity id to start at
     */
    public int getNextEntityNum(GameTurn turn, int start) {
        boolean startPassed = false;
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            if (entity.getId() == start) {
                startPassed = true;
            } else if (startPassed && turn.isValidEntity(entity, this)) {
                return entity.getId();
            }
        }
        return getFirstEntityNum(turn);
    }

    /**
     * Returns the number of the first deployable entity
     */
    public int getFirstDeployableEntityNum() {
      return getFirstDeployableEntityNum(getTurn());
    }
    
    public int getFirstDeployableEntityNum(GameTurn turn) {
      int num = getFirstEntityNum(turn);
      Entity en = getEntity(num);
      
      while ( (null == en) || ((null != en) && !en.shouldDeploy(getRoundCount())) ) {
        num = getNextEntityNum(turn, num);
        en = getEntity(num);
      }
      
      //Sanity check
      return ((null != en) && en.shouldDeploy(getRoundCount())) ? num : -1;
    }
    
    /**
     * Returns the number of the next deployable entity
     */
    public int getNextDeployableEntityNum(int entityId) {
      return getNextDeployableEntityNum(getTurn(), entityId);
    }
    
    public int getNextDeployableEntityNum(GameTurn turn, int entityId) {
      int num = getNextEntityNum(turn, entityId);
      Entity en = getEntity(num);
      
      while ( (null == en) || ((null != en) && !en.shouldDeploy(getRoundCount())) ) {
        num = getNextDeployableEntityNum(turn, num);
        en = getEntity(num);
      }
      
      //Sanity check
      return ((null != en) && en.shouldDeploy(getRoundCount())) ? num : -1;
    }
    

    public void determineWindDirection() {
        windDirection = Compute.d6(1)-1;
        String[] dirNames = {"North", "Northeast", "Southeast", "South", "Southwest", "Northwest"};
        stringWindDirection = dirNames[windDirection];
    }
    
    public int getWindDirection() {
        return windDirection;
    }
    
    public String getStringWindDirection() {
        return stringWindDirection;
    }
    
    /**
     * Get the entities for the player.
     *
     * @param   player - the <code>Player</code> whose entities are required.
     * @return  a <code>Vector</code> of <code>Entity</code>s.
     */
    public Vector getPlayerEntities( Player player ) {
        Vector output = new Vector();
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            if ( player.equals(entity.getOwner()) ) {
                output.addElement( entity );
            }
        }
        return output;
    }
    
    /**
     * Determines if the indicated player has any remaining selectable infanty.
     *
     * @param playerId - the <code>int</code> ID of the player
     * @return  <code>true</code> if the player has any remaining
     *    active infantry, <code>false</code> otherwise.
     */
    public boolean hasInfantry( int playerId ) {
  Player player = this.getPlayer( playerId );

        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            if ( player.equals(entity.getOwner()) &&
     entity.isSelectableThisTurn(this) &&
     entity instanceof Infantry ) {
                return true;
            }          
        }
        return false;
    }
    
    /**
     * Returns the number of remaining selectable infantry owned by a player.
     */
    public int infantryLeft(int playerId) {
  Player player = this.getPlayer( playerId );
        int remaining = 0;

        for (Enumeration i = entities.elements(); i.hasMoreElements();) {
            final Entity entity = (Entity)i.nextElement();
            if ( player.equals(entity.getOwner()) &&
     entity.isSelectableThisTurn(this) &&
     entity instanceof Infantry ) {
                remaining++;
            }          
        }
        
        return remaining;
    }
    
    /**
     * Removes the last, next turn found that the specified entity can move in.
     * Used when, say, an entity dies mid-phase.
     */
    public void removeTurnFor(Entity entity) {
        for (int i = turnVector.size() - 1; i >= turnIndex; i--) {
            GameTurn turn = (GameTurn)turnVector.elementAt(i);
            if (turn.isValidEntity(entity, this)) {
                turnVector.removeElementAt(i);
                break;
            }
        }
    }

    /**
     * Check each player for the presence of a Battle Armor squad equipped
     * with a Magnetic Clamp.  If one unit is found, update that player's
     * units to allow the squad to be transported.
     * <p/>
     * This method should be called </b>*ONCE*</b> per game, after all units
     * for all players have been loaded.
     *
     * @return  <code>true</code> if a unit was updated, <code>false</code>
     *          if no player has a Battle Armor squad equipped with a
     *          Magnetic Clamp.
     */
    public boolean checkForMagneticClamp() {

        // Declare local variables.
        Player          player = null;
        Entity          unit = null;
        boolean         result;
        Hashtable       playerFlags = null;
        Enumeration     enum = null;
        Mounted         equip = null;
        String          name = null;

        // Assume that we don't need new transporters.
        result = false;

        // Create a map of flags for the players.
        playerFlags = new Hashtable( this.getNoOfPlayers() );

        // Walk through the game's entities.
        for (Enumeration i = entities.elements(); i.hasMoreElements();) {

            // Is the next unit a Battle Armor squad?
            unit = (Entity)i.nextElement();
            if ( unit instanceof BattleArmor ) {

                // Does the unit have a Magnetic Clamp?
                enum = unit.getMisc();
                while ( enum.hasMoreElements() ) {
                    equip = (Mounted) enum.nextElement();
                    name = equip.getType().getInternalName();
                    if ( BattleArmor.MAGNETIC_CLAMP.equals( name ) ){
                        // The unit's player needs new transporters.
                        result = true;
                        playerFlags.put( unit.getOwner(), Boolean.TRUE );

                        // Stop looking.
                        break;
                    }
                }

            } // End unit-is-BattleArmor

        } // Handle the next entity.

        // Do we need to add any Magnetic Clamp transporters?
        if ( result ) {

            // Walk through the game's entities again.
            for (Enumeration i = entities.elements(); i.hasMoreElements();) {

                // Get this unit's player.
                unit = (Entity)i.nextElement();
                player = unit.getOwner();

                // Does this player need updated transporters?
                if ( Boolean.TRUE.equals( playerFlags.get(player) ) ) {

                    // Add the appropriate transporter to the unit.
                    if ( !unit.isOmni() && unit instanceof Mech ) {
                        unit.addTransporter( new ClampMountMech() );
                    }
                    else if ( unit instanceof Tank ) {
                        unit.addTransporter( new ClampMountTank() );
                    }

                }
            } // End player-needs-transports

        } // Handle the next unit.

        // Return the result.
        return result;

    } // End private boolean checkForMagneticClamp()
    
    /** Adds the specified action to the actions list for this phase. */
    public void addAction(EntityAction ea) {
        actions.addElement(ea);
    }
    
    /** Returns an Enumeration of actions scheduled for this phase. */
    public Enumeration getActions() {
        return actions.elements();
    }
    
    /** Resets the actions list. */
    public void resetActions() {
        actions.removeAllElements();
    }
    
    /** Removes all actions by the specified entity */
    public void removeActionsFor(int entityId) {
        // or rather, only keeps actions NOT by that entity
        Vector toKeep = new Vector(actions.size());
        for (Enumeration i = actions.elements(); i.hasMoreElements();) {
            EntityAction ea = (EntityAction)i.nextElement();
            if (ea.getEntityId() != entityId) {
                toKeep.addElement(ea);
            }
        }
        this.actions = toKeep;
    }
    
    public int actionsSize() {
        return actions.size();
    }
    
    /** Returns the actions vector.  Do not use to modify the actions;
     * I will be angry. >:[  Used for sending all actions to the client.
     */
    public Vector getActionsVector() {
        return actions;
    }
    
    /** Adds a pending displacement attack to the list for this phase. */
    public void addCharge(AttackAction ea) {
        pendingCharges.addElement(ea);
    }
    
    /** 
     * Returns an Enumeration of displacement attacks scheduled for the end
     * of the physical phase.
     */
    public Enumeration getCharges() {
        return pendingCharges.elements();
    }
    
    /** Resets the pending charges list. */
    public void resetCharges() {
        pendingCharges.removeAllElements();
    }
    
    /** Returns the charges vector.  Do not modify. >:[ Used for sending all 
     * charges to the client.
     */
    public Vector getChargesVector() {
        return pendingCharges;
    }
    
    /** Adds a pending PSR to the list for this phase. */
    public void addPSR(PilotingRollData psr) {
        pilotRolls.addElement(psr);
    }
    
    /** Returns an Enumeration of pending PSRs. */
    public Enumeration getPSRs() {
        return pilotRolls.elements();
    }
    
    /** Resets the PSR list. */
    public void resetPSRs() {
        pilotRolls.removeAllElements();
    }
    
    /** Getter for property roundCount.
     * @return Value of property roundCount.
     */
    public int getRoundCount() {
        return roundCount;
    }
    
    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }
    
    /** Increments the round counter */
    public void incrementRoundCount() {
        roundCount++;
    }
    
    /** Getter for property forceVictory.
     * @return Value of property forceVictory.
     */
    public boolean isForceVictory() {
        return forceVictory;
    }
    
    /** Setter for property forceVictory.
     * @param forceVictory New value of property forceVictory.
     */
    public void setForceVictory(boolean forceVictory) {
        this.forceVictory = forceVictory;
    }
    
    /** Getter for property roundReport.
     * @return Value of property roundReport.
     */
    public java.lang.StringBuffer getRoundReport() {
        return roundReport;
    }
    
    /** Resets the round report */
    public void resetRoundReport() {
        this.roundReport = new StringBuffer();
    }
    
    /** Getter for property phaseReport.
     * @return Value of property phaseReport.
     */
    public java.lang.StringBuffer getPhaseReport() {
        return phaseReport;
    }
    
    /** Resets the round report */
    public void resetPhaseReport() {
        this.phaseReport = new StringBuffer();
    }
    
    /** Getter for property victoryPlayerId.
     * @return Value of property victoryPlayerId.
     */
    public int getVictoryPlayerId() {
        return victoryPlayerId;
    }
    
    /** Setter for property victoryPlayerId.
     * @param victoryPlayerId New value of property victoryPlayerId.
     */
    public void setVictoryPlayerId(int victoryPlayerId) {
        this.victoryPlayerId = victoryPlayerId;
    }
    
    /** Getter for property victoryTeam.
     * @return Value of property victoryTeam.
     */
    public int getVictoryTeam() {
        return victoryTeam;
    }
    
    /** Setter for property victoryTeam.
     * @param victoryTeam New value of property victoryTeam.
     */
    public void setVictoryTeam(int victoryTeam) {
        this.victoryTeam = victoryTeam;
    }
    
    /**
     * Returns true if the specified player is either the victor, or is on the
     * winning team.  Best to call during PHASE_VICTORY.
     */
    public boolean isPlayerVictor(Player player) {
        if (player.getTeam() == Player.TEAM_NONE) {
            return player.getId() == victoryPlayerId;
        } else {
            return player.getTeam() == victoryTeam;
        }
    }
    /** Shortcut to isPlayerVictor(Player player) */
    public boolean isPlayerVictor(int playerId) {
        return isPlayerVictor(getPlayer(playerId));
    }
}
