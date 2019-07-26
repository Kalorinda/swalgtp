/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.sprayandwait;


/**
 *
 * @author Kalorinda
 * 
 * Interface untuk mengambil hasil perhitungan/estimasi jumlah node pada kelas taxi problem
 * dan diakan dipanggil dikelas report AverageConvergenTimeReport dan MessageReportGetCounting
 */
public interface CountingTaxiProblem {
    public int getCountTotalEstimationOfTheNode();
}
