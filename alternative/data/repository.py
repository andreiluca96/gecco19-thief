import os

from data.problem import Problem

INPUT_DIRECTORY = "input"


def read_problem(problem_name):

    cities = []
    items = {}
    with open(os.path.join(INPUT_DIRECTORY, problem_name + ".txt")) as f:
        lines = f.readlines()

        line_no = 0
        while line_no < len(lines):
            line = lines[line_no]

            if 'DIMENSION' in line:
                no_cities = int(line.split(":")[1].strip())
            elif 'NUMBER OF ITEMS' in line:
                no_items = int(line.split(":")[1].strip())
            elif 'CAPACITY OF KNAPSACK' in line:
                knapsack_capacity = int(line.split(":")[1].strip())
            elif 'MIN SPEED' in line:
                min_speed = float(line.split(":")[1].strip())
            elif 'MAX SPEED' in line:
                max_speed = float(line.split(":")[1].strip())
            elif 'NODE_COORD_SECTION' in line:

                line_no += 1
                line = lines[line_no]
                for node_no in range(no_cities):
                    coords = line.split()
                    cities.append((float(coords[1].strip()), float(coords[2].strip())))

                    line_no += 1
                    line = lines[line_no]

                line_no -= 1

            elif 'ITEMS SECTION' in line:

                line_no += 1
                line = lines[line_no]
                for node_no in range(no_items):
                    item = line.split()

                    city = int(item[3].strip()) - 1
                    if city not in items:
                        items[city] = []

                    items[city].append([int(item[0].strip()), float(item[1].strip()), float(item[2].strip())])

                    line_no += 1
                    if line_no == len(lines):
                        break
                    line = lines[line_no]

            line_no += 1

        problem = Problem()

        problem.no_cities = no_cities
        problem.no_items = no_items
        problem.knapsack_capacity = knapsack_capacity
        problem.min_speed = min_speed
        problem.max_speed = max_speed

        problem.cities = cities
        problem.items = items

        return problem


if __name__ == "__main__":
    problem = read_problem("a280-n279")

    print(problem.items)

    assert problem.no_cities >= len(problem.items.keys())
    assert problem.no_cities == len(problem.cities)


