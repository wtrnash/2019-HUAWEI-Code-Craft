from collections import defaultdict
from math import ceil


class Car:
    def __init__(self, car_id, begin, end, speed, plan_time):
        """
        初始化汽车类.
        :param car_id: 车辆id.
        :param begin: 始发地.
        :param end: 目的地.
        :param speed: 最高速度.
        :param plan_time:  计划出发时间.
        """
        self.car_id = car_id
        self.begin = begin
        self.end = end
        self.speed = speed
        self.plan_time = plan_time


class Road:
    def __init__(self, road_id, length, speed, channel, begin, end, is_duplex):
        """
        初始化道路类.
        :param road_id: 道路id.
        :param length: 道路长度.
        :param speed: 最高限速.
        :param channel: 车道数目.
        :param begin: 起始点id.
        :param end: 终点id.
        :param is_duplex: 是否双向.
        """
        self.road_id = road_id
        self.length = length
        self.speed = speed
        self.channel = channel
        self.begin = begin
        self.end = end
        self.is_duplex = is_duplex


class Cross:
    def __init__(self, cross_id, up_id, right_id, down_id, left_id):
        """
        初始化路口类.
        :param cross_id: 路口id.
        :param up_id: 上面的道路id.
        :param right_id: 右边的道路id.
        :param down_id: 下面的道路id.
        :param left_id: 左边的道路id.
        """
        self.cross_id = cross_id
        self.up_id = up_id
        self.right_id = right_id
        self.down_id = down_id
        self.left_id = left_id
        self.left_index = -100000
        self.up_index = -100000


class Graph:
    def __init__(self, roads):
        """
        构造函数，建立路口所有连通的道路.
        :param roads: 所有道路.
        """
        self.roads = {}                 # 每个道路id对应的道路
        self.map = defaultdict(list)    # 路口对应连接道路的字典
        self.cars_in_roads = {}    # 每条道路的车辆情况维护，每次有车经过，记录该车在该道路上的通行时间
        for road in roads:
            self.roads[road.road_id] = road
            self.map[road.begin].append(road)
            self.cars_in_roads[road.road_id] = []
            if road.is_duplex == 1:
                self.map[road.end].append(Road(road.road_id, road.length, road.speed, road.channel, road.end,
                                               road.begin, road.is_duplex))

    def get_time(self, road_id, v1, v2, length, start_time):
        """
        车通过该路的时间，根据路况对纯粹时间加上权值.
        :param v1: 汽车最高速度.
        :param road_id: 通过的道路id.
        :param v2: 道路的最高速度.
        :param length: 道路长度.
        :param start_time: 该车的出发时间
        :return: 处理后的通过时间.
        """
        car_time_list = self.cars_in_roads[road_id]
        weight = 0
        car_num = 0
        index = 0
        for time in car_time_list:
            if time[1] + 20 < start_time:   # 因为车子按时间顺序进来，所以为了效率，移除已经开走的车子时间组
                index += 1
            if time[0] - 20 <= start_time <= time[1] + 20:   # 该车的出发时间和在路段上车的时间大致相重合
                car_num += 1

        self.cars_in_roads[road_id] = car_time_list[index:]
        channel_number = self.roads[road_id].channel

        weight += (car_num // channel_number) * 10

        return self.get_pure_time(v1, v2, length) + weight

    def get_pure_time(self, v1, v2, length):
        """
        车通过该路的纯粹时间.
        :param v1: 汽车最高速度.
        :param v2: 道路的最高速度.
        :param length: 道路长度.
        :return: 通过时间.
        """

        return ceil(length / min(v1, v2))

    def calculate_time(self, path, v, start_time):
        """
        计算该车在每段路径上所处的时间段
        :param path: 该车的最短路径
        :param v: 车速
        :param start_time: 车子的出发时间
        :return:
        """
        for p in path:
            time = self.get_pure_time(v, self.roads[p].speed, self.roads[p].length)
            self.cars_in_roads[p].append([start_time, start_time + time])
            start_time += time

    def dijkstra(self, car):
        """
        迪杰斯特拉最短路径.
        :param car: 对应车的最短路径.
        :return: 该车对应的规划路径.
        """
        begin = car.begin
        end = car.end
        v = car.speed
        start_time = car.plan_time
        # 初始化
        visited = [begin]   # 是否访问
        distance = {begin: 0}   # 距离
        previous = {}   # 前驱节点
        road_list = self.map[begin]
        for road in road_list:
            distance[road.end] = self.get_time(road.road_id, v, road.speed, road.length, start_time)
            previous[road.end] = begin

        cross_num = len(self.map)

        for i in range(1, cross_num):
            temp = 2 ** 31
            u = begin
            # 找到没被访问过且路径最短的端点
            for j in distance.keys():
                if j in visited:
                    continue
                if distance[j] < temp:
                    temp = distance[j]
                    u = j

            visited.append(u)
            # 如果到达终点
            if u == end:
                path = []
                while not previous[u] == begin:
                    road_list = self.map[previous[u]]
                    for road in road_list:
                        if road.end == u:
                            path.insert(0, road.road_id)
                            break
                    u = previous[u]

                road_list = self.map[begin]
                for road in road_list:
                    if road.end == u:
                        path.insert(0, road.road_id)
                        break
                self.calculate_time(path, v, start_time)    # 将选择这条路径车在每段路径上的时间进行记录
                return path

            # 更新距离
            road_list = self.map[u]
            for road in road_list:
                d = self.get_time(road.road_id, v, road.speed, road.length, start_time)
                if road.end not in distance.keys():
                    distance[road.end] = temp + d
                    previous[road.end] = u
                elif temp + d < distance[road.end]:
                    distance[road.end] = temp + d
                    previous[road.end] = u

        return []

