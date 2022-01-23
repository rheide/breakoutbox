import sys
import traceback
import requests
import json
import argparse
from urllib.parse import quote_plus

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Fetch crypto prices and output a normalized value.')
    parser.add_argument('pair', type=quote_plus, help='Currency pair, e.g. "XBTUSD" - see https://docs.kraken.com/rest/#operation/getTradableAssetPairs')
    parser.add_argument('min', type=int, help='Minimum value (exit code 0)"')
    parser.add_argument('max', type=int, help='Maximum value (exit code 15)"')
    args = parser.parse_args()

    url = f"https://api.kraken.com/0/public/Ticker?pair={args.pair}"
    try:
        res = requests.get(url).json()
        key = list(res['result'].keys())[0]
        price = float(res['result'][key]["a"][0])
        normalized = (price - args.min) / (args.max - args.min) * 15
        print(f"say {args.pair} price is: {price}. Normalized: {normalized}")
        normalized = max(min(15, int(normalized)), 0)
        sys.exit(int(normalized))
    except Exception as e:
        traceback.print_exc()
    sys.exit(0)
