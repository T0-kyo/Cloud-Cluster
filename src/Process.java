public class Process{
    
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private int remainingTime;
    private int priority;
    private int finishTime;
    private int turnAroundTime;
    private int waitingTime;

    // For ACS
    public Process(String name, int arrivalTime, int burstTime, int priority){
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.finishTime = 0;
        this.turnAroundTime = 0;
        this.waitingTime = 0;
    }

    // For SJF
    public Process(String name, int arrivalTime, int burstTime){
        this(name, arrivalTime, burstTime, 0);
    }

    public String getName() {
        return name;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public int getBurstTime() {
        return burstTime;
    }
    public int getRemainingTime() {
        return remainingTime;
    }
    public int getPriority() {
        return priority;
    }
    public int getFinishTime() {
        return finishTime;
    }
    public int getTurnAroundTime() {
        return turnAroundTime;
    }
    public int getWaitingTime() {
        return waitingTime;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }
    public void setFinishTime(int finishTime){
        this.finishTime = finishTime;
    }
    public void setTurnAroundTime(int turnAroundTime){
        this.turnAroundTime = turnAroundTime;
    }
    public void setWaitingTime(int waitingTime){
        this.waitingTime = waitingTime;
    }
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}