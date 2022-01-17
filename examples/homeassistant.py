import sys
import json
import argparse
import requests

URL = "http://localhost:8123/api/"
ACCESS_TOKEN = "youraccesstokengoeshere"


def get_state(entity):
    url = URL + 'states/' + args.entity
    headers = {
        "Authorization": "Bearer {}".format(ACCESS_TOKEN),
        "content-type": "application/json",
    }
    response = requests.get(url, headers=headers)
    state = response.json().get('state', 'off')
    print("State: {}".format(state))
    sys.exit(state == 'on')


def set_state(entity, service_name, exit_code):
    url = URL + 'services/light/' + service_name
    headers = {
        "Authorization": "Bearer {}".format(ACCESS_TOKEN),
        "content-type": "application/json",
    }
    response = requests.post(url, headers=headers, data=json.dumps({'entity_id': entity}))
    res = response.json()
    print("{}: {}".format(url, response.status_code))
    sys.exit(exit_code)


def toggle_state(entity):
    url = URL + 'services/light/toggle'
    headers = {
        "Authorization": "Bearer {}".format(ACCESS_TOKEN),
        "content-type": "application/json",
    }
    response = requests.post(url, headers=headers, data=json.dumps({'entity_id': entity}))
    print("{}: {}".format(url, response.status_code))
    res = response.json()
    state = response.json()[0].get('state', 'off')
    sys.exit(state == 'on')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Call a HomeAssistant API command.')
    parser.add_argument('cmd', type=str, help='Command (state, toggle, on, off)')
    parser.add_argument('entity', type=str, help='Entity name ("light.my_light"')
    args = parser.parse_args()
    if args.cmd == 'state':
        get_state(args.entity)
    elif args.cmd == 'on':
        set_state(args.entity, 'turn_on', 1)
    elif args.cmd == 'off':
        set_state(args.entity, 'turn_off', 0)
    elif args.cmd == 'toggle':
        toggle_state(args.entity)
    else:
        sys.exit(0)
