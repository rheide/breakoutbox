# Silly script that simulates buying and selling crypto
import os
import sys
import traceback
import requests
import json
import argparse
import hashlib
from urllib.parse import quote_plus

START_BALANCE_USD = 100000

PAIRS = {
    'XBTUSD': ('XBT', 'USD'),
    
}

def _wallet_name(target_name):
    return "wallet_{}.json".format(hashlib.sha256(target_name.encode('utf8')).hexdigest())


def get_rate(pair):
    url = f"https://api.kraken.com/0/public/Ticker?pair={args.pair}"
    try:
        res = requests.get(url).json()
        key = list(res['result'].keys())[0]
        ask = float(res['result'][key]["a"][0])
        bid = float(res['result'][key]["b"][0])
        return bid, ask
    except Exception as e:
        print("say Failed to get balance for pair")
        sys.exit(0)


def load_wallet(target_name):
    wallet_fn = _wallet_name(target_name)
    if not os.path.exists(wallet_fn):
         return {'USD': START_BALANCE_USD}
    wallet = {}
    try:
        with open(wallet_fn) as f:
            return json.load(f)
    except Exception as e:
        print(f"say could not wallet for {target_name}")
        sys.exit(0)


def save_wallet(target_name, wallet):
    with open(_wallet_name(target_name), 'w+') as f:
        f.write(json.dumps(wallet))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Fetch crypto prices and output a normalized value.')
    parser.add_argument('target', type=str, help='Target entity')
    parser.add_argument('command', type=str, help='"buy", "sell", "view" to view balance')
    parser.add_argument('--pair', type=quote_plus, nargs='?', help='Currency pair to trade, e.g. "XBTUSD" - see https://docs.kraken.com/rest/#operation/getTradableAssetPairs')
    parser.add_argument('--currency', type=str, nargs='?', help='Currency to view balance of, e.g. "XBT" or "USD"')
    parser.add_argument('--amount', type=str, nargs='?', help='Amount to buy/sell, or "all" for all, in the originating currency.')
    parser.add_argument('--max', type=str, nargs='?', help='For "view" this is the range to use to transform the amount into a redstone signal.')
    args = parser.parse_args()

    target_name = args.target.split(',')[0]
    if not target_name:
        #print("No valid target")
        sys.exit(0)

    wallet = load_wallet(target_name)

    if args.command == "view":
        max_amount = float(args.max or 1)
        actual_amount = wallet.get(args.currency, 0)
        normalized_amount = max(min((actual_amount / max_amount) * 15.0, 15), 0)
        print(f"say {target_name} balance: {actual_amount:.4f} {args.currency} ({normalized_amount:.0f})")
        sys.exit(int(normalized_amount))

    if args.pair not in PAIRS:
        print("say Unrecognized pair")
        sys.exit(0)

    bid, ask = get_rate(args.pair)
    if args.command == "buy":
        to_cur, from_cur = PAIRS[args.pair]
        price = ask
    elif args.command =="sell":
        from_cur, to_cur = PAIRS[args.pair]
        price = 1.0 / bid
    else:
        print("say unsupported command")
        sys.exit(0)

    # XBTUSD: buy args.amount of USD and turn into XBT
    wallet_amount = wallet.get(from_cur, 0)
    requested_amount = wallet_amount if args.amount == 'all' else float(args.amount)
    if requested_amount > wallet_amount or wallet_amount == 0:
        print(f"say {target_name} does not have enough funds! ({from_cur})")
        sys.exit(1)
    amount_bought = float(requested_amount) / price

    wallet[from_cur] = wallet_amount - requested_amount
    wallet[to_cur] = wallet.get(to_cur, 0) + amount_bought
    save_wallet(target_name, wallet)

    new_bal = "[{}:{:.2f},{}:{:.2f}]".format(to_cur, wallet.get(to_cur, 0), from_cur, wallet.get(from_cur, 0))
    if args.command == "buy":
        print(f"say {target_name} {args.command} {amount_bought:.2f} {to_cur} for {requested_amount:.2f} {from_cur} @ {price:.2f} | {new_bal}")
    elif args.command == "sell":
        price_disp = bid
        print(f"say {target_name} {args.command} {requested_amount:.2f} {from_cur} @ {price_disp:.2f} for {amount_bought:.2f} {to_cur} | {new_bal}")
    sys.exit(2)

