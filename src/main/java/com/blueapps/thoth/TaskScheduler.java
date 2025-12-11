package com.blueapps.thoth;

public class TaskScheduler implements RunnableListener{

    private Thread worker = new Thread();

    private Runnable currentTask = null;
    private Runnable nextTask = null;

    public void addTask(Runnable task){
        if(currentTask == null){
            currentTask = task;
            worker = new Thread(currentTask);
            worker.start();
        } else {
            nextTask = task;
        }
    }

    @Override
    public void onFinish() {
        currentTask = null;
        if (nextTask != null){
            currentTask = nextTask;
            nextTask = null;
            worker = new Thread(currentTask);
            worker.start();
        }
    }
}
