import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private final DefaultTableModel inputModel =
            new DefaultTableModel(new String[]{"Name", "Arrival", "Burst", "Priority"}, 0);
    private final JTable inputTable = new JTable(inputModel);
    private final TableColumn priorityColumn;
    private boolean priorityVisible = true;

    private final JComboBox<String> algoBox = new JComboBox<>(new String[]{"SJF", "ACS"});
    private final JSpinner csSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner qSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSlider speedSlider = new JSlider(1, 10, 6);   
    private final JButton runButton = new JButton("Run");
    private final JButton resetButton = new JButton("Reset");
    private final JLabel statusLabel = new JLabel("Time: -");

    private final GanttPanel gantt = new GanttPanel();
    private final DefaultTableModel resultModel =
            new DefaultTableModel(new String[]{"Process", "Arrival", "Finish", "TAT", "WT", "RT"}, 0);
    private final JLabel averagesLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea(8, 40);

    private Timer timer;
    private List<Process> simProcesses = new ArrayList<>();
    private final List<String> logLines = new ArrayList<>();
    private final List<Integer> logTimes = new ArrayList<>();
    private final List<Process> shown = new ArrayList<>();   
    private int nextLog = 0;
    private int currentTime = 0;
    private int totalTime = 0;

    public MainFrame() {
        super("Cloud Cluster Scheduler Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        priorityColumn = inputTable.getColumnModel().getColumn(3);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        algoBox.addActionListener(e -> updateAlgoUI());
        speedSlider.addChangeListener(e -> {
            if (timer != null) timer.setDelay(delayMs());
        });
        loadTestCase(1);
        updateAlgoUI();

        setSize(950, 800);
        setLocationRelativeTo(null);
    }


    private JComponent buildTopPanel() {
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Scheduler:"));
        row1.add(algoBox);
        row1.add(new JLabel("Context switch (CS):"));
        row1.add(csSpinner);
        row1.add(new JLabel("Quantum (Q):"));
        row1.add(qSpinner);

        speedSlider.setPreferredSize(new Dimension(130, 24));
        speedSlider.setToolTipText("Simulation speed");
        runButton.addActionListener(e -> runSimulation());
        resetButton.addActionListener(e -> resetOutputs());

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Speed:"));
        row2.add(speedSlider);
        row2.add(runButton);
        row2.add(resetButton);
        row2.add(statusLabel);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(row1);
        p.add(row2);
        return p;
    }

    private JComponent buildCenter() {
        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Processes"));
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove selected");
        JButton t1 = new JButton("Test case 1");
        JButton t2 = new JButton("Test case 2");

        add.addActionListener(e ->
                inputModel.addRow(new Object[]{nextName(), 0, 1, 1}));
        remove.addActionListener(e -> {
            if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
            int row = inputTable.getSelectedRow();
            if (row >= 0) inputModel.removeRow(inputTable.convertRowIndexToModel(row));
        });
        t1.addActionListener(e -> loadTestCase(1));
        t2.addActionListener(e -> loadTestCase(2));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(t1);
        buttons.add(t2);
        inputPanel.add(buttons, BorderLayout.SOUTH);

        JPanel ganttWrap = new JPanel(new BorderLayout());
        ganttWrap.setBorder(BorderFactory.createTitledBorder("Gantt chart"));
        gantt.setPreferredSize(new Dimension(800, 105));
        ganttWrap.add(gantt, BorderLayout.CENTER);

        JTable resultTable = new JTable(resultModel);
        resultTable.setEnabled(false);
        JPanel resultPanel = new JPanel(new BorderLayout(4, 4));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultPanel.add(averagesLabel, BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Simulation log"));

        JSplitPane resultsAndLog = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPanel, logScroll);
        resultsAndLog.setResizeWeight(0.5);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.add(ganttWrap, BorderLayout.NORTH);
        bottom.add(resultsAndLog, BorderLayout.CENTER);

        JSplitPane all = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, bottom);
        all.setResizeWeight(0.3);
        return all;
    }


    private boolean isSjf() {
        return "SJF".equals(algoBox.getSelectedItem());
    }

    private void updateAlgoUI() {
        boolean sjf = isSjf();
        qSpinner.setEnabled(!sjf);
        if (sjf && priorityVisible) {
            inputTable.removeColumn(priorityColumn);
            priorityVisible = false;
        } else if (!sjf && !priorityVisible) {
            inputTable.addColumn(priorityColumn);
            priorityVisible = true;
        }
    }

    private void loadTestCase(int choice) {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
        inputModel.setRowCount(0);
        if (choice == 1) {
            inputModel.addRow(new Object[]{"P1", 0, 4, 2});
            inputModel.addRow(new Object[]{"P2", 1, 7, 3});
            inputModel.addRow(new Object[]{"P3", 3, 2, 1});
            csSpinner.setValue(0);
        } else {
            inputModel.addRow(new Object[]{"P1", 0, 5, 4});
            inputModel.addRow(new Object[]{"P2", 1, 6, 2});
            inputModel.addRow(new Object[]{"P3", 4, 4, 3});
            csSpinner.setValue(1);
        }
        qSpinner.setValue(3);
    }

    private String nextName() {
        int i = inputModel.getRowCount() + 1;
        while (nameExists("P" + i)) i++;
        return "P" + i;
    }

    private boolean nameExists(String name) {
        for (int r = 0; r < inputModel.getRowCount(); r++) {
            if (String.valueOf(inputModel.getValueAt(r, 0)).trim().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private ArrayList<Process> readProcesses(boolean sjf) {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();

        ArrayList<Process> list = new ArrayList<>();
        Map<String, Integer> seenNames = new HashMap<>();   

        for (int i = 0; i < inputModel.getRowCount(); i++) {
            String name = String.valueOf(inputModel.getValueAt(i, 0)).trim();

            if (name.isEmpty()) {
                selectRow(i);
                throw new IllegalArgumentException("Row " + (i + 1) + ": process name is required.");
            }
            Integer firstRow = seenNames.get(name.toLowerCase());
            if (firstRow != null) {
                selectRow(i);
                throw new IllegalArgumentException("Row " + (i + 1) + ": duplicate process name \""
                        + name + "\" (already used in row " + (firstRow + 1) + "). Names must be unique.");
            }
            seenNames.put(name.toLowerCase(), i);

            try {
                int arrival = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 1)).trim());
                int burst = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 2)).trim());
                if (arrival < 0 || burst <= 0) throw new NumberFormatException();

                if (sjf) {
                    list.add(new Process(name, arrival, burst));              
                } else {
                    int priority = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 3)).trim());
                    list.add(new Process(name, arrival, burst, priority));    
                }
            } catch (NumberFormatException ex) {
                selectRow(i);
                throw new IllegalArgumentException("Row " + (i + 1)
                        + ": arrival, burst and priority must be whole numbers (arrival >= 0, burst > 0).");
            }
        }
        if (list.isEmpty()) throw new IllegalArgumentException("Add at least one process.");
        return list;
    }

    private void selectRow(int row) {
        inputTable.setRowSelectionInterval(row, row);
        inputTable.scrollRectToVisible(inputTable.getCellRect(row, 0, true));
    }


    private int delayMs() {
        return 1100 - speedSlider.getValue() * 100;
    }

    private void runSimulation() {
        try {
            boolean sjf = isSjf();
            ArrayList<Process> processes = readProcesses(sjf);   

            int cs = (Integer) csSpinner.getValue();
            int q = sjf ? 0 : (Integer) qSpinner.getValue();      
            SimulationConfig config = new SimulationConfig(cs, q);

            IScheduler scheduler = sjf ? new SJF() : new ACS();
            List<SimulationResult> result = scheduler.simulate(processes, config);

            resetOutputs();

            for (SimulationResult r : result) {
                logTimes.add(r.getTime());
                if (sjf) {
                    String running = (r.getName() == null || r.getName().isEmpty()) ? "Idle" : r.getName();
                    logLines.add("Time " + r.getTime() + " | Running: " + running
                            + " | Ready: " + readyQueue(r.getTier1Processes()));
                } else {
                    logLines.add(r.toString());
                }
            }

            List<Slice> slices = buildSlices(result, processes);
            int total = 0;
            for (Process p : processes) total = Math.max(total, p.getFinishTime());
            for (Slice s : slices) total = Math.max(total, s.end);

            simProcesses = processes;
            totalTime = total;
            gantt.setData(slices, total);

            startAnimation();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Simulation failed: " + ex,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startAnimation() {
        currentTime = 0;
        nextLog = 0;
        runButton.setEnabled(false);

        advance();                                  
        if (currentTime >= totalTime) return;       

        timer = new Timer(delayMs(), e -> {
            currentTime++;
            advance();
        });
        timer.start();
    }

    private void advance() {
        
        while (nextLog < logLines.size() && logTimes.get(nextLog) <= currentTime) {
            logArea.append(logLines.get(nextLog++) + "\n");
        }
        logArea.setCaretPosition(logArea.getDocument().getLength());

        for (Process p : simProcesses) {
            if (p.getFinishTime() <= currentTime && !shown.contains(p)) addResultRow(p);
        }

        gantt.setCurrentTime(currentTime);
        statusLabel.setText("Time: " + Math.min(currentTime, totalTime) + " / " + totalTime);

        if (currentTime >= totalTime) finishAnimation();
    }

    private void finishAnimation() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        while (nextLog < logLines.size()) logArea.append(logLines.get(nextLog++) + "\n");
        for (Process p : simProcesses) {
            if (!shown.contains(p)) addResultRow(p);
        }

        double totalWT = 0, totalTAT = 0, totalRT = 0;
        for (Process p : simProcesses) {
            totalWT += p.getWaitingTime();
            totalTAT += p.getTurnAroundTime();
            totalRT += p.getResponseTime();
        }
        int n = simProcesses.size();
        if (n > 0) {
            averagesLabel.setText(String.format(
                    "  Avg TAT: %.2f    Avg WT: %.2f    Avg RT: %.2f",
                    totalTAT / n, totalWT / n, totalRT / n));
        }
        runButton.setEnabled(true);
    }

    private void addResultRow(Process p) {
        shown.add(p);
        resultModel.addRow(new Object[]{
                p.getName(), p.getArrivalTime(), p.getFinishTime(),
                p.getTurnAroundTime(), p.getWaitingTime(), p.getResponseTime()});
    }

    private void resetOutputs() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        logArea.setText("");
        resultModel.setRowCount(0);
        averagesLabel.setText(" ");
        statusLabel.setText("Time: -");
        gantt.clear();

        simProcesses = new ArrayList<>();
        logLines.clear();
        logTimes.clear();
        shown.clear();
        nextLog = 0;
        currentTime = 0;
        totalTime = 0;
        runButton.setEnabled(true);
    }

    private static String readyQueue(List<Process> list) {
        if (list == null || list.isEmpty()) return "[Empty]";
        List<String> items = new ArrayList<>();
        for (Process p : list) {
            items.add(p.getName() + " (remaining: " + p.getRemainingTime() + ")");
        }
        return items.toString();
    }


    private static class Slice {
        final String name;
        final int start, end;
        Slice(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

    private List<Slice> buildSlices(List<SimulationResult> results, List<Process> processes) {
        int maxFinish = 0;
        for (Process p : processes) maxFinish = Math.max(maxFinish, p.getFinishTime());

        List<Slice> out = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            SimulationResult r = results.get(i);
            String name = r.getName() == null ? "" : r.getName();
            int start = r.getTime();
            int end = (i + 1 < results.size()) ? results.get(i + 1).getTime() : maxFinish;
            if (end <= start) continue;

            Slice last = out.isEmpty() ? null : out.get(out.size() - 1);
            if (last != null && last.name.equals(name) && last.end == start) {
                out.set(out.size() - 1, new Slice(name, last.start, end));
            } else {
                out.add(new Slice(name, start, end));
            }
        }
        return out;
    }

    private static class GanttPanel extends JPanel {
        private static final Color[] PALETTE = {
                new Color(0x8ECAE6), new Color(0xFFB703), new Color(0xB5E48C),
                new Color(0xF4A6A6), new Color(0xCDB4DB), new Color(0xFFD6A5),
                new Color(0x9BF6FF), new Color(0xFDFFB6), new Color(0xBDB2FF),
                new Color(0xCAFFBF)
        };

        private List<Slice> slices = new ArrayList<>();
        private final Map<String, Color> colors = new HashMap<>();
        private int total = 0;
        private int currentTime = 0;

        GanttPanel() {
            setBackground(Color.WHITE);
        }

        void setData(List<Slice> slices, int total) {
            this.slices = slices;
            this.total = total;
            this.currentTime = 0;
            colors.clear();
            int next = 0;
            for (Slice s : slices) {
                if (!s.name.isEmpty() && !colors.containsKey(s.name)) {
                    colors.put(s.name, PALETTE[next++ % PALETTE.length]);
                }
            }
            repaint();
        }

        void setCurrentTime(int t) {
            this.currentTime = t;
            repaint();
        }

        void clear() {
            slices = new ArrayList<>();
            colors.clear();
            total = 0;
            currentTime = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (total <= 0) {
                g.setColor(Color.GRAY);
                g.drawString("Press Run to see the timeline.", 20, 40);
                return;
            }

            int left = 20, top = 24, barH = 40;
            double unit = (getWidth() - 2 * left) / (double) total;
            int trackEnd = left + (int) Math.round(total * unit);
            FontMetrics fm = g.getFontMetrics();

            g.setColor(new Color(0xF2F2F2));
            g.fillRect(left, top, trackEnd - left, barH);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(left, top, trackEnd - left, barH);

            for (Slice s : slices) {
                if (s.start >= currentTime) continue;            
                int visEnd = Math.min(s.end, currentTime);       
                int x = left + (int) Math.round(s.start * unit);
                int xEnd = left + (int) Math.round(visEnd * unit);
                int w = Math.max(1, xEnd - x);
                boolean idle = s.name.isEmpty();

                g.setColor(idle ? new Color(0xDDDDDD) : colors.get(s.name));
                g.fillRect(x, top, w, barH);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, top, w, barH);

                String label = idle ? "Idle" : s.name;
                if (fm.stringWidth(label) + 4 <= w) {
                    g.drawString(label, x + (w - fm.stringWidth(label)) / 2, top + barH / 2 + 5);
                }
                g.drawString(String.valueOf(s.start), x, top + barH + 16);
            }

            if (currentTime >= total) {
                g.setColor(Color.DARK_GRAY);
                g.drawString(String.valueOf(total), trackEnd, top + barH + 16);
            } else {
                int x = left + (int) Math.round(currentTime * unit);
                g.setColor(Color.RED);
                g.drawLine(x, top - 6, x, top + barH + 4);
                g.drawString("t=" + currentTime, x + 4, top - 9);
            }
        }
    }
}
