import matplotlib.pyplot as plt

file_name = "TEAM_JENETICS_a280-n279.f"
file = open("data/input/%s" % file_name, "r")

lines = file.readlines()

cost = []
time = []

for line in lines:
    splits = line.split(" ")

    if len(splits) == 2:
        c = float(splits[0])
        t = float(splits[1])

        cost.append(c)
        time.append(t)


plt.plot(cost, time, 'ro')
plt.xlabel('cost')
plt.ylabel('time')

plt.savefig('data/output/fig.jpg')
plt.show()
