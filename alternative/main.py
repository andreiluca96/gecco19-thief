import math

import numpy as np
from deap import base, creator

from data.repository import read_problem

creator.create("FitnessMinMax", base.Fitness, weights=(-1.0, 1.0))
creator.create("Individual", dict, fitness=creator.FitnessMinMax)

import random
from deap import tools

problem = read_problem("test-example-n4")

print("### Problem instance ###")
print(problem.items)
print(problem.cities)
print("###")


def initIndividual(container, city_func, item_func):
    return container(zip(['cities', 'items'], [city_func(), item_func()]))


def initPermutation():
    return random.sample(range(problem.no_cities - 1), problem.no_cities - 1)


def initZeroOne():
    return 1 if random.random() > 0.9 else 0


def euclidean_distance(a, b):
    return math.sqrt(
        math.pow(problem.cities[a][0] - problem.cities[b][0], 2) + math.pow(problem.cities[a][1] - problem.cities[b][1],
                                                                            2))


def evaluate(individual):
    pi = individual['cities']
    z = individual['items']

    # print(full_pi, z)

    profit = 0
    time = 0
    weight = 0

    full_pi = [x + 1 for x in pi]

    for index, city in enumerate([0] + full_pi):
        possible_items_for_current_city = problem.items.get(city, [])
        items_collected_for_current_city = filter(lambda x: z[x[0] - 1] == 1, possible_items_for_current_city)

        for item in items_collected_for_current_city:
            profit += item[1]
            weight += item[2]

        speed = problem.max_speed - (weight / problem.knapsack_capacity) * (problem.max_speed - problem.min_speed)
        next = full_pi[(index + 1) % (problem.no_cities - 1)]

        # print("Cities: ", city, next)

        distance = math.ceil(euclidean_distance(city, next))

        # print(distance)

        # print(distance, speed)

        time += distance / speed

        if weight > problem.knapsack_capacity:
            time = np.inf
            profit = - np.inf
            break

    return time, profit


def crossover(ind1, ind2, city_crossover, item_crossover, indpb1, indpb2):
    pi1 = ind1['cities']
    z1 = ind1['items']

    pi2 = ind2['cities']
    z2 = ind2['items']

    city_crossover_result1, city_crossover_result2 = city_crossover(pi1, pi2)
    item_crossover_result1, item_crossover_result2 = item_crossover(z1, z2)

    return initIndividual(creator.Individual, lambda: city_crossover_result1, lambda: item_crossover_result1), \
           initIndividual(creator.Individual, lambda: city_crossover_result2, lambda: item_crossover_result2)


def mutation(ind, city_mutation, item_mutation, indpb1, indpb2):
    pi = ind['cities']
    z = ind['items']

    return initIndividual(creator.Individual, lambda: city_mutation(pi, indpb1), lambda: item_mutation(z, indpb2))


toolbox = base.Toolbox()

toolbox.register("city_attribute", initPermutation)
toolbox.register("items_attribute", tools.initRepeat, list, initZeroOne, problem.no_items)
toolbox.register("individual", initIndividual, creator.Individual, toolbox.city_attribute, toolbox.items_attribute)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("evaluate", evaluate)
toolbox.register("mate", crossover, city_crossover=tools.cxPartialyMatched, item_crossover=tools.cxOnePoint, indpb1=None, indpb2=None)
toolbox.register("mutate", mutation, city_mutation=tools.mutShuffleIndexes, item_mutation=tools.mutFlipBit, indpb1=0.05, indpb2=0.05)
toolbox.register("select", tools.selNSGA2)

stats = tools.Statistics(key=lambda ind: ind.fitness.values)
stats.register("avg", np.mean, axis=0)
stats.register("std", np.std, axis=0)
stats.register("min", np.min, axis=0)
stats.register("max", np.max, axis=0)

# stats.register("profit-avg", np.mean, axis=1)
# stats.register("profit-std", np.std, axis=1)
# stats.register("profit-min", np.min, axis=1)
# stats.register("profit-max", np.max, axis=1)


pop = toolbox.population(n=10)
CXPB, MUTPB, NGEN = 0.3, 0.1, 50

# Evaluate the entire population
fitnesses = map(toolbox.evaluate, pop)
for ind, fit in zip(pop, fitnesses):
    ind.fitness.values = fit

for g in range(NGEN):
    # Logging current population fitnesses
    record = stats.compile(pop)
    print(record)

    # Select the next generation individuals
    offspring = toolbox.select(pop, 10)
    # Clone the selected individuals
    offspring = list(map(toolbox.clone, offspring))

    # Apply crossover and mutation on the offspring
    for child1, child2 in zip(offspring[::2], offspring[1::2]):
        if random.random() < CXPB:
            toolbox.mate(child1, child2)
            del child1.fitness.values
            del child2.fitness.values

    for mutant in offspring:
        if random.random() < MUTPB:
            toolbox.mutate(mutant)
            del mutant.fitness.values

    # Evaluate the individuals with an invalid fitness
    invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
    fitnesses = map(toolbox.evaluate, invalid_ind)
    for ind, fit in zip(invalid_ind, fitnesses):
        ind.fitness.values = fit

    # The population is entirely replaced by the offspring
    pop[:] = offspring

for ind in pop:
    print([1] + [x + 2 for x in ind['cities']], ind['items'], evaluate(ind))


