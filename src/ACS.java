import java.util.*;

public class ACS implements IScheduler{

    private final Queue<Process> tier1;
    private final Queue<Process> tier2;


    public ACS(){
        tier1 = new ArrayDeque<>();
        tier2 = new ArrayDeque<>();
    }

    @Override 
    public List<SimulationResult> simulate(List<Process> processes, SimulationConfig config){

        List<SimulationResult> result = new ArrayList<>();

        int time = 0;
        int maxArrivalTime = 0;
        int q = config.getQ();
        boolean tier2IsRunning = false;
        Process pro = null;
        int cs = 0;

        for (Process process : processes) {
            maxArrivalTime = Math.max(maxArrivalTime, process.getArrivalTime());
        }

        while (time <= maxArrivalTime || !tier1.isEmpty() || !tier2.isEmpty()) {

            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).getArrivalTime() == time)
                    tier1.add(processes.get(i));
            }

            if (cs != 0){
                cs--;
                time++;
                continue;
            }

            if (tier1.isEmpty() || tier2IsRunning){
                if (!tier2.isEmpty()){
                    tier2IsRunning = true;

                    Process p = tier2.peek();

                    if (pro != null){
                        p = pro;
                    }
                    else{
                        for (Process process : tier2){
                            if (process.getPriority() > p.getPriority())
                                p = process;
                        }

                        pro = p;
                        tier2.remove(p);
                    }

                    p.setRemainingTime(p.getRemainingTime() - 1);

                    if (p.getRemainingTime() == 0){
                        tier2IsRunning = false;
                        cs = config.getCS();
                    }

                    for (Process process : tier2){
                        process.setPriority(process.getPriority() + 1);
                    }
                }

                time++;
                continue;
            }

            Process p = tier1.peek();

            p.setRemainingTime(p.getRemainingTime() - 1);
            q--;

            if (p.getRemainingTime() == 0){
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

        return result;
    }
}