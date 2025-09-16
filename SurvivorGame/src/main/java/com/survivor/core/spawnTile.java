package com.survivor.core;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.CellState;
import com.survivor.util.aStarGrid;
public class spawnTile extends Component {
    private int w, h;
    private int x, y;

    public spawnTile(int w, int h) {
        this.w = w/16;
        this.h = h/16;
    }

    @Override
    public void onAdded() {
        x =  (int)Math.round(entity.getPosition().getX())/16;
        y = (int)Math.round( entity.getPosition().getY())/16;
//        for(int i = Math.max(x-1,0);i<Math.min(w+x+1,600);i++)
//        {
//            for(int j = Math.max(y,0);j<Math.min(h+y+1,350);j++)
//            {
//                aStarGrid.grid.get(i,j).setState(CellState.NOT_WALKABLE);
//                System.out.println(aStarGrid.grid.get(i,j));
//            }
//        }
        for(int i = Math.max(x-1,0);i<Math.min(w+x+1,600);i++)
        {
            for(int j = Math.max(y,0);j<Math.min(h+y,350);j++)
            {
                aStarGrid.grid.get(i,j).setState(CellState.NOT_WALKABLE);
//                System.out.println(aStarGrid.grid.get(i,j));
            }
        }
    }
}
