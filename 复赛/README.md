## 4.6

复赛在初赛的基本上，先让预置车辆跑完，然后再按初赛的策略，每个单位时间发20辆，成绩为19615。每个单位时间发30辆，成绩为14619。每个单位时间发33辆，成绩为13692。34辆成绩13411。35辆车死锁。然后之前版本的问题是把预置车辆没有去除，这样看似发了34辆，但实际上有些预置车辆是不作数的，实际发的车要少，34辆死锁。33辆成绩11865。换成同样速度同样时间优先车辆先发结果死锁。33辆同样速度然后优先车辆先发，再比较时间，成绩11335。34辆11148。35辆死锁。34辆，后5%40辆死锁。34辆，后5%38辆，结果成绩11203比之前更长。优先优先车辆，再速度、时间，34、33、30辆死锁。 

优先优先车辆，再速度、时间。遇到优先车跑完，隔50个单位时间，再发非优先车，发30辆，成绩8460。隔30个单位时间再发非优先，成绩8420。发33辆，死锁。发31辆死锁。调整间隔也没用。

## 4.7

训练地图预置车辆是每5个时间，第一个时间发80辆。所以我在剩余4个时间每个时间段发4辆车，总成绩7996。发10辆死锁。发8辆死锁。发6、5辆死锁。还是发4辆，但是由于每五个时间有一个时间不发车，应该是下列公式

```java
if(count / 5 < maxStartTime)    //预置车辆发的时候也跟着发车
{
    while(m.containsKey(count / 4)){
        count++;
    }

    startTime = Math.max(Integer.valueOf(carArr.get(i)[4]), count / 4);
}
else {      //预置车辆发完后
    startTime = maxStartTime + (count -  5 * maxStartTime)/ 30;
}
```

成绩7949。



## 4.9

用弗洛伊德求出路口之间距离除以限速的矩阵。然后在速度相同的情况下，路径短的优先发车。可以发31辆了，成绩7861。发32辆死锁。预置车辆阶段发6辆死锁。发5辆，7838。