/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.vulkanmod.render.profiling;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class Profiler {
    private static final boolean DEBUG = false;
    private static final boolean FORCE_ACTIVE = false;
    private static final int NANOS_IN_MS = 1000000;
    private static final float CONVERSION = 1000000.0f;
    private static final float INV_CONVERSION = 1.0E-6f;
    private static final int SAMPLE_COUNT = 200;
    public static boolean ACTIVE = false;
    private static final Profiler MAIN_PROFILER = new Profiler("Main");
    private final String name;
    LongArrayList startTimes = new LongArrayList();
    ObjectArrayList<Node> nodeStack = new ObjectArrayList();
    ObjectArrayList<Node> nodes = new ObjectArrayList();
    ObjectArrayList<Node> currentFrameNodes = new ObjectArrayList();
    Object2ReferenceOpenHashMap<String, Node> nodeMap = new Object2ReferenceOpenHashMap();
    Node mainNode;
    Node selectedNode;
    Node currentNode;
    ProfilerResults profilerResults = new ProfilerResults();

    public static Profiler getMainProfiler() {
        return MAIN_PROFILER;
    }

    public static void setActive(boolean b) {
        ACTIVE = b;
    }

    public Profiler(String s) {
        this.name = s;
        this.selectedNode = this.mainNode = new Node(s);
        this.currentNode = this.mainNode;
    }

    public void push(String s) {
        if (!ACTIVE) {
            return;
        }
        Node node = (Node)this.nodeMap.get((Object)s);
        if (node == null) {
            node = new Node(s);
            this.nodeMap.put((Object)s, (Object)node);
            this.currentNode.addChild(node);
        }
        node.setParent(this.currentNode);
        node.children.clear();
        if (node.parent == this.selectedNode) {
            this.currentFrameNodes.add((Object)node);
        }
        this.currentNode = node;
        this.pushNodeStack(node);
    }

    private void pushNodeStack(Node node) {
        long startTime = System.nanoTime();
        this.startTimes.push(startTime);
        this.nodeStack.push((Object)node);
    }

    public void pop() {
        if (!ACTIVE) {
            return;
        }
        if (this.nodeStack.isEmpty()) {
            return;
        }
        int i = this.nodeStack.size() - 1;
        Node node = (Node)this.nodeStack.remove(i);
        long startTime = this.startTimes.removeLong(i);
        long deltaMs = System.nanoTime() - startTime;
        node.push(deltaMs);
        this.currentNode = this.currentNode.parent;
    }

    public void start() {
        if (!ACTIVE) {
            return;
        }
        if (!this.nodeStack.isEmpty()) {
            this.nodeStack.clear();
            this.startTimes.clear();
        }
        this.currentNode = this.mainNode;
        this.mainNode.children.clear();
        this.pushNodeStack(this.mainNode);
        ObjectArrayList<Node> t = this.nodes;
        this.nodes = this.currentFrameNodes;
        this.currentFrameNodes = t;
        this.currentFrameNodes.clear();
    }

    public void end() {
        if (!ACTIVE) {
            return;
        }
        this.pop();
    }

    public void round() {
        this.end();
        this.start();
    }

    public ProfilerResults getProfilerResults() {
        this.profilerResults.update(this.selectedNode, (List<Node>)this.nodes);
        return this.profilerResults;
    }

    public static class ProfilerResults {
        Result result;
        ObjectArrayList<Result> partialResults = new ObjectArrayList();

        public void update(Node mainNode, List<Node> nodes) {
            mainNode.updateResult();
            this.result = mainNode.result;
            this.partialResults.clear();
            for (Node node : nodes) {
                node.updateResult();
                this.partialResults.push((Object)node.result);
            }
        }

        public Result getResult() {
            return this.result;
        }

        public ObjectArrayList<Result> getPartialResults() {
            return this.partialResults;
        }
    }

    public static class Node {
        final String name;
        Node parent;
        List<Node> children = new ObjectArrayList();
        long maxDuration;
        long minDuration;
        LongArrayFIFOQueue values = new LongArrayFIFOQueue(200);
        long accumulatedDuration;
        Result result;

        Node(String name) {
            this.name = name;
            this.result = new Result(name);
            this.reset();
        }

        void setParent(Node node) {
            this.parent = node;
            node.addChild(this);
        }

        void addChild(Node node) {
            this.children.add(node);
        }

        void push(long duration) {
            if (duration < this.minDuration) {
                this.minDuration = duration;
            }
            if (duration > this.maxDuration) {
                this.maxDuration = duration;
            }
            if (this.values.size() >= 200) {
                this.accumulatedDuration -= this.values.dequeueLong();
            }
            this.values.enqueue(duration);
            this.accumulatedDuration += duration;
        }

        public void updateResult() {
            this.result.setValue((float)this.accumulatedDuration / (float)this.values.size() * 1.0E-6f);
        }

        void reset() {
            this.minDuration = Long.MAX_VALUE;
            this.maxDuration = Long.MIN_VALUE;
            this.accumulatedDuration = 0L;
        }

        public String toString() {
            return this.name;
        }
    }

    public static class Result {
        public final String name;
        public float value;

        public Result(String name) {
            this.name = name;
        }

        void setValue(float value) {
            this.value = value;
        }
    }
}

