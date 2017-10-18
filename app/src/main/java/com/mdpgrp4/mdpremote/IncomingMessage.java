package com.mdpgrp4.mdpremote;

/**
 * Created by felix on 10/3/2017.
 */

public class IncomingMessage {
    public final String robotStatus;
    public final String[] robotPosition;
    public final String mapObstacle;
    public final String mapExplored;
    public final String grid;

    public IncomingMessage(String robotStatus, String[] robotPosition,
                           String mapObstacle, String mapExplored, String grid) {
        this.robotStatus = robotStatus;
        this.robotPosition = robotPosition;
        this.mapObstacle = mapObstacle;
        this.mapExplored = mapExplored;
        this.grid = grid;
    }
}
