import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

class CircularQueue {
    private final ArrayList<String> queue;
    private final int front;

    public CircularQueue(String[] items) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("배열을 입력해 주세요.");
        }
        this.queue = new ArrayList<>();
        queue.addAll(Arrays.asList(items));
        this.front = 0;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void enqueue(String item) {
        queue.add(item);
    }

    public String dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("큐가 비었습니다. 제거할 항목이 없습니다.");
        }
        return queue.remove(front);
    }

    public String peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("큐가 비었습니다.");
        }
        return queue.get(front);
    }

    public void display() {
        if (isEmpty()) {
            System.out.println("큐가 비었습니다.");
            return;
        }
        System.out.println(queue);
    }

    public String rotateQueue() {
        String item = dequeue();
        enqueue(item);
        return item;
    }

    public static void main(String[] args) {
        String[] geminiModels = {
                "gemini-2.0-flash-001",
                "gemini-2.0-pro-exp-02-05",
                "gemini-2.0-flash-exp",
                "gemini-1.5-pro",
                "gemini-exp-1206",
                "gemini-1.5-flash",
                "gemini-1.5-flash-8b"
        };

        CircularQueue queue = new CircularQueue(geminiModels);
        queue.display();

        queue.rotateQueue();
        queue.display();

        queue.enqueue("new-model");
        queue.display();
    }
}
