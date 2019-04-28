package com.huawei;
import java.util.LinkedHashSet;
import java.util.Set;

public class DeadInfo {
    Set<Integer> deadLock=new LinkedHashSet<Integer>();
    Set<Car> deadCar=new LinkedHashSet<Car>();

    public DeadInfo(Set<Integer> deadLock, Set<Car> deadCar) {
        this.deadLock = deadLock;
        this.deadCar = deadCar;
    }
}
