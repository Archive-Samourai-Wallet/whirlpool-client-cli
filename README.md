[![Build Status](https://travis-ci.org/Samourai-Wallet/whirlpool-client-cli.svg?branch=develop)](https://travis-ci.org/Samourai-Wallet/whirlpool-client-cli)
[![](https://jitpack.io/v/Samourai-Wallet/whirlpool-client-cli.svg)](https://jitpack.io/#Samourai-Wallet/whirlpool-client-cli)

# whirlpool-client-cli

Command line client for [Whirlpool](https://github.com/Samourai-Wallet/Whirlpool) by Samourai-Wallet.


## General usage
```
java -jar target/whirlpool-client-version-run.jar --network={main,test} --server=host:port
[--ssl=true] [--tor=true] [--debug] [--pool=] [--test-mode]
[--rpc-client-url=http://user:password@host:port] {args...}
```

### Required arguments:
- network: (main,test) bitcoin network to use. Client will abort if server runs on a different network.
- server: (host:port) server to connect to

### Optional arguments:
- ssl: enable or disable SSL
- tor: enable or disable TOR
- debug: display more logs for debugging
- pool: id of the pool to join
- test-mode: disable tx0 checks, only available when enabled on server
- rpc-client-url: rpc url to connect to your own bitcoin node for broadcasting tx0 or aggregate transactions (warning: connection is not encrypted, use on trusted network only). If not provided, client will show rawtx and stop to let you broadcast it manually.

### List pools
```
--network={main,test} --server=host:port
```

Example:
```
java -jar target/whirlpool-client-version-run.jar --network=test --server=host:port
```

### Mix a wallet
You need a wallet holding funds to mix. The script will run the following automatic process:
1. List wallet utxos
2. When needed, split existing wallet utxo to pre-mix utxos with a valid tx0. Broadcast it (when rpc-client-url provided) or halt to let you broadcast it manually.
3. Mix pre-mix utxos, and repeat

```
--network={main,test} --server=host:port [--rpc-client-url=http://user:password@host:port] --pool=
[--clients=1] [--iteration-delay=0] [--client-delay=60] [--auto-aggregate-postmix] [--postmix-index=]
```

Example:
```
java -jar target/whirlpool-client-version-run.jar --network=test --server=host:port --pool=0.1btc --rpc-client-url=http://user:password@host:port
```
- clients: number of simultaneous clients to connect
- iteration-delay: delay (in seconds) to wait between mixs
- client-delay: delay (in seconds) between each client connexion
- auto-aggregate-postmix: enable automatically post-mix wallet agregation to refill premix when empty
- postmix-index: force postmix-index instead of reading it from local state. Use --postmix-index=0 to resync local state with API.

## Expert usage

### Mix specific utxo
You need a valid pre-mix utxo (output of a valid tx0) to mix.
```
--network={main,test} --server=host:port --pool=
--utxo= --utxo-key= --utxo-balance=
[--paynym-index=0]
[--mixs=1]
```

Example:
```
java -jar target/whirlpool-client-version-run.jar --network=test --server=host:port --pool=0.1btc --utxo=5369dfb71b36ed2b91ca43f388b869e617558165e4f8306b80857d88bdd624f2-3 --utxo-key=cN27hV14EEjmwVowfzoeZ9hUGwJDxspuT7N4bQDz651LKmqMUdVs --utxo-balance=100001000 --paynym-index=5
```
- utxo: (txid:ouput-index) pre-mix input to spend (obtained from a valid tx0)
- utxo-key: ECKey for pre-mix input
- utxo-balance: pre-mix input balance (in satoshis). Whole utxo-balance balance will be spent.
- paynym-index: paynym index to use for computing post-mix address to receive the funds
- mixs: (1 to N) number of mixes to complete. Client will keep running until completing this number of mixes.


### Tx0
You need a wallet holding funds to split.
```
--network={main,test} --server=host:port [--rpc-client-url=http://user:password@host:port] --pool=
--tx0=
[--rpc-client-url=http://user:password@host:port]
```

Example:
```
java -jar target/whirlpool-client-version-run.jar --network=test --server=host:port --pool=0.1btc --tx0=10 --rpc-client-url=http://user:password@host:port
```
- tx0: number of pre-mix utxo to generate

### Aggregate postmix / move funds
Move all postmix funds back to premix wallet and consolidate to a single UTXO.
Only allowed on testnet for testing purpose.
```
--network={main,test} --server=host:port [--rpc-client-url=http://user:password@host:port] --pool=
--aggregate-postmix[=address]
```

Example:
```
java -jar target/whirlpool-client-version-run.jar --network=test --server=host:port --pool=0.1btc --aggregate-postmix --rpc-client-url=http://user:password@host:port
```
- aggregate-postmix: move funds back to premix-wallet. Or --aggregate-postmix=address to move funds to a specific address.

## Build instructions
Build with maven:

```
cd whirlpool-client-cli
mvn clean install -Dmaven.test.skip=true
```

## Resources
 * [whirlpool](https://github.com/Samourai-Wallet/Whirlpool)
 * [whirlpool-protocol](https://github.com/Samourai-Wallet/whirlpool-protocol)
 * [whirlpool-client](https://github.com/Samourai-Wallet/whirlpool-client)
 * [whirlpool-server](https://github.com/Samourai-Wallet/whirlpool-server)
