import java.util.HashMap;
import java.util.Map;

class LFUCacheNode {
    Integer key;
    Integer value;
    Integer frequency;
    LFUCacheNode prev;
    LFUCacheNode next;

    public LFUCacheNode(int key, int value) {
        this.key = key;
        this.value = value;
        this.frequency = 1;
    }

    public Integer getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public LFUCacheNode getPrev() {
        return prev;
    }

    public LFUCacheNode getNext() {
        return next;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }
}

class DLL {
    LFUCacheNode head;
    LFUCacheNode tail;
    Integer size;

    DLL() {
        head = new LFUCacheNode(0, 0);
        tail = new LFUCacheNode(0, 0);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    void addFirst(LFUCacheNode node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
        size++;
    }

    void remove(LFUCacheNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size--;
    }

    LFUCacheNode removeLast() {
        if (size == 0) {
            return null;
        }
        LFUCacheNode last = tail.prev;
        remove(last);
        return last;
    }
}

public class LFUCacheWithDLLLRU {
    private Integer capacity;
    private Integer minFreq;
    private Map<Integer, LFUCacheNode> cache;
    private Map<Integer, DLL> freqMap;

    LFUCacheWithDLLLRU(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.minFreq = 0;
        this.freqMap = new HashMap<>();
    }

    private void update(LFUCacheNode node) {
        DLL currList = freqMap.get(node.getFrequency());
        currList.remove(node);
        if (currList.size == 0 && node.getFrequency() == minFreq) {
            minFreq++;
        }
        node.setFrequency(node.getFrequency() + 1);
        if (!freqMap.containsKey(node.getFrequency())) {
            freqMap.put(node.getFrequency(), new DLL());
        }
        freqMap.get(node.getFrequency()).addFirst(node);
    }

    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        LFUCacheNode node = cache.get(key);
        update(node);
        return node.getValue();
    }

    public void put(int key, int value) {
        if (capacity == 0) {
            return;
        }
        if (cache.containsKey(key)) {
            LFUCacheNode node = cache.get(key);
            node.setValue(value);
            update(node);
            return;
        }
        if (cache.size() == capacity) {
            DLL minFreqList = freqMap.get(minFreq);
            LFUCacheNode removed = minFreqList.removeLast();
            cache.remove(removed.getKey());
        }
        LFUCacheNode newNode = new LFUCacheNode(key, value);
        cache.put(key, newNode);
        if (!(freqMap.containsKey(1))) {
            freqMap.put(1, new DLL());
        }
        freqMap.get(1).addFirst(newNode);
        minFreq = 1;
    }
}

