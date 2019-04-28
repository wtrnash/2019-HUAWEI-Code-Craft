package com.huawei;
import java.util.*;

public class Graph {
    //存储路口id与坐标的映射 12->12:34
    static Map<Integer, coordinate> crossMapToZuo=new HashMap<Integer, coordinate>();
    //计算欧式距离使用 12:34->12
    static Map<String,Integer> zuoMapToCross=new HashMap<String,Integer>();

    static int Max=2000;
    //存储邻接表
    static List<List<Edge>> map=new ArrayList<List<Edge>>(Max);
    //存储路口id网 可以展示路口号相对关系
    static int[][] crossMap=new int[Max][Max];

    //存储两路口id与其距离的映射 10->15 20
    static Map<String, Road> RoadMapToDis=new HashMap<String, Road>();
    //存储路口方向与转弯方向的映射 10->15:Left
    static Map<String,Integer> roadToDirect=new HashMap<String, Integer>();
    //存储车辆在路口要转弯的方向关系映射 10:5(cross):left->15
    static Map<String,String> roadCrossDirectToRoad=new TreeMap<String, String>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });
    //存储道路和起始点与路口对象的映射 1000:2->roadDisMap
    static Map<String,Road> roadAndStartToRoadMap=new TreeMap<String, Road>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });
    //存储道路与终点是否存在的映射 5000:12 true
    static Map<String,Boolean> roadEndToBool=new HashMap<String, Boolean>();

    //存储在路上的车辆集合
    static List<Car> onRoadCar=new ArrayList<Car>();
    //存储等待区内的车辆
    static List<Car> waitToStart=new ArrayList<Car>();
    //存储路口id与cross对象的映射
    static Map<String,Cross> crossIdToCross=new TreeMap<String, Cross>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });

    static Map<String,Edge> crossIdToEdge=new HashMap<String, Edge>();

    //建图用的参数
    int startX=200,startY=200;
    int minX=Integer.MAX_VALUE,maxX=0,minY=Integer.MAX_VALUE,maxY=0;
    //路口的数量
    int allNum=0;
    public Graph(){
        for (int i=0;i<Max;i++){
            map.add(new ArrayList<Edge>());
        }
    }
    public void generateMap(List<String[]> roadArr,List<String[]> crossArr){
        //建立crossId与cross之间的映射
        for(int i=0;i<crossArr.size();i++){
            crossIdToCross.put(crossArr.get(i)[0],new Cross(crossArr.get(i)[0]));
        }

        //生成邻接表
        for(int i=1;i<roadArr.size();i++){
            String[] tempArr=roadArr.get(i);
            Edge tempEdge=new Edge(Integer.valueOf(tempArr[5]),Double.valueOf(tempArr[1])/Double.valueOf(tempArr[2]));
            map.get(Integer.valueOf(tempArr[4])).add(tempEdge);
            crossIdToEdge.put(tempArr[4]+"->"+tempArr[5],tempEdge);
            Road roadDisMap=new Road(tempArr[0],Integer.valueOf(tempArr[1]),Integer.valueOf(tempArr[2]),Integer.valueOf(tempArr[4]),Integer.valueOf(tempArr[5]),Integer.valueOf(tempArr[3]));
            //存储路口到路口的长度
            RoadMapToDis.put(tempArr[4]+"->"+tempArr[5],roadDisMap);

            //存储道路和开始点到道路对象的映射
            roadAndStartToRoadMap.put(tempArr[0]+":"+tempArr[4],roadDisMap);

            //判断是否有那条路
            roadEndToBool.put(tempArr[0]+":"+tempArr[5],true);

            //将道路加入所在路口
            crossIdToCross.get(tempArr[5]).addToRoad(roadDisMap);

            //如果道路支持双向,则将反向的在存储一遍
            if(Integer.valueOf(tempArr[6])==1){
                Edge tempEdge2=new Edge(Integer.valueOf(tempArr[4]),Double.valueOf(tempArr[1])/Double.valueOf(tempArr[2]));
                map.get(Integer.valueOf(tempArr[5])).add(tempEdge2);
                crossIdToEdge.put(tempArr[5]+"->"+tempArr[4],tempEdge2);
                RoadMapToDis.put(tempArr[5]+"->"+tempArr[4],roadDisMap);
                Road roadDisMap2=new Road(tempArr[0],Integer.valueOf(tempArr[1]),Integer.valueOf(tempArr[2]),Integer.valueOf(tempArr[5]),Integer.valueOf(tempArr[4]),Integer.valueOf(tempArr[3]));
                roadAndStartToRoadMap.put(tempArr[0]+":"+tempArr[5],roadDisMap2);
                crossIdToCross.get(tempArr[4]).addToRoad(roadDisMap2);
                crossIdToCross.get(tempArr[4]).roadSort();
                roadEndToBool.put(tempArr[0]+":"+tempArr[4],true);
            }
        }
        //创建道路之间的位置关系
        for(int i=1;i<crossArr.size();i++){
            String crossId=crossArr.get(i)[0];
            for(int j=1;j<crossArr.get(i).length-1;j++){
                for(int k=j+1;k<crossArr.get(i).length;k++){
                    if(crossArr.get(i)[j].equals("-1")||crossArr.get(i)[k].equals("-1")){
                        continue;
                    }
                    if(k-j==1){
                        roadToDirect.put(crossArr.get(i)[j]+"->"+crossArr.get(i)[k],Car.LEFT);
                        roadToDirect.put(crossArr.get(i)[k]+"->"+crossArr.get(i)[j],Car.RIGHT);
                        roadCrossDirectToRoad.put(crossArr.get(i)[j]+":"+crossId+":"+"left",crossArr.get(i)[k]);
                        roadCrossDirectToRoad.put(crossArr.get(i)[k]+":"+crossId+":"+"right",crossArr.get(i)[j]);
                    }else if(k-j==2){
                        roadToDirect.put(crossArr.get(i)[j]+"->"+crossArr.get(i)[k],Car.STRAIGHT);
                        roadToDirect.put(crossArr.get(i)[k]+"->"+crossArr.get(i)[j],Car.STRAIGHT);
                        roadCrossDirectToRoad.put(crossArr.get(i)[j]+":"+crossId+":"+"straight",crossArr.get(i)[k]);
                        roadCrossDirectToRoad.put(crossArr.get(i)[k]+":"+crossId+":"+"straight",crossArr.get(i)[j]);
                    }else if(k-j==3){
                        roadToDirect.put(crossArr.get(i)[j]+"->"+crossArr.get(i)[k],Car.RIGHT);
                        roadToDirect.put(crossArr.get(i)[k]+"->"+crossArr.get(i)[j],Car.LEFT);
                        roadCrossDirectToRoad.put(crossArr.get(i)[j]+":"+crossId+":"+"right",crossArr.get(i)[k]);
                        roadCrossDirectToRoad.put(crossArr.get(i)[k]+":"+crossId+":"+"left",crossArr.get(i)[j]);
                    }
                }
            }
        }

        //根据路口信息建构道路网
        //makeCrossMap(crossArr);

        //将路口id与坐标值一一联系
//        for(int i=minX;i<=maxX;i++){
//            for(int j=minY;j<=maxY;j++){
//                crossMapToZuo.put(crossMap[i][j],new coordinate(i,j));
//                zuoMapToCross.put(new coordinate(i,j).toString(),crossMap[i][j]);
//            }
//        }
//        for(int i=minX;i<=maxX;i++){
//            String temp="";
//            for(int j=minY;j<=maxY;j++){
//                //System.out.print(crossArr.get(i)[j]+" ");
//                temp+=crossMap[i][j];
//                temp+=" ";
//            }
//            temp=temp.substring(0,temp.length()-1);
//            System.out.println(temp);
//        }
        //System.out.println(allNum);
    }
    public void makeCrossMap(List<String[]> crossArr){
        //根据路口信息建构道路网
        crossMap[startX][startY]=Integer.valueOf(crossArr.get(1)[0]);
        String startF=crossArr.get(1)[1];
        crossArr.get(1)[1]="-1";
        genMapTable(crossArr,new coordinate(startX,startY),Integer.valueOf(startF),1);
        //allNum
    }
    public void genMapTable(List<String[]> crossArr, coordinate lastZuo, int roadID, int position){
        int nowPosition=0;
        if(position>0&&position<=2){
            nowPosition=position+2;
        }else{
            nowPosition=(position+3)%5;
        }
        coordinate curZuo=null;
        for(int i=1;i<crossArr.size();i++){
            if(Integer.valueOf(crossArr.get(i)[nowPosition])==roadID){
                allNum++;
                switch (position){
                    case 1:
                        crossMap[lastZuo.x-1][lastZuo.y]=Integer.valueOf(crossArr.get(i)[0]);
                        curZuo=new coordinate(lastZuo.x-1,lastZuo.y);
                        break;
                    case 2:
                        crossMap[lastZuo.x][lastZuo.y+1]=Integer.valueOf(crossArr.get(i)[0]);
                        curZuo=new coordinate(lastZuo.x,lastZuo.y+1);
                        break;
                    case 3:
                        crossMap[lastZuo.x+1][lastZuo.y]=Integer.valueOf(crossArr.get(i)[0]);
                        curZuo=new coordinate(lastZuo.x+1,lastZuo.y);
                        break;
                    case 4:
                        crossMap[lastZuo.x][lastZuo.y-1]=Integer.valueOf(crossArr.get(i)[0]);
                        curZuo=new coordinate(lastZuo.x,lastZuo.y-1);
                        break;
                }
                //求出坐标的最大最小值
                minX=Math.min(minX,curZuo.x);
                maxX=Math.max(maxX,curZuo.x);
                minY=Math.min(minY,curZuo.y);
                maxY=Math.max(maxY,curZuo.y);
                crossArr.get(i)[nowPosition]="-1";
                for(int j=1;j<=4;j++){
                    if(!crossArr.get(i)[j].equals("-1")){
                        String startF=crossArr.get(i)[j];
                        crossArr.get(i)[j]="-1";
                        genMapTable(crossArr,curZuo,Integer.valueOf(startF),j);
                    }
                }
                break;
            }
        }
    }
    public void loadAnswer(List<String[]> ansArr,List<String[]> carArr){
        Map<String,String[]> search=new HashMap<String, String[]>();
        for(int i=0;i<carArr.size();i++){
            search.put(carArr.get(i)[0],carArr.get(i));
        }
        for(int i=0;i<ansArr.size();i++){
            String carId=ansArr.get(i)[0];
            String startTime=ansArr.get(i)[1];
            String startCrossId=null,endCrossId=null,speed=null;
            List<Integer> pathArr=new ArrayList<Integer>();
            for(int k=2;k<ansArr.get(i).length;k++){
                pathArr.add(Integer.valueOf(ansArr.get(i)[k]));
            }
            System.out.println("已加载:"+i+"辆车");
            String[] target=search.get(carId);
            startCrossId=target[1];
            endCrossId = target[2];
            speed = target[3];
            Car temp = new Car(carId, Integer.valueOf(startCrossId), Integer.valueOf(endCrossId), Integer.valueOf(speed), pathArr, Integer.valueOf(startTime), 0);
            waitToStart.add(temp);
//            for(int j=1;j<carArr.size();j++){
//                if(carArr.get(j)[0].equals(carId)){
//                    startCrossId=carArr.get(j)[1];
//                    endCrossId=carArr.get(j)[2];
//                    speed=carArr.get(j)[3];
//                    Car temp=new Car(carId,Integer.valueOf(startCrossId),Integer.valueOf(endCrossId),Integer.valueOf(speed),pathArr,Integer.valueOf(startTime),0);
//                    waitToStart.add(temp);
//                    break;
//                }
//            }
            waitToStart.sort(new Comparator<Car>() {
                @Override
                public int compare(Car o1, Car o2) {
                    if(o1.planStartTime==o2.planStartTime){
                        return o1.id.compareTo(o2.id);
                    }
                    return o1.planStartTime<o2.planStartTime?-1:1;
                }
            });
        }

    }
    public int startDispatch(){
        int i=0;
        while(onRoadCar.size()!=0||waitToStart.size()!=0){
            dispatch(++i);
            System.out.println(i);
        }
        return i;
    }
    public void dispatch(int timeClip){
        //第一步遍历所有道路上的车
        for(Map.Entry<String,Road> entry:roadAndStartToRoadMap.entrySet()){
            Road tempRoad=entry.getValue();
            tempRoad.updateCarInRoad();
        }
        //第二部，遍历所有路口,这一步要多次循环(要改进)
        int time=1;
        while(time<15){
            for(Map.Entry<String,Cross> entry:crossIdToCross.entrySet()){
                Cross curCross=entry.getValue();
                curCross.crossDispatch();
            }
            time++;
        }
        DeadInfo deadLockRes=checkDeadLock();
        if(deadLockRes.deadLock.size()>0){
            StringBuilder sb=new StringBuilder();
            sb.append("路口:");
            for(int i:deadLockRes.deadLock){
                sb.append(i);
                sb.append(',');
            }
            sb.setLength(sb.length()-1);
            sb.append("发生死锁");
            System.out.println(sb.toString());
            System.exit(0);
        }
        //安排等待队列的车辆上车
        if(waitToStart.size()>0){
            if(waitToStart.get(0).planStartTime<=timeClip){
                ListIterator<Car> it=waitToStart.listIterator();
                while (it.hasNext()){
                    Car tempCar=it.next();
                    if(tempCar.planStartTime>timeClip){
                        break;
                    }
                    int targetRoadId=tempCar.pathArr.get(0);
                    int startCrossId=tempCar.startId;
                    Road targetRoad=roadAndStartToRoadMap.get(targetRoadId+":"+startCrossId);
                    tempCar.curSpeed=Math.min(targetRoad.limitSpeed,tempCar.maxSpeed);
                    int[] res=targetRoad.checkDriveWayNo(tempCar.curSpeed,0);
                    if(res[0]==FlagSet.ALL_STOP||res[0]==FlagSet.NOT_FIND){
                        continue;
                    }else{
                        targetRoad.insertCar(tempCar,res[0],Car.STOP,res[1],0);
                        onRoadCar.add(tempCar);
                        it.remove();
                    }
                }
            }
        }

    }
    public DeadInfo checkDeadLock(){
        Set<Integer> deadCross=new LinkedHashSet<Integer>();
        Set<Car> deadCar=new LinkedHashSet<Car>();
        for(int i=0;i<onRoadCar.size();i++){
            Car tempCar=onRoadCar.get(i);
            if(tempCar.status==Car.WAIT_TO_GO){
                deadCross.add(tempCar.pos.endId);
                deadCar.add(tempCar);
            }
        }
        return new DeadInfo(deadCross,deadCar);
    }
    public List<List<Integer>> startSearch(List<String[]> carArr){
        return AStar.startSearch(carArr);
    }
    public List<Integer> SearchOne(Car car){
        List<Integer> res=AStar.Search(car.startId,car.endId,car.planStartTime);
        Collections.reverse(res);
        return res;
    }
}
