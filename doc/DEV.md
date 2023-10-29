# whirlpool-client-cli for developers

## Build instructions
Build with maven:

```
cd whirlpool-client-cli
mvn clean install -Dmaven.test.skip=true
```


#### Testing loop for testnet
You can run CLI in loop mode on testnet to generate liquidity on testnet server:
- automatically run TX0 while possible
- mix while possible
- consolidate wallet when PREMIX is empty and start again
```
--clients=5 --tx0-max-outputs=15 --auto-tx0=0.01btc --auto-tx0-aggregate --scode=
```

Adjust mixing rate with ```cli.mix.clientDelay = 60```
Generate simultaneous liquidity with ```cli.mix.clientsPerPool = 5```


#### Reading input from console redirection
It's possible to run CLI programmatically and read keyboard input from console redirection.  
```
echo -e "input1\nline2\nline3"|java -jar target/whirlpool-client-cli.jar
```
Example for initializing CLI programmatically:
```
echo -e "{{pairingPayload...}}\ny"|java -jar target/whirlpool-client-cli.jar --init
```
