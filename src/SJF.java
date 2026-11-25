import java.util.*;

public class SJF implements IScheduler{

    private List<Process> snapshot(List<Process> list){
        List<Process> copy = new ArrayList<>();
        for (Process p : list) copy.add(p.copy());
        return copy;
    }

    @Override
    public List<SimulationResult> simulate(List<Process> processes, SimulationConfig config){

        List<SimulationResult> result = new ArrayList<>();
        List<Process> ready = new ArrayList<>();

        int time = 0;
        int maxArrivalTime = 0;
        int cs = 0;
        Process current = null;

        for (Process process : processes) {
            if (process.getRemainingTime() > 0)
                maxArrivalTime = Math.max(maxArrivalTime, process.getArrivalTime());
        }

        while (time <= maxArrivalTime || !ready.isEmpty() || current != null) {

            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).getArrivalTime() == time && processes.get(i).getRemainingTime() > 0)
                    ready.add(processes.get(i));
            }

            if (cs > 0 && current == null && ready.isEmpty())
                cs = 0;

            Process shortest = null;
            for (Process process : ready){
                if (shortest == null
                        || process.getRemainingTime() < shortest.getRemainingTime()
                        || (process.getRemainingTime() == shortest.getRemainingTime()
                            && process.getArrivalTime() < shortest.getArrivalTime()))
                    shortest = process;
            }

            if (current != null && shortest != null && shortest.getRemainingTime() < current.getRemainingTime()){
                ready.add(current);
                current = null;
                cs = config.getCS();
            }

            if (cs > 0){
                result.add(new SimulationResult(time, "Context Switch", snapshot(ready), new ArrayList<>()));
                cs--;
                time++;
                continue;
            }

            if (current == null){
                if (shortest == null){
                    result.add(new SimulationResult(time, "", snapshot(ready), new ArrayList<>()));
                    time++;
                    continue;
                }
                current = shortest;
                ready.remove(shortest);
            }

            result.add(new SimulationResult(time, current.getName(), snapshot(ready), new ArrayList<>()));
            current.markStart(time);
            current.setRemainingTime(current.getRemainingTime() - 1);

            if (current.getRemainingTime() == 0){
                current.setFinishTime(time + 1);
                current.setTurnAroundTime(current.getFinishTime() - current.getArrivalTime());
                current.setWaitingTime(current.getTurnAroundTime() - current.getBurstTime());
                current = null;
                cs = config.getCS();
            }

            time++;
        }

        result.add(new SimulationResult(time, "", snapshot(ready), new ArrayList<>()));
        return result;
    }
}
