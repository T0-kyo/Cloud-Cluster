public class Process{
    
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int basePriority;
    private int priority;
    private int startTime;       // first time on CPU
    private int responseTime;
    private int remainingTime;
    private int finishTime;
    private int turnAroundTime;
    private int waitingTime; // total
    private int tier2WaitingTime; // tier2

    // For ACS
    public Process(String name, int arrivalTime, int burstTime, int priority){
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.basePriority = priority;
        this.priority = priority;
        this.startTime = -1;
        this.responseTime = 0;
        this.remainingTime = burstTime;
        this.finishTime = 0;
        this.turnAroundTime = 0;
        this.waitingTime = 0;
        this.tier2WaitingTime = 0;
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
    public int getBasePriority(){
        return basePriority;
    }
    public int getPriority() {
        return priority;
    }
    public int getStartTime() {
        return startTime;
    }
    public int getResponseTime() {
        return responseTime;
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
    public int getTier2WaitingTime() {
        return tier2WaitingTime;
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
    public void setTier2WaitingTime(int tier2WaitingTime) {
        this.tier2WaitingTime = tier2WaitingTime;
    }

    public void markStart(int time){          // call every time the process gets the CPU
        if (startTime == -1){
            startTime = time;
            responseTime = time - arrivalTime;
        }
    }

    public Process copy(){
        Process c = new Process(name, arrivalTime, burstTime, basePriority);
        c.remainingTime = remainingTime;
        c.priority = priority;
        c.tier2WaitingTime = tier2WaitingTime;
        return c;
    }

    @Override
    public String toString(){
        return name + " (remaining: " + remainingTime + ", Priority: " + priority + ")";
    }
}