import sys
import argparse
from os.path import exists

# This script accepts commands to scroll a virtual map display.


# The total size of your full map, values in each array are minecraft map ids.
map_data = [
    [709, 709, 709, 709, 709, 709, 709, 709, 709, 709],
    [709, 709, 709, 435, 430, 555, 550, 545, 709, 709],
    [709, 709, 445, 440, 425, 570, 619, 540, 535, 709],
    [709, 709, 450, 560, 420, 565, 609, 604, 530, 709],
    [709, 460, 455, 575, 415, 410, 614, 599, 525, 709],
    [709, 465, 624, 629, 698, 669, 687, 515, 520, 709],
    [709, 470, 475, 634, 703, 708, 505, 510, 709, 709],
    [709, 709, 480, 485, 490, 495, 500, 709, 709, 709],
    [709, 709, 709, 709, 709, 709, 709, 709, 709, 709],
]

initial_x = 3
initial_y = 3

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

    x, y = load_pos(args.x, args.y, args.z)

    if args.direction == "tp":
        base_x = -9290
        base_z = -7112
        tp_x = base_x + (2048 * y)
        tp_z = base_z + (2048 * x)
        print("tp @p {} 100 {}".format(tp_x, tp_z))
        sys.exit(0)

    if args.direction not in cmds:
        print("msg @p Invalid direction: {}".format(args.direction))
        sys.exit(1)

    offset_x = int((args.size_x - 1) / 2)
    offset_y = int((args.size_y - 1) / 2)

    x_chg, y_chg = cmds[args.direction]

    x += x_chg
    x = min(max(x, offset_x), len(map_data) - (1 + offset_x))

    y += y_chg
    y = min(max(y, offset_y), len(map_data[x]) - (1 + offset_y))

    save_pos(args.x, args.y, args.z, x, y)

    for mx in range(-offset_x, offset_x + 1):
        for my in range(-offset_y, offset_y + 1):
            # TODO This only works in one dimension..
            map_id = map_data[x + mx][y - my]
            cmd = "data modify entity @e[nbt={TileX:%s,TileY:%s,TileZ:%s},type=minecraft:item_frame,limit=1] Item set value {id:\"minecraft:filled_map\", tag: {map: %s}, Count: 1}"
            cmd = cmd % (args.x - my, args.y - mx, args.z, map_id)
            print(cmd)

    sys.exit(0)
