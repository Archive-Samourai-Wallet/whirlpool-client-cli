# whirlpool-client-cli configuration

CLI is configured in `whirlpool-cli-config.properties` or with equivalent argument:
```
--cli.tor=true --cli.apiKey=foo...
```
Default configuration is [../src/main/resources/application.properties].  


#### Basic
| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| cli.server | TESTNET | Bitcoin network (TESTNET or MAINNET) |
| cli.apiKey | *generated on --init* | Secret key for using CLI API |
| cli.seed | *generated on --init* | Wallet seed encrypted with passphrase (AES) |
| cli.tor | false | Enable Tor |
| cli.dojo.enabled | false | Enable Dojo as wallet backend |
| cli.scode | - | SCODE for discount Whirlpool fees |
| cli.mix.autoMix | true | Automatically (re)mix premix & postmix. When disabled, each utxo must be mixed manually. |


#### Dojo

| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| cli.dojo.url | - | Dojo url |
| cli.dojo.apiKey | - | Dojo API key |


#### Logs

| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| logging.file | - | Enable external log (/tmp/whirlpool-cli.log) |

See advanced log settings (rotation, limits...):
https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging-file-output


#### Tor

Tor should be automatically detected, installed or configured.  
You can customize it for your needs:

| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| cli.torConfig.coordinator.enabled | true | Enable Tor for whirlpool coordinator (when cli.tor=true) |
| cli.torConfig.backend.enabled | true | Enable Tor for wallet backend (when cli.tor=true) |
| cli.torConfig.soroban.enabled | true | Enable Tor for Soroban (when cli.tor=true) |
| cli.torConfig.coordinator.onion | true | Use Tor hidden services (instead of clearnet over Tor) for whirlpool server |
| cli.torConfig.backend.onion | true | Use Tor hidden services (instead of clearnet over Tor) for wallet backend |
| cli.torConfig.soroban.onion | true | Use Tor hidden services (instead of clearnet over Tor) for Soroban |
| cli.torConfig.executable | auto | - `auto` : use embedded tor or detect a local Tor install when your system is not supported.<br/>- `local` : detect a local tor install<br/>- `/path/to/bin/tor` : use your own tor binary|
| cli.torConfig.customTorrc |  | `/path/to/torrc` : custom tor configuration to append to Torrc|
| cli.torConfig.fileCreationTimeout | 20 | Tor startup timeout (in seconds)|


#### CLI API
whirlpool-client-cli exposes a REST API over HTTPS when started with --listen (see [API.md](API.md)).  
It can be exposed over HTTP at your own risk.

| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| cli.api.port | 8899 | Port for CLI API over HTTPS (when started with --listen) |
| cli.api.http-enable | false | Enable unsecure CLI API over HTTP (not recommended, use it at your own risk!) |
| cli.api.http-port | 8898 | Port for unsecure CLI API over HTTP (when started with --listen and cli.api.http-enable=true) |


#### CLI API certificate
By default CLI API uses a self-signed certificate for HTTPS, which can be downloaded by opening https://CLI-HOST:8899/ with Firefox, then Advanced -> View certificate -> Download PEM.

You can configure your own cert:

| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| server.ssl.key-store | classpath:keystore/whirlpool.p12 | Path to your own keystore |
| server.ssl.key-store-type | PKCS12 | Keystore type: PKCS12 or JKS |
| server.ssl.key-store-password | whirlpool | Keystore password |
| server.ssl.key-alias | whirlpool | Alias in keystore |


#### Technical settings
| Setting | Default value | Description |
| ----------- | ----------- | ----------- |
| cli.proxy | - | Custom proxy to connect through. |
| cli.requestTimeout | 30000 | HTTP requests timeout |
| cli.mix.tx0MaxOutputs | 0 | Max premixs to create per TX0 (0 for max) |
| cli.mix.clients | 5 | Max simultaneous mixing clients.<br/>(Increasing this value won't make you mix faster.) |
| cli.mix.clientsPerPool | 1 | Max simultaneous mixing clients per pool.<br/>(Increasing this value won't make you mix faster.) |
| cli.mix.extraLiquidityClientsPerPool | 1 | Max additional mixing clients per pool for remixing POSTMIXS simultaneously while mixing threads are busy with PREMIXS.<br/>This may lead to more mixing threads than cli.mix.clientsPerPool, but cli.mix.clients will never be exceeded. |
| cli.mix.clientDelay | 15 | Connecting delay (seconds) between each mixing client |
| cli.mix.autoTx0Delay | 60 | Delay (seconds) between each auto-tx0 (when --auto-tx0) |
| cli.seedAppendPassphrase | *generated on --init* | Use passphrase as additional seed word (always true for wallets created with SW, may be false for external wallets imported into SW) |
| cli.partner | - | {SPARROW} |
| cli.version | *generated* | Technical setting for tracking CLI upgrades |
