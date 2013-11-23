public class SimpleReadWriteLock2 {
    private int readers;
    private boolean writer;
    private MyLock readLock;
    private MyLock writeLock;

    public SimpleReadWriteLock2() {
        writer = false;
        readers = 0;
        readLock = new MyReadLock();
        writeLock = new MyWriteLock();
    }

    public MyLock readLock() {
        return readLock;
    }

    public MyLock writeLock() {
        return writeLock;
    }

    protected interface MyLock {
        void lock() throws InterruptedException;
        void unlock();
        SimpleReadWriteLock2 that = SimpleReadWriteLock2.this;
    }

    protected class MyReadLock implements MyLock{

        @Override
        public void lock() throws InterruptedException {
            synchronized (that) {
                while (writer) {
                    that.wait();
                }
                readers++;
            }
        }

        @Override
        public void unlock() {
            synchronized (that) {
                readers--;
                if (readers == 0) {
                    that.notifyAll();
                }
            }
        }
    }

    protected class MyWriteLock implements MyLock {

        @Override
        public void lock() throws InterruptedException {
            synchronized (that) {
                while (readers > 0 || writer) {
                    that.wait();
                }
                writer = true;
            }
        }

        @Override
        public void unlock() {
            synchronized (that) {
                writer = false;
                that.notifyAll();
            }
        }
    }
}
