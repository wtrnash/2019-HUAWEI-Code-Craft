package com.huawei;
import java.util.Arrays;

public class Edge {
    int target;
    double[] disArr;
    public Edge(int target, double dis) {
        this.target = target;
        //this.dis = dis;
        disArr=new double[9000];
        Arrays.fill(disArr,dis);
    }
}
