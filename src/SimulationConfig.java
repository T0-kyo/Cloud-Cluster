public class SimulationConfig{
    
    private final int _cs;
    private final int _q;

    public SimulationConfig(int cs, int q){
        _cs = cs;
        _q = q;
    }

    public int getCS(){
        return _cs;
    }

    public int getQ(){
        return _q;
    }
}