/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import java.util.*;
import core.DTNHost;
import core.Settings;
import core.UpdateListener;

/**
 *
 * @author Jarkom
 * 
 * Report untuk melihat node interface yang hidup atau mati (true/false)
 */
public class IsActiveInterface extends Report implements UpdateListener {

    public static final String COUNTING_REPORT_INTERVAL = "Interval";
    
    public static final int DEFAULT_COUNTING_REPORT_INTERVAL = 1800;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Boolean>> counting = new HashMap<DTNHost, ArrayList<Boolean>>();

    public IsActiveInterface() {
        super();

        Settings settings = getSettings();
        if (settings.contains(COUNTING_REPORT_INTERVAL)) {
            interval = settings.getInt(COUNTING_REPORT_INTERVAL);
        } else {
            interval = -1;
        }

        if (interval < 0) {
            interval = DEFAULT_COUNTING_REPORT_INTERVAL;
        }
    }

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
            ArrayList<Boolean> countingList = new ArrayList<Boolean>();
            boolean a = h.isRadioActive();
            if (counting.containsKey(h)) {

                countingList = counting.get(h);
                countingList.add(a);
                counting.put(h, countingList);
            } else {
                counting.put(h, countingList);
            }
        }

    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Boolean>> entry : counting.entrySet()) {

            String printHost = "" + entry.getKey().getAddress() + "\t";
            for (Boolean countList : entry.getValue()) {
                printHost = printHost + "\t" + countList;
            }
            write(printHost);
        }
        super.done();
    }

}
