import sys
import argparse
from os.path import exists

# This script accepts commands to scroll a virtual map display.


# The total size of your full map, values in each array are minecraft map ids.
map_data = [
    [ 3,  4,  5,  6,  7],
    [ 2,  1,  0,  9,  8],
    [14, 13, 10, 11, 12],
]

initial_x = 2
initial_y = 2

cmds = {
    'left': (0, -1),
    'right': (0, 1),
    'up': (-1, 0),
    'down': (1, 0),
}


def load_pos(x, y, z):
    fn = "map-pos-{}-{}-{}.txt".format(x, y, z)
    if exists(fn):
        with open(fn) as f:
            line = f.readline().strip()
            return [int(v) for v in line.split(',')]
    return initial_x, initial_y


def save_pos(x, y, z, mx, my):
    fn = "map-pos-{}-{}-{}.txt".format(x, y, z)
    with open(fn, 'w+') as f:
        f.write('{},{}'.format(mx, my))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Scroll a minecraft map.')
    parser.add_argument('direction', type=str, help='Command (left,right,up,down)')
    parser.add_argument('x', type=int)
    parser.add_argument('y', type=int)
    parser.add_argument('z', type=int)
    parser.add_argument('size_x', type=int, help='Horizontal size of the map')
    parser.add_argument('size_y', type=int, help='Vertical size of the map')
    args = parser.parse_args()
    if args.direction not in cmds:
        print("msg @p Invalid direction: {}".format(args.direction))
        sys.exit(1)

    offset_x = int((args.size_x - 1) / 2)
    offset_y = int((args.size_y - 1) / 2)

    x, y = load_pos(args.x, args.y, args.z)
    x_chg, y_chg = cmds[args.direction]

    x += x_chg
    x = min(max(x, offset_x), len(map_data) - (1 + offset_x))

    y += y_chg
    y = min(max(y, offset_y), len(map_data[x]) - (1 + offset_y))

    save_pos(args.x, args.y, args.z, x, y)

    for mx in range(-offset_x, offset_x + 1):
        for my in range(-offset_y, offset_y + 1):
            # TODO This only works in one dimension..
            map_id = map_data[x + mx][y + my]
            cmd = "data modify entity @e[nbt={TileX:%s,TileY:%s,TileZ:%s},type=minecraft:item_frame,limit=1] Item set value {id:\"minecraft:filled_map\", tag: {map: %s}, Count: 1}"
            cmd = cmd % (args.x - my, args.y - mx, args.z, map_id)
            print(cmd)

    sys.exit(0)
