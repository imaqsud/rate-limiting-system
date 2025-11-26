import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

class RankBasedCacheWithLRUNode {
    Integer key;
    Integer value;
    Integer rank;
    RankBasedCacheWithLRUNode prev;
    RankBasedCacheWithLRUNode next;

    public RankBasedCacheWithLRUNode(int key, int value, int rank) {
        this.key = key;
        this.value = value;
        this.rank = rank;
    }

    public Integer getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getRank() {
        return rank;
    }

    public RankBasedCacheWithLRUNode getPrev() {
        return prev;
    }

    public RankBasedCacheWithLRUNode getNext() {
        return next;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}

class DLLForRankBasedWithLRU {
    RankBasedCacheWithLRUNode head;
    RankBasedCacheWithLRUNode tail;
    Integer size;

    DLLForRankBasedWithLRU() {
        head = new RankBasedCacheWithLRUNode(0, 0, 0);
        tail = new RankBasedCacheWithLRUNode(0, 0, 0);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    void addFirst(RankBasedCacheWithLRUNode node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
        size++;
    }

    void remove(RankBasedCacheWithLRUNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size--;
    }

    RankBasedCacheWithLRUNode removeLast() {
        if (size == 0) {
            return null;
        }
        RankBasedCacheWithLRUNode last = tail.prev;
        remove(last);
        return last;
    }
}

public class RankBasedCacheWithLRU {
    private Integer capacity;
    private Map<Integer, RankBasedCacheWithLRUNode> cache;
    private TreeMap<Integer, DLLForRankBasedWithLRU> rankMap;

    RankBasedCacheWithLRU(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.rankMap = new TreeMap<>();
    }

    private void update(RankBasedCacheWithLRUNode node) {
        DLLForRankBasedWithLRU currList = rankMap.get(node.getRank());
        currList.remove(node);
        currList.addFirst(node);
    }

    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        RankBasedCacheWithLRUNode node = cache.get(key);
        update(node);
        return node.getValue();
    }

    public void put(int key, int value) {
        if (capacity == 0) {
            return;
        }
        if (cache.containsKey(key)) {
            RankBasedCacheWithLRUNode node = cache.get(key);
            node.setValue(value);
            update(node);
            return;
        }
        if (cache.size() == capacity) {
            int lowestRank = rankMap.firstKey();
            DLLForRankBasedWithLRU minRankList = rankMap.get(lowestRank);
            RankBasedCacheWithLRUNode removed = minRankList.removeLast();
            cache.remove(removed.getKey());
        }
        int rank = new Random().nextInt(1, 10);
        RankBasedCacheWithLRUNode newNode = new RankBasedCacheWithLRUNode(key, value, rank);
        cache.put(key, newNode);
        rankMap.computeIfAbsent(rank, k -> new DLLForRankBasedWithLRU()).addFirst(newNode);
    }
}
