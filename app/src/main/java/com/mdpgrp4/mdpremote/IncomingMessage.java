package com.mdpgrp4.mdpremote;

/**
 * Created by felix on 10/3/2017.
 */

public class IncomingMessage {
    public final String robotStatus;
    public final int[] robotPosition;
    public final Integer robotOrientation;
    public final String mapObstacle;
    public final String mapExplored;

    public IncomingMessage(String robotStatus, int[] robotPosition, Integer robotOrientation,
                           String mapObstacle, String mapExplored) {
        this.robotStatus = robotStatus;
        this.robotPosition = robotPosition;
        this.robotOrientation = robotOrientation;
        this.mapObstacle = mapObstacle;
        this.mapExplored = mapExplored;
    }
}