import logging
import sys
import pandas as pd
import util
from collections import defaultdict


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


def sort_car(x):
    return -x.speed, x.plan_time


def main():
    if len(sys.argv) != 5:
        logging.info('please input args: car_path, road_path, cross_path, answerPath')
        exit(1)

    car_path = sys.argv[1]
    road_path = sys.argv[2]
    cross_path = sys.argv[3]
    answer_path = sys.argv[4]

    # to read input file
    cars, roads, crosses = read_input_file(car_path, road_path, cross_path)

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

    count = 0
    for car in cars:
        count += 1
        car.plan_time += count // 32
        path = graph.dijkstra(car.begin, car.end, car.plan_time)
        path.insert(0, car.plan_time)
        path.insert(0, car.car_id)
        paths.append(path)

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