# whirlpool-client-cli API


## Using CLI API
whirlpool-client-cli exposes a REST API when started with --listen (see [CONFIG.md](CONFIG.md) for configuration).  
It can be used by whirlpool-gui or any REST client.


#### Required headers
* apiVersion (see [CliApi.java](src/main/java/com/samourai/whirlpool/cli/api/protocol/CliApi.java))
* apiKey (configured in ```whirlpool-cli-config.properties```)


## Pools

### List pools: ```GET /rest/pools```
Parameters:
* (optional) tx0FeeTarget: tx0 fee target for tx0BalanceMin computation
* (optional) mixFeeTarget: mix fee target for tx0BalanceMin computation

Response:
```
{
    "pools":[
        {
            "poolId":"0.1btc",
            "denomination":10000000,
            "feeValue":5000000,
            "mustMixBalanceMin":10000102,
            "mustMixBalanceMax":10010000,
            "minAnonymitySet":5,
            "tx0MaxOutputs":70,
            "nbRegistered":0,
            "mixAnonymitySet":5,
            "mixStatus":"CONFIRM_INPUT",
            "elapsedTime":22850502,
            "nbConfirmed":0,
            "tx0BalanceMin":10020005
        }
    ]
}
```

## Wallet

### Deposit: ```GET /rest/wallet/deposit[?increment=false]```
Parameters:
* (optional) Use increment=true make sure this address won't be reused.

Response:
```
{
    depositAddress: "tb1qjxzp9z2ax8mg9820dvwasy2qtle4v2q6s0cant"
}
```

## Global mix control

### Mix state: ```GET /rest/mix```
Response:
```
{

    "started":true,
    "nbMixing":1,
    "nbQueued":17,
    "threads":[{
        "hash":"c7f456d5ff002faa89dadec01cc5eb98bb00fdefb92031890324ec127f9d1541",
        "index":5,
        "value":1000121,
        "confirmations":95,
        "path":"M/0/166",
        "account":"PREMIX",
        "status":"MIX_STARTED",
        "mixableStatus":"MIXABLE",
        "progressPercent":"10",
        "progressLabel":"CONNECTING",
        "poolId":"0.01btc",
        "priority":5,
        "mixsDone":0,
        "message":" - [MIX 1/1] ▮▮▮▮▮▯▯▯▯▯ (5/10) CONFIRMED_INPUT : joined a mix!",
        "error":null,
        "lastActivityElapsed": 23001
    }]
}
```

### Start mixing: ```POST /rest/mix/start```

### Stop mixing: ```POST /rest/mix/stop```

## UTXO controls

### List utxos: ```GET /rest/utxos```
Parameters:
* refresh (optional): "true" to refresh utxos

Response:
```
{
    deposit: {
        utxos: [(utxos detail)],
        balance: 0,
        zpub: ""
    },
    premix: {
        utxos: [(utxos detail)],
        balance: 0,
        zpub: ""
    },
    postmix: {
        utxos: [(utxos detail)],
        balance: 0,
        zpub: ""
    },
    balance: 0,
    lastUpdate: 0
}
```

### Tx0 preview ```POST /rest/tx0/preview```
Payload:
* inputs {hash, index} (mandatory): utxos to spend for tx0
* tx0FeeTarget (mandatory): fee target for tx0
* mixFeeTarget (mandatory): fee target for mix
* poolId (optional): override utxo's poolId
```
{
    inputs: [
        {hash:"c7f456d5ff002faa89dadec01cc5eb98bb00fdefb92031890324ec127f9d1541", index:5}
    ],
    tx0FeeTarget: "BLOCKS_4",
    mixFeeTarget: "BLOCKS_6",
    poolId: "0.01btc"
}
```


Response:
```
{
    
}
```

### Tx0 ```POST /rest/tx0```
Payload:
* inputs {hash, index} (mandatory): utxos to spend for tx0
* tx0FeeTarget (mandatory): fee target for tx0
* mixFeeTarget (mandatory): fee target for mix
* poolId (optional): override utxo's poolId
```
{
    inputs: [
        {hash:"c7f456d5ff002faa89dadec01cc5eb98bb00fdefb92031890324ec127f9d1541", index:5}
    ],
    tx0FeeTarget: "BLOCKS_4",
    mixFeeTarget: "BLOCKS_6",
    poolId: "0.01btc"
}
```


Response:
```
{
    "txid":"aa079c0323349f4abf3fb793bf2ed1ce1e11c53cd22aeced3554872033bfa722"
}
```

### Start mixing UTXO: ```POST /rest/utxos/{hash}:{index}/startMix```
Parameters:
* hash,index: utxo to mix.

### Stop mixing UTXO: ```POST /rest/utxos/{hash}:{index}/stopMix```
Parameters:
* hash,index: utxo to stop mixing.


## CLI

### CLI state: ```GET /rest/cli```
Response:
```
{
    "cliStatus": "READY",
    "loggedIn": true,
    "torProgress": 100,
    "cliMessage": "",
    "network": "test",
    "serverUrl": "",
    "serverName": "TESTNET",
    "dojoUrl": "",
    "tor": true,
    "dojo": true
}
```

### login: ```POST /rest/cli/login```
Payload:
* seedPassphrase: passphrase of configured wallet
```
{
    seedPassphrase: "..."
}
```

Response:
```
{
    "cliStatus": "READY",
    "cliMessage": "",
    "loggedIn": true
}
```

### logout: ```POST /rest/cli/logout```
Response:
```
{
    "cliStatus": "READY",
    "cliMessage": "",
    "loggedIn": false
}
```

### initialize: ```POST /rest/cli/init```
Payload:
* pairingPayload: pairing payload from Samourai Wallet
* tor: enable Tor
* dojo: enable Dojo (use null to auto-detect from pairingPayload)
```
{
    pairingPayload: "...",
    tor: true,
    dojo: true
}
```

### restart: ```POST /rest/cli/restart```

### get config: ```GET /rest/cli/config```

### set config: ```PUT /rest/cli/config```

### reset config: ```DELETE /rest/cli/config```

### resync mix counters: ```POST /rest/cli/resync```

