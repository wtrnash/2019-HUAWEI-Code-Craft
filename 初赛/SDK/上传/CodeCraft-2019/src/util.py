from collections import defaultdict


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


class Graph:
    def __init__(self, roads):
        """
        构造函数，建立路口所有连通的道路.
        :param roads: 所有道路.
        """
        self.weights = {}
        self.roads = {}                 # 每个道路id对应的道路
        self.map = defaultdict(list)    # 路口对应连接道路的字典
        for road in roads:
            self.weights[road.road_id] = [road.length / road.speed for _ in range(2800)]
            self.roads[road.road_id] = road
            self.map[road.begin].append(road)
            if road.is_duplex == 1:
                self.map[road.end].append(Road(road.road_id, road.length, road.speed, road.channel, road.end,
                                               road.begin, road.is_duplex))

        self.cross_num = len(self.map)

    def dijkstra(self, begin, end, start_time):
        """
        迪杰斯特拉最短路径.
        :param car: 对应车的最短路径.
        :return: 该车对应的规划路径.
        """
        # 初始化
        visited = [begin]   # 是否访问
        distance = {begin: 0}   # 距离
        previous = {}   # 前驱节点
        road_list = self.map[begin]
        for road in road_list:
            distance[road.end] = self.weights[road.road_id][start_time]
            previous[road.end] = begin

        for i in range(1, self.cross_num):
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
                            for j in range(start_time, start_time + 10):
                                self.weights[road.road_id][j] += 1 / self.roads[road.road_id].channel
                            break
                    u = previous[u]

                road_list = self.map[begin]
                for road in road_list:
                    if road.end == u:
                        path.insert(0, road.road_id)
                        for j in range(start_time, start_time + 10):
                            self.weights[road.road_id][j] += 1 / self.roads[road.road_id].channel
                        break

                return path

            # 更新距离
            road_list = self.map[u]
            for road in road_list:
                d = self.weights[road.road_id][start_time]
                if road.end not in distance.keys():
                    distance[road.end] = temp + d
                    previous[road.end] = u
                elif temp + d < distance[road.end]:
                    distance[road.end] = temp + d
                    previous[road.end] = u

        return []

