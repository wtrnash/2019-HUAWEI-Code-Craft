package com.huawei;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader carBf=new BufferedReader(new FileReader(args[0]));
        BufferedReader crossBf=new BufferedReader(new FileReader(args[2]));
        BufferedReader roadBf=new BufferedReader(new FileReader(args[1]));
        BufferedReader presetAnswerBf=new BufferedReader(new FileReader(args[3]));
        List<String[]> crossArr=new ArrayList<String[]>();
        List<String[]> roadArr=new ArrayList<String[]>();
        List<String[]> carArr=new ArrayList<String[]>();
        List<String[]> presetAnswerArr=new ArrayList<String[]>();
        String str;
        while((str=crossBf.readLine())!=null){
            crossArr.add(getStrArr(str));
        }
        while((str=roadBf.readLine())!=null){
            roadArr.add(getStrArr(str));
        }
        while((str=carBf.readLine())!=null){
            carArr.add(getStrArr(str));
        }
        while((str=presetAnswerBf.readLine())!=null){
            presetAnswerArr.add(getStrArr(str));
        }

        //路口ID对应二维数组下标
        HashMap<Integer, Integer> crossIdToIndex = new HashMap<>();
        for(int i = 1; i < crossArr.size(); i++){
            crossIdToIndex.put(Integer.valueOf(crossArr.get(i)[0]), i - 1);
        }

        //二维数组存各个路口之间的纯距离除以路径最高限速
        double [][] map = new double[crossArr.size() - 1][crossArr.size() - 1];

        //初始化
        for(int i = 0; i < crossArr.size() - 1; i++){
            for(int j = 0; j < crossArr.size() - 1; j++){
                if(i == j){
                    map[i][j] = 0;
                }
                else{
                    map[i][j] = Integer.MAX_VALUE;
                }
            }
        }
        //将直接连通道路的纯距离除以限速放入二维数组
        for(int i = 1; i < roadArr.size(); i++)
        {
            int length = Integer.valueOf(roadArr.get(i)[1]);
            int speed = Integer.valueOf(roadArr.get(i)[2]);
            int from = Integer.valueOf(roadArr.get(i)[4]);
            int to = Integer.valueOf(roadArr.get(i)[5]);
            int isDuplex = Integer.valueOf(roadArr.get(i)[6]);

            int fromIndex = crossIdToIndex.get(from);
            int toIndex =  crossIdToIndex.get(to);
            map[fromIndex][toIndex] = (double)length / speed;
            if(isDuplex == 1){
                map[toIndex][fromIndex] = (double)length / speed;
            }
        }
        //弗洛伊德算法
        for(int k = 0; k < map.length; k++){
            for(int i = 0; i< map.length; i++){
                for(int j = 0; j < map.length; j++){
                    double temp = (map[i][k] == Integer.MAX_VALUE || map[k][j] == Integer.MAX_VALUE ) ?  Integer.MAX_VALUE  : (map[i][k] + map[k][j]);
                    if (map[i][j] > temp) {
                        map[i][j] = temp;
                    }
                }
            }
        }

        presetAnswerArr = presetAnswerArr.subList(1, presetAnswerArr.size());
        HashMap<Integer, Integer> m = new HashMap<>();
        for(int i = 0; i < presetAnswerArr.size(); i++){
            int key = Integer.valueOf(presetAnswerArr.get(i)[1]);
            if(m.containsKey(key)){
                m.put(key, m.get(key) + 1);
            }
            else{
                m.put(key, 1);
            }
        }

        for (int key : m.keySet()) {
            int value = m.get(key);
            System.out.println(key + ":" + value);
        }

        int maxStartTime=0;
        for(int i=0;i<presetAnswerArr.size();i++){
            maxStartTime=Math.max(Integer.valueOf(presetAnswerArr.get(i)[1]),maxStartTime);
        }

        carArr=carArr.subList(1,carArr.size());
        //移除预置车辆
        for(int i = carArr.size() - 1; i >= 0; i--){
            String[] item = carArr.get(i);
            int preset = Integer.valueOf(item[6]);
            if(preset == 1){
                carArr.remove(item);
            }
        }

        carArr.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int from1 = Integer.valueOf(o1[1]), from2=Integer.valueOf(o2[1]);
                int to1 = Integer.valueOf(o1[2]), to2=Integer.valueOf(o2[2]);
                int priority1 = Integer.valueOf(o1[5]), priority2=Integer.valueOf(o2[5]);
                int time1=Integer.valueOf(o1[4]),time2=Integer.valueOf(o2[4]);
                int speed1=Integer.valueOf(o1[3]),speed2=Integer.valueOf(o2[3]);

                int fromIndex1 = crossIdToIndex.get(from1);
                int toIndex1 = crossIdToIndex.get(to1);
                int fromIndex2 = crossIdToIndex.get(from2);
                int toIndex2 = crossIdToIndex.get(to2);
                double distance1 = map[fromIndex1][toIndex1];
                double distance2 = map[fromIndex2][toIndex2];

                if(priority1 == priority2){
                    if(speed1==speed2){
                        if(distance1 == distance2){
                            if(time1==time2){
                                return 0;
                            }
                            return time1>time2?1:-1;
                        }
                        return distance1>distance2?1:-1;    //让距离短的先跑

                    }
                    return speed1>speed2?-1:1;
                }
                return priority1>priority2?-1:1;    //优先车辆先走
            }
        });
        Map<String,Integer> countMap=new HashMap<String,Integer>();
        for(int i=0;i<carArr.size();i++){
            while(true){
                String planTime=carArr.get(i)[4];
                String startId=carArr.get(i)[1];
                String key=planTime+":"+startId;
                if(!countMap.containsKey(key)){
                    countMap.put(key,1);
                    break;
                }else{
                    if(countMap.get(key)>=3){
                        int res=Integer.valueOf(planTime)+1;
                        carArr.get(i)[4]=String.valueOf(res);
                    }else{
                        countMap.put(key,countMap.get(key)+1);
                        break;
                    }
                }
            }
        }


        carArr.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int from1 = Integer.valueOf(o1[1]), from2=Integer.valueOf(o2[1]);
                int to1 = Integer.valueOf(o1[2]), to2=Integer.valueOf(o2[2]);
                int priority1 = Integer.valueOf(o1[5]), priority2=Integer.valueOf(o2[5]);
                int time1=Integer.valueOf(o1[4]),time2=Integer.valueOf(o2[4]);
                int speed1=Integer.valueOf(o1[3]),speed2=Integer.valueOf(o2[3]);

                int fromIndex1 = crossIdToIndex.get(from1);
                int toIndex1 = crossIdToIndex.get(to1);
                int fromIndex2 = crossIdToIndex.get(from2);
                int toIndex2 = crossIdToIndex.get(to2);
                double distance1 = map[fromIndex1][toIndex1];
                double distance2 = map[fromIndex2][toIndex2];

                if(priority1 == priority2){
                    if(speed1==speed2){
                        if(distance1 == distance2){
                            if(time1==time2){
                                return 0;
                            }
                            return time1>time2?1:-1;
                        }
                        return distance1>distance2?1:-1;    //让距离短的先跑

                    }
                    return speed1>speed2?-1:1;
                }
                return priority1>priority2?-1:1;    //优先车辆先走
            }
        });
        Graph graph=new Graph();
        graph.generateMap(copyArr(roadArr),copyArr(crossArr));
        int count=0;
        int startTime;
        int priority;
        int priorityNumber = 0;
        for(int i = 0; i < carArr.size(); i++)
        {
            priority = Integer.valueOf(carArr.get(i)[5]);
            if(priority == 0){
                priorityNumber = i;
                break;
            }
        }

        int commonStartTime = 0; //普通车开始的发车时间
        int presetCount = 20;
        for(int i=0;i<carArr.size();i++){
            if(i < priorityNumber)
            {
                count++;
                if(count / presetCount < maxStartTime)    //预置车辆发的时候也跟着发车
                {
                    int preTime = Integer.valueOf(carArr.get(i)[4]);

                    while(m.containsKey(preTime) && m.get(preTime) >= presetCount)
                    {
                        preTime += 1;
                    }
                    if(m.containsKey(preTime))
                        m.put(preTime, m.get(preTime) + 1);
                    else
                        m.put(preTime, 1);


                    startTime = Math.max(preTime, count / presetCount);
                }
                else {      //预置车辆发完后
                    startTime = maxStartTime + (count -  presetCount * maxStartTime)/ Math.round(28 + ((float) i / priorityNumber) * 10);
                }

            }
            else if(i == priorityNumber)
            {
                count += 900;         //优先和非优先之间隔时段发车

                count++;
                if(count / presetCount < maxStartTime)    //预置车辆发的时候也跟着发车
                {
                    int preTime = Integer.valueOf(carArr.get(i)[4]);

                    while(m.containsKey(preTime) && m.get(preTime) >= presetCount)
                    {
                        preTime += 1;
                    }
                    if(m.containsKey(preTime))
                        m.put(preTime, m.get(preTime) + 1);
                    else
                        m.put(preTime, 1);
                    startTime = Math.max(preTime, count / presetCount);
                }
                else {      //预置车辆发完后
                    startTime = maxStartTime + (count -  presetCount * maxStartTime)/ 35;
                }

                commonStartTime = startTime + 100;
                count = 0;
            }
            else
            {
                count++;
                startTime = commonStartTime + count / Math.round(31 + ((float) i / carArr.size()) * 10);   //普通车
            }

            carArr.get(i)[4]=String.valueOf(startTime);
        }
        List<List<Integer>> res=graph.startSearch(carArr);
        outPut(args[4],res);
    }
    public static void outPut(String src,List<List<Integer>> res) throws IOException {
        BufferedWriter bw=new BufferedWriter(new FileWriter(src));
        for(int j=0;j<res.size();j++){
            String tempStr="(";
            for(int i:res.get(j)){
                tempStr+=i;
                tempStr+=',';
            }
            tempStr=tempStr.substring(0,tempStr.length()-1);
            tempStr+=')';
            bw.write(tempStr);
            if(j<res.size()-1){
                bw.newLine();
            }
            bw.flush();
        }
    }
    public static String[] getStrArr(String ques){
        String temp=ques;
        temp=temp.substring(1,temp.length()-1).replace(" ","");
        String[] tempArr=temp.split(",");
        return tempArr;
    }
    public static List<String[]> copyArr(List<String[]> ques){
        List<String[]> res=new ArrayList<String[]>();
        for(int i=0;i<ques.size();i++){
            String[] tempCopy=ques.get(i);
            String[] targetCopy=new String[tempCopy.length];
            for(int j=0;j<tempCopy.length;j++){
                targetCopy[j]=tempCopy[j];
            }
            res.add(targetCopy);
        }
        //res.addAll(ques);
        return res;
    }
}
