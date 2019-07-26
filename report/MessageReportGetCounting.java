/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.sprayandwait.CountingTaxiProblem;

/**
 * Report untuk melihat hasil perhitungan/estimasi setiap node per interval waktu
 */
public class MessageReportGetCounting extends Report implements UpdateListener {

     public static final String INTERVAL_COUNT = "Interval";
    public static final int DEFAULT_INTERVAL1_COUNT = 1800;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Integer>> counttaxiproblem = new HashMap<DTNHost, ArrayList<Integer>>();

    public MessageReportGetCounting() {

        super();
        Settings settings = getSettings();
        if (settings.contains(INTERVAL_COUNT)) {
            interval = settings.getInt(INTERVAL_COUNT);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_INTERVAL1_COUNT;
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
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }

            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof CountingTaxiProblem)) {
                continue;
            }

            CountingTaxiProblem n = (CountingTaxiProblem) de;
            ArrayList<Integer> countList = new ArrayList<Integer>();
            int temp = n.getCountTotalEstimationOfTheNode();

            if (counttaxiproblem.containsKey(h)) {
                countList = counttaxiproblem.get(h);
                countList.add(temp);
                counttaxiproblem.put(h, countList);
            } else {
                counttaxiproblem.put(h, countList);
            }
        }

    }

    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Integer>> entry : counttaxiproblem.entrySet()) {
            String printHost = " " + entry.getKey() + "\t";
            for (Integer countList : entry.getValue()) {
                printHost = printHost + "\t" + countList;
            }
            write(printHost);
        }
        super.done();
    }
}
