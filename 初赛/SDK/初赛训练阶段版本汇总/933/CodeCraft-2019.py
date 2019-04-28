import logging
import sys
import pandas as pd
import util
from collections import defaultdict
import copy


def read_input_file(car_path, road_path, cross_path):
    # 读入车辆数据
    car_data = pd.read_csv(car_path)
    car_data.columns = ['id', 'from', 'to', 'speed', 'planTime']

    car_data[car_data.columns[0]] = car_data[car_data.columns[0]].str.lstrip('(').apply(pd.to_numeric)
    car_data[car_data.columns[car_data.columns.size - 1]] = car_data[car_data.columns[car_data.columns.size - 1]].\
        str.rstrip(')').apply(
        pd.to_numeric)

    cars = []
    for index, row in car_data.iterrows():
        car = util.Car(row['id'], row['from'], row['to'], row['speed'], row['planTime'])
        cars.append(car)

    # 读入道路数据
    road_data = pd.read_csv(road_path)
    road_data.columns = ['id', 'length', 'speed', 'channel', 'from', 'to', 'isDuplex']

    road_data[road_data.columns[0]] = road_data[road_data.columns[0]].str.lstrip('(').apply(pd.to_numeric)
    road_data[road_data.columns[road_data.columns.size - 1]] = road_data[
        road_data.columns[road_data.columns.size - 1]].str.rstrip(')').apply(
        pd.to_numeric)

    roads = []
    for index, row in road_data.iterrows():
        road = util.Road(row['id'], row['length'], row['speed'],
                         row['channel'], row['from'], row['to'], row['isDuplex'])
        roads.append(road)

    # 读入路口数据
    cross_data = pd.read_csv(cross_path)
    cross_data.columns = ['id', 'up', 'right', 'down', 'left']

    cross_data[cross_data.columns[0]] = cross_data[cross_data.columns[0]].str.lstrip('(').apply(pd.to_numeric)
    cross_data[cross_data.columns[cross_data.columns.size - 1]] = \
        cross_data[cross_data.columns[cross_data.columns.size - 1]].str.rstrip(')').apply(pd.to_numeric)

    crosses = []
    for index, row in cross_data.iterrows():
        cross = util.Cross(row['id'], row['up'], row['right'], row['down'], row['left'])
        crosses.append(cross)

    return cars, roads, crosses


# 建立起路口的左右位置索引
def left_index_build(cross, get_cross_by_road):
    up_index = cross.up_index
    left_road = cross.left_id
    left_index = -1
    c = copy.deepcopy(cross)
    # 往左遍历
    while left_road != -1:
        temp = get_cross_by_road[left_road]
        if temp[0] == c.cross_id:
            left_cross = temp[1]
        else:
            left_cross = temp[0]

        cross_map[left_cross].left_index = left_index
        cross_map[left_cross].up_index = up_index
        left_index -= 1
        left_road = cross_map[left_cross].left_id
        c = copy.deepcopy(cross_map[left_cross])

    right_road = cross.right_id
    left_index = 1
    c = copy.deepcopy(cross)
    # 往右遍历
    while right_road != -1:
        temp = get_cross_by_road[right_road]
        if temp[0] == c.cross_id:
            right_cross = temp[1]
        else:
            right_cross = temp[0]
        cross_map[right_cross].left_index = left_index
        cross_map[right_cross].up_index = up_index
        left_index += 1
        right_road = cross_map[right_cross].right_id
        c = copy.deepcopy(cross_map[right_cross])


# 建立起路口的上下位置索引，并在循环中也建立起左右位置索引
def up_index_build(cross, get_cross_by_road, up_index, left_index):
    # 建立当前位置的左右位置索引
    cross.left_index = left_index
    cross.up_index = up_index
    left_index_build(cross, get_cross_by_road)

    up_road = cross.up_id
    up_index = -1
    c = copy.deepcopy(cross)
    # 往上遍历
    while up_road != -1:
        temp = get_cross_by_road[up_road]
        if temp[0] == c.cross_id:
            up_cross = temp[1]
        else:
            up_cross = temp[0]

        cross_map[up_cross].up_index = up_index
        cross_map[up_cross].left_index = left_index
        left_index_build(cross_map[up_cross], get_cross_by_road)
        up_index -= 1
        up_road = cross_map[up_cross].up_id
        c = copy.deepcopy(cross_map[up_cross])

    down_road = cross.down_id
    up_index = 1
    c = copy.deepcopy(cross)
    # 往下遍历
    while down_road != -1:
        temp = get_cross_by_road[down_road]
        if temp[0] == c.cross_id:
            down_cross = temp[1]
        else:
            down_cross = temp[0]

        cross_map[down_cross].up_index = up_index
        cross_map[down_cross].left_index = left_index
        left_index_build(cross_map[down_cross], get_cross_by_road)
        up_index += 1
        down_road = cross_map[down_cross].down_id
        c = copy.deepcopy(cross_map[down_cross])


# 建立路口之间相互关系
def build_cross_depend(roads, crosses):
    # 确定路口之间的上下左右关系
    get_cross_by_road = {}
    for road in roads:
        get_cross_by_road[road.road_id] = [road.begin, road.end]

    cross = crosses[0]  # 以第一个路口开始
    up_index = 0
    left_index = 0
    up_index_build(cross, get_cross_by_road, up_index, left_index)
    untreated_crosses = []
    # 处理遗漏路口
    for cross in crosses:
        if cross.up_index == -100000 or cross.left_index == -100000:
            untreated_crosses.append(cross)

    while untreated_crosses:
        for cross in untreated_crosses:
            if cross.up_id != -1:
                t = get_cross_by_road[cross.up_id]
                if t[0] == cross.cross_id:
                    t = t[1]
                else:
                    t = t[0]

                if cross_map[t].up_index != -100000:
                    cross.up_index = cross_map[t].up_index + 1
                    cross.left_index = cross_map[t].left_index
                    untreated_crosses.remove(cross)
                    continue

            if cross.left_id != -1:
                t = get_cross_by_road[cross.left_id]
                if t[0] == cross.cross_id:
                    t = t[1]
                else:
                    t = t[0]

                if cross_map[t].up_index != -100000:
                    cross.up_index = cross_map[t].up_index
                    cross.left_index = cross_map[t].left_index + 1
                    untreated_crosses.remove(cross)
                    continue

            if cross.down_id != -1:
                t = get_cross_by_road[cross.down_id]
                if t[0] == cross.cross_id:
                    t = t[1]
                else:
                    t = t[0]

                if cross_map[t].up_index != -100000:
                    cross.up_index = cross_map[t].up_index - 1
                    cross.left_index = cross_map[t].left_index
                    untreated_crosses.remove(cross)
                    continue

            if cross.right_id != -1:
                t = get_cross_by_road[cross.right_id]
                if t[0] == cross.cross_id:
                    t = t[1]
                else:
                    t = t[0]

                if cross_map[t].up_index != -100000:
                    cross.up_index = cross_map[t].up_index
                    cross.left_index = cross_map[t].left_index - 1
                    untreated_crosses.remove(cross)
                    continue


def get_direction(begin, end):
    begin = cross_map[begin]
    end = cross_map[end]
    if begin.left_index <= end.left_index and begin.up_index >= end.up_index:   # 右上
        direction = 1
    elif begin.left_index <= end.left_index and begin.up_index <= end.up_index:  # 右下
        direction = 2
    elif begin.left_index >= end.left_index and begin.up_index >= end.up_index:   # 左上
        direction = 3
    else:                                                                          # 左下
        direction = 4

    return direction


def sort_car(x):
    return -x.speed, x.plan_time


def main():
    global cross_map
    if len(sys.argv) != 5:
        logging.info('please input args: car_path, road_path, cross_path, answerPath')
        exit(1)

    car_path = sys.argv[1]
    road_path = sys.argv[2]
    cross_path = sys.argv[3]
    answer_path = sys.argv[4]

    # to read input file
    cars, roads, crosses = read_input_file(car_path, road_path, cross_path)

    # process
    # 建立cross_id和对应cross的关系
    cross_map = {}
    for cross in crosses:
        cross_map[cross.cross_id] = cross

    # build_cross_depend(roads, crosses)

    graph = util.Graph(roads)
    paths = []

    # 按速度降序以及出发时间升序进行排序
    cars = sorted(cars, key=sort_car)

    # 同时同个地点同一个方向出发的不超过3辆

    m = defaultdict(int)
    for index in range(len(cars)):
        while (cars[index].plan_time, cars[index].begin) in m.keys():
            if m[(cars[index].plan_time, cars[index].begin)] >= 3:
                cars[index].plan_time += 1
            else:
                break

        m[(cars[index].plan_time, cars[index].begin)] += 1

    cars = sorted(cars, key=sort_car)

    cars_num = len(cars)
    count = 0
    for car in cars:
        count += 1
        car.plan_time += count // 34

        path = graph.dijkstra(car)
        path.insert(0, car.plan_time)
        path.insert(0, car.car_id)
        paths.append(path)

    time = []
    for path in paths:
        time.append(path[1])
    print(pd.value_counts(time))

    # to write output file
    with open(answer_path, 'w') as f:
        for path in paths:
            f.write("(")
            n = len(path)
            for i in range(n):
                f.write(str(path[i]))
                if not i == n - 1:
                    f.write(',')
            f.write(")\n")


if __name__ == "__main__":
    main()