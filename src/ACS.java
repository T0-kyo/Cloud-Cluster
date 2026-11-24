//TODO 1) Handling priority
//TODO 2) Construct the result
//TODO 3) Comment the code
//TODO 4) Tier 2 wait calculation (add in process)
import java.util.*;

public class ACS implements IScheduler{

    private final Queue<Process> tier1;
    private final Queue<Process> tier2;

    public ACS(){
        tier1 = new ArrayDeque<>();
        tier2 = new ArrayDeque<>();
    }

    private List<Process> snapshot(Queue<Process> queue){
        List<Process> copy = new ArrayList<>();
        for (Process p : queue) copy.add(p.copy());
        return copy;
    }

    @Override 
    public List<SimulationResult> simulate(List<Process> processes, SimulationConfig config){

        List<SimulationResult> result = new ArrayList<>();

        int time = 0; // to walk step by step 
        int maxArrivalTime = 0; // to handle when process still coming
        int q = config.getQ(); // to handle the process in RR (tier 1)
        boolean tier2IsRunning = false; // to work on a process in tier 2 when tier 1 is empty
        Process pro = null; // to get the highest priority process for tier 2
        int cs = 0; // to simulate the context switch time

        for (Process process : processes) {
            maxArrivalTime = Math.max(maxArrivalTime, process.getArrivalTime());
        }

        // simulating loop
        while (time <= maxArrivalTime || !tier1.isEmpty() || !tier2.isEmpty()) {

            // look for an arrived process
            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).getArrivalTime() == time)
                    tier1.add(processes.get(i));
            }

            // simulate context switch time
            if (cs > 0){
                result.add(new SimulationResult(time, "Context Switch", snapshot(tier1), snapshot(tier2)));
                for (Process process : tier2)                      // CS time counts as waiting
                    process.setTier2WaitingTime(process.getTier2WaitingTime() + 1);
                cs--;
                time++;
                continue;
            }

            // to handle if there's no processes coming and tier 1 is empty and tier 2 has processes
            if (tier1.isEmpty() || tier2IsRunning){
                if (!tier2.isEmpty()){ // work on tier 2
                    tier2IsRunning = true;

                    Process p;

                    // get the process with highest priority and update priority of processes
                    if (pro != null){
                        p = pro;
                    }
                    else{
                        p = tier2.peek();   
                        for (Process process : tier2){             // aging from the BASE priority
                            int aged = Math.max(1, process.getBasePriority() - process.getTier2WaitingTime() / 4);
                            process.setPriority(aged);
                            if (process.getPriority() < p.getPriority())   // strict < keeps FIFO on ties
                                p = process;
                        }
                        pro = p;
                    }
                    
                    result.add(new SimulationResult(time, p.getName(), snapshot(tier1), snapshot(tier2)));
                    p.markStart(time);
                    p.setRemainingTime(p.getRemainingTime() - 1);
                    
                    if (p.getRemainingTime() == 0){
                        p.setFinishTime(time + 1);
                        p.setTurnAroundTime(p.getFinishTime() - p.getArrivalTime());
                        p.setWaitingTime(p.getTurnAroundTime() - p.getBurstTime());
                        tier2.remove(p);
                        tier2IsRunning = false;
                        pro = null;
                        cs = config.getCS();
                    }

                    for (Process process : tier2){                 // everyone except the running one waits
                        if (process != p)
                            process.setTier2WaitingTime(process.getTier2WaitingTime() + 1);
                    }
                }
                else {
                    result.add(new SimulationResult(time, "", snapshot(tier1), snapshot(tier2)));
                }

                time++;
                continue;
            }

            Process p = tier1.peek();
            result.add(new SimulationResult(time, p.getName(), snapshot(tier1), snapshot(tier2)));
            p.markStart(time);

            for (Process process : tier2)
                process.setTier2WaitingTime(process.getTier2WaitingTime() + 1);

            p.setRemainingTime(p.getRemainingTime() - 1);
            q--;

            if (p.getRemainingTime() == 0){
                p.setFinishTime(time + 1);
                p.setTurnAroundTime(p.getFinishTime() - p.getArrivalTime());
                p.setWaitingTime(p.getTurnAroundTime() - p.getBurstTime());
                q = config.getQ();
                tier1.remove();
                cs = config.getCS();
            }

            else if (q == 0){
                q = config.getQ();
                tier2.add(tier1.remove());
                cs = config.getCS();
            }

            time++;
        }
        
        result.add(new SimulationResult(time, "", snapshot(tier1), snapshot(tier2)));
        return result;
    }
}