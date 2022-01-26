package server;

public class ThreadPool {
    private final BlockingQueue<Runnable> queue;
    private final Worker[] workers;

    public ThreadPool(int nThreads, int limitNTasks) {
        queue = new BlockingQueue<>(limitNTasks);
        workers = new Worker[nThreads];
        for (int i = 0; i < workers.length; ++i) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public void execute(Runnable task) {
        queue.put(task);
    }

    public void shutdown() {
        for (Worker w : workers) {
            w.shutdownSignal = true;
            w.interrupt();
            w = null;
        }
    }

    private class Worker extends Thread {
        boolean shutdownSignal = false;

        @Override
        public void run() {
            Runnable task;
            while (!shutdownSignal) {
                try {
                    task = queue.get();
                } catch (InterruptedException e) {
                    break; // this thread is interrupted. Time to leave this loop
                }
                task.run();

            }
        }
    }
}
