package ru.vsu.cs.skofenko;

import java.util.*;

public final class FibonacciHeap<T> {
    class Node {
        private int degree = 0;
        private boolean marked = false;

        private Node next, prev, parent, child;

        private T elem;
        private int priority;

        public void setElem(T elem) {
            this.elem = elem;
        }

        public T getElem() {
            return elem;
        }

        public int getPriority() {
            return priority;
        }

        private Node(T elem, int priority) {
            next = prev = this;
            this.elem = elem;
            this.priority = priority;
        }
    }

    private Node min = null;

    private int size = 0;

    public Node add(T value, int priority) {
        Node result = new Node(value, priority);
        min = mergeLists(min, result);
        size++;
        return result;
    }

    public Node min() {
        if (isEmpty())
            throw new NoSuchElementException();
        return min;
    }

    public boolean isEmpty() {
        return min == null;
    }

    public int size() {
        return size;
    }

    public Node poll() {
        if (isEmpty())
            throw new NoSuchElementException();
        size--;
        Node minElem = min;
        if (min.next == min) {
            min = null;
        } else {
            min.prev.next = min.next;
            min.next.prev = min.prev;
            min = min.next;
        }

        if (minElem.child != null) {
            Node curr = minElem.child;
            do {
                curr.parent = null;
                curr = curr.next;
            } while (curr != minElem.child);
        }
        min = mergeLists(min, minElem.child);
        if (min == null) return minElem;
        consolidate();
        return minElem;
    }

    private void consolidate() {
        List<Node> treeTable = new ArrayList<>();
        List<Node> toVisit = new ArrayList<>();

        for (Node curr = min; toVisit.isEmpty() || toVisit.get(0) != curr; curr = curr.next)
            toVisit.add(curr);
        for (Node curr : toVisit) {
            while (true) {
                while (curr.degree >= treeTable.size())
                    treeTable.add(null);

                if (treeTable.get(curr.degree) == null) {
                    treeTable.set(curr.degree, curr);
                    break;
                }

                Node other = treeTable.get(curr.degree);
                treeTable.set(curr.degree, null);
                Node min = (other.priority < curr.priority) ? other : curr;
                Node max = (other.priority < curr.priority) ? curr : other;
                max.next.prev = max.prev;
                max.prev.next = max.next;
                max.next = max.prev = max;
                min.child = mergeLists(min.child, max);
                max.parent = min;
                max.marked = false;
                min.degree++;
                curr = min;
            }
            if (curr.priority <= min.priority) min = curr;
        }
    }

    public void decreaseKey(Node Node, int newPriority) {
        if (newPriority > Node.priority)
            throw new IllegalArgumentException();

        Node.priority = newPriority;
        if (Node.parent != null && Node.priority <= Node.parent.priority)
            cutNode(Node);
        if (Node.priority <= min.priority)
            min = Node;
    }

    private static <T> FibonacciHeap<T>.Node mergeLists(FibonacciHeap<T>.Node one, FibonacciHeap<T>.Node two) {
        if (one == null && two == null) {
            return null;
        } else if (one != null && two == null) {
            return one;
        } else if (one == null) {
            return two;
        } else {
            FibonacciHeap<T>.Node oneNext = one.next;
            one.next = two.next;
            one.next.prev = one;
            two.next = oneNext;
            two.next.prev = two;
            return one.priority < two.priority ? one : two;
        }
    }

    private void cutNode(Node Node) {
        Node.marked = false;
        if (Node.parent == null) return;
        if (Node.next != Node) {
            Node.next.prev = Node.prev;
            Node.prev.next = Node.next;
        }
        if (Node.parent.child == Node) {
            if (Node.next != Node) {
                Node.parent.child = Node.next;
            } else {
                Node.parent.child = null;
            }
        }
        Node.parent.degree--;
        Node.prev = Node.next = Node;
        min = mergeLists(min, Node);
        if (Node.parent.marked)
            cutNode(Node.parent);
        else
            Node.parent.marked = true;
        Node.parent = null;
    }
}