[![](https://jitpack.io/v/io.samourai.code.whirlpool/whirlpool-client-cli.svg)](https://jitpack.io/#io.samourai.code.whirlpool/whirlpool-client-cli)

# whirlpool-client-cli

Command line client for [Whirlpool](https://code.samourai.io/whirlpool/whirlpool) by Samourai-Wallet.

## Getting started

#### Download and verify CLI
- Download whirlpool-client-cli-\[version\]-run.jar from [releases](https://code.samourai.io/whirlpool/whirlpool-client-cli/-/releases)
- Verify sha256 hash of the jar with signed message in whirlpool-client-cli-\[version\]-run.jar.sig
- Verify signature with [@SamouraiDev](https://github.com/SamouraiDev) 's key

#### Initial setup
You can setup whirlpool-client-cli in 2 ways:
- command-line: run CLI with ```--init```
- remotely: run CLI with ```--listen```, then use GUI or API

#### Run
```
java -jar target/whirlpool-client-version-run.jar
```

- [doc/USAGE.md](doc/USAGE.md) for CLI usage.
- [doc/API.md](doc/API.md) to manage CLI remotely. 
- [doc/CONFIG.md](doc/CONFIG.md) for advanced usage, integration and development.
- [doc/DEV.md](doc/DEV.md) for developers. 


## Resources
 * [whirlpool](https://code.samourai.io/whirlpool/Whirlpool)
 * [whirlpool-protocol](https://code.samourai.io/whirlpool/whirlpool-protocol)
 * [whirlpool-client](https://code.samourai.io/whirlpool/whirlpool-client)
 * [whirlpool-server](https://code.samourai.io/whirlpool/whirlpool-server)

