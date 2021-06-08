package ru.vsu.cs.skofenko;

import java.util.*;

public class WGraph {
    static class Pair implements Comparable<Pair> {
        int to;
        int l;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return to == pair.to && l == pair.l;
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, l);
        }

        public Pair(int to, int l) {
            this.to = to;
            this.l = l;
        }

        @Override
        public int compareTo(Pair o) {
            return l - o.l;
        }
    }

    private final List<List<Pair>> adjacencyList = new ArrayList<>();
    private int edgeCount = 0;

    private int min = -1;
    private Pair[] bestWay = null;

    public int getMin() {
        return min;
    }

    public Pair[] getBestWay() {
        return bestWay;
    }

    public WGraph(int v) {
        for (int i = 0; i < v; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public void addEdge(int a, int b, int l) {
        adjacencyList.get(a).add(new Pair(b, l));
        adjacencyList.get(b).add(new Pair(a, l));
        min = -1;
        edgeCount++;
    }

    public void removeVert(int v) {
        int count = adjacencyList.get(v).size();
        adjacencyList.remove(v);
        for (List<Pair> list : adjacencyList) {
            for (var it = list.iterator(); it.hasNext(); ) {
                var pair = it.next();
                if (pair.to == v) {
                    it.remove();
                    count++;
                } else if (pair.to > v) {
                    pair.to--;
                }
            }
        }
        edgeCount -= count;
        min = -1;
    }

    public void removeEdge(int fr, int to) {
        for (var it = adjacencyList.get(fr).iterator(); it.hasNext(); ) {
            if (it.next().to == to) {
                it.remove();
                edgeCount--;
                break;
            }
        }
        for (var it = adjacencyList.get(to).iterator(); it.hasNext(); ) {
            if (it.next().to == fr) {
                it.remove();
                edgeCount--;
                break;
            }
        }
        min = -1;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public List<Pair> getEdges(int a) {
        return adjacencyList.get(a);
    }

    public int numOfVert() {
        return adjacencyList.size();
    }

    public boolean isAdj(int v1, int v2) {
        for (Pair pair : adjacencyList.get(v1)) {
            if (pair.to == v2)
                return true;
        }
        return false;
    }

    public void addVertex() {
        adjacencyList.add(new ArrayList<>());
        min = -1;
    }

    public void findMinVert() {
        int sum = Integer.MAX_VALUE;
        for (int i = 0; i < numOfVert(); i++) {
            sum = Math.min(sum, dijkstra(i, sum));
        }
    }

    private int dijkstra(int i, int exSum) {
        Pair[] from = new Pair[numOfVert()];
        FibonacciHeap<Integer> pQueue = new FibonacciHeap<>();
        ArrayList<FibonacciHeap<Integer>.Node> paths = new ArrayList<>();
        for (int j = 0; j < numOfVert(); j++) {
            if (i != j)
                paths.add(pQueue.add(j, Integer.MAX_VALUE));
            else
                paths.add(pQueue.add(i, 0));

        }
        for (int j = 0; j < paths.size() - 1; j++) {
            int v = pQueue.poll().getElem();
            List<Pair> list = getEdges(v);
            for (Pair pair : list) {
                if (paths.get(pair.to).getPriority() > paths.get(v).getPriority() + pair.l) {
                    from[pair.to] = new Pair(v, pair.l);
                    pQueue.decreaseKey(paths.get(pair.to), paths.get(v).getPriority() + pair.l);
                }
            }
        }
        int sum = 0;
        for (var node : paths) {
            sum += node.getPriority();
        }
        if (exSum > sum) {
            min = i;
            bestWay = from;
        }
        return sum;
    }
}
