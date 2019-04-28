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
        int maxStartTime=0;
        for(int i=1;i<presetAnswerArr.size();i++){
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
                int priority1 = Integer.valueOf(o1[5]), priority2=Integer.valueOf(o2[5]);
                int time1=Integer.valueOf(o1[4]),time2=Integer.valueOf(o2[4]);
                int speed1=Integer.valueOf(o1[3]),speed2=Integer.valueOf(o2[3]);

                if(priority1 == priority2){
                    if(speed1==speed2){
                        if(time1==time2){
                            return 0;
                        }
                        return time1>time2?1:-1;

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
                int priority1 = Integer.valueOf(o1[5]), priority2=Integer.valueOf(o2[5]);
                int time1=Integer.valueOf(o1[4]),time2=Integer.valueOf(o2[4]);
                int speed1=Integer.valueOf(o1[3]),speed2=Integer.valueOf(o2[3]);

                if(priority1 == priority2){
                    if(speed1==speed2){
                        if(time1==time2){
                            return 0;
                        }
                        return time1>time2?1:-1;

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
        boolean isPriority = true;
        for(int i=0;i<carArr.size();i++){
            if(isPriority)
            {
                int priority = Integer.valueOf(carArr.get(i)[5]);
                if(priority == 0){
                    count += 900;    //优先和非优先之间隔时段发车
                    isPriority = false;
                }
                count++;
                startTime = maxStartTime + count / 30;
            }
            else
            {
                count++;
                startTime = maxStartTime + count / 30;
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
