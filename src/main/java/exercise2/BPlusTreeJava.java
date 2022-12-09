package exercise2;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.exercise2.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;

/**
 * This is the B+-Tree implementation you will work on.
 * Your task is to implement the insert-operation.
 *
 */
@ChosenImplementation(true)
public class BPlusTreeJava extends AbstractBPlusTree {
    public BPlusTreeJava(int order) {
        super(order);
    }

    public BPlusTreeJava(BPlusTreeNode<?> rootNode) {
        super(rootNode);
    }

    // return the position the key would be inserted at
    private int keyPosition(BPlusTreeNode node, int key) {
        int keyPosition = 0;
        while (keyPosition + 1 < order && node.keys[keyPosition] != null && node.keys[keyPosition] < key) {
            keyPosition++;
        }
        return keyPosition;
    }

    private int takeHighestKey(InnerNode node) {
        int lastIndex = order - 2;
        while (node.keys[lastIndex] == null) lastIndex--;
        assert lastIndex >= 0;
        int key = node.keys[lastIndex];
        node.keys[lastIndex] = null;
        return key;
    }

    private <T> void insertKey(BPlusTreeNode<T> node, boolean nodeIsLeaf, int keyPosition, int key, T reference) {
        assert !node.isFull();
        int referencePosition = keyPosition;
        if (!nodeIsLeaf) referencePosition++;

        for (int shiftDestination = node.keys.length - 1; shiftDestination > keyPosition; shiftDestination--) {
            node.keys[shiftDestination] = node.keys[shiftDestination - 1];
        }
        for (int shiftDestination = node.references.length - 1; shiftDestination > referencePosition; shiftDestination--) {
            node.references[shiftDestination] = node.references[shiftDestination - 1];
        }
        node.keys[keyPosition] = key;
        node.references[referencePosition] = reference;
    }

    private <T> void insertAndSplit(int keyPosition, int key, T reference, boolean nodesAreLeaves,
                                    BPlusTreeNode<T> originalNode,
                                    BPlusTreeNode<T> leftNode,
                                    BPlusTreeNode<T> rightNode) {
        assert originalNode.isFull();
        int referencePosition = keyPosition;
        if (!nodesAreLeaves) referencePosition++;

        ArrayList<Integer> keyList = new ArrayList<Integer>(Arrays.asList(originalNode.keys));
        ArrayList<T> referenceList = new ArrayList<T>(Arrays.asList(originalNode.references));

        keyList.add(keyPosition, key);
        referenceList.add(referencePosition, reference);

        int keysInRightLeaf = order / 2;
        int keysInLeftLeaf = order - keysInRightLeaf;
        int keysPerNode = order - 1;

        for (int i = 0; i < keysPerNode; i++) {
            if (i < keysInLeftLeaf) {
                leftNode.keys[i] = keyList.get(i);
                leftNode.references[i] = referenceList.get(i);
            } else {
                leftNode.keys[i] = null;
                leftNode.references[i] = null;
            }

            if (i < keysInRightLeaf) {
                rightNode.keys[i] = keyList.get(keysInLeftLeaf + i);
                rightNode.references[i] = referenceList.get(keysInLeftLeaf + i);
            } else {
                rightNode.keys[i] = null;
                rightNode.references[i] = null;
            }
        }
        if (!nodesAreLeaves) {
            rightNode.references[keysInRightLeaf] = referenceList.get(order);
            leftNode.references[order - 1] = null;
            rightNode.references[order - 1] = null;
        }
    }

    private void splitInitialRoot(int entryPosition, @NotNull int key, @NotNull ValueReference value) {
        assert getHeight() == 0;
        InitialRootNode initialRoot = (InitialRootNode) rootNode;

        LeafNode leftChild = new LeafNode(order);
        LeafNode rightChild = new LeafNode(order);
        leftChild.nextSibling = rightChild;

        insertAndSplit(entryPosition, key, value, true, initialRoot, leftChild, rightChild);

        rootNode = new InnerNode(order, leftChild, rightChild);
    }

    @Nullable
    @Override
    public ValueReference insert(@NotNull Integer key, @NotNull ValueReference value) {

        LeafNode leaf = rootNode.findLeaf(key);
        int leafSize = leaf.getNodeSize();
        int entryPosition = keyPosition(leaf, key);

        if (entryPosition < leafSize && leaf.keys[entryPosition] == key) {
            ValueReference oldValue = leaf.references[entryPosition];
            leaf.references[entryPosition] = value;
            return oldValue;
        }

        if (leaf.isFull()) {
            if (getHeight() == 0) {
                splitInitialRoot(entryPosition, key, value);

            } else {
                Stack<InnerNode> parents = new Stack<InnerNode>();
                InnerNode currentParent = (InnerNode) rootNode;
                parents.push(currentParent);
                for (int level = 1; level < getHeight(); level++) {
                    currentParent = (InnerNode) currentParent.selectChild(key);
                    parents.push(currentParent);
                }

                // leaf case
                LeafNode newLeaf = new LeafNode(order);
                insertAndSplit(entryPosition, key, value, true, leaf, leaf, newLeaf);
                newLeaf.nextSibling = leaf.nextSibling;
                leaf.nextSibling = newLeaf;
                
                Integer parentKey = newLeaf.keys[0];
                BPlusTreeNode nodeReference = newLeaf;

                while (parentKey != null && !parents.isEmpty()) {
                    InnerNode parent = parents.pop();
                    int keyPosition = keyPosition(parent, parentKey);

                    if (parent.isFull()) {
                        InnerNode newNode = new InnerNode(order);
                        insertAndSplit(keyPosition, parentKey, nodeReference, false,
                                parent, parent, newNode);
                        parentKey = takeHighestKey(parent);
                        nodeReference = newNode;
                    } else {
                        insertKey(parent, false, keyPosition, parentKey, nodeReference);
                        parentKey = null;
                    }
                }

                if (parentKey != null) {
                    InnerNode newRoot = new InnerNode(order, rootNode, nodeReference);
                    rootNode = newRoot;
                }
            }
        } else {
            insertKey(leaf, true, entryPosition, key, value);
        }

        return null;
    }
}
