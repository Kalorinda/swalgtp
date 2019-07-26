/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

/**
 *
 * @author Kalorinda
 */
import java.util.*;
import core.DTNHost;
import core.Settings;
import core.UpdateListener;

public class BufferOccupancyPerTimeTugas extends Report implements UpdateListener {

    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 300;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Double>> bufferCounts = new HashMap<DTNHost, ArrayList<Double>>();

    public BufferOccupancyPerTimeTugas() {
        super();

        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }

    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private void printLine(List<DTNHost> hosts) {

        for (DTNHost h : hosts) {
            ArrayList<Double> bufferList = new ArrayList<Double>();
            double temp = h.getBufferOccupancy();
            temp = (temp <= 100.0) ? (temp) : (100.0);
            if (bufferCounts.containsKey(h)) {
                bufferList = bufferCounts.get(h);
                bufferList.add(temp);
                bufferCounts.put(h, bufferList);
            } else {
                bufferCounts.put(h, bufferList);
            }
        }
    }

    @Override
    public void done() {

        for (Map.Entry<DTNHost, ArrayList<Double>> entry : bufferCounts.entrySet()) {
//            DTNHost key = entry.getKey();
//            ArrayList<Double> value = entry.getValue();

            String printHost = "Node " + entry.getKey().getAddress() + "\t";
            for (Double bufferList : entry.getValue()) {
                printHost = printHost + "\t" + bufferList;

            }
            write(printHost);
        }
        super.done(); //To change body of generated methods, choose Tools | Templates.
    }

}
