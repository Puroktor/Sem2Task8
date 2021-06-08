package ru.vsu.cs.skofenko;

import java.util.ArrayList;

public class WGraphWithCord extends WGraph {
    static class Cord {
        int x;
        int y;

        public Cord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private final ArrayList<Cord> listWithCord = new ArrayList<>();

    public WGraphWithCord(int v) {
        super(v);
    }

    @Override
    public void removeVert(int v) {
        super.removeVert(v);
        listWithCord.remove(v);
    }

    public void addVertex(int x, int y) {
        listWithCord.add(new Cord(x, y));
        addVertex();
    }

    public ArrayList<Cord> getListWithCord() {
        return listWithCord;
    }
}
