# whirlpool-client-cli usage

#### Basic

| Argument | Description |
| ---------|------------ |
| --init | Initialize CLI (by setting wallet seed and generating API key) |
| --authenticate | Enable interactive authentication on startup |
| --listen | Enable CLI API for remote commands & GUI (see [API.md](API.md))|
| --list-pools | List pools and exit|

#### Advanced

| Argument | Description |
| ---------|------------ |
| --dump-payload | Dump pairing-payload of current wallet and exit |
| --resync | Resynchronize mix counters on startup (startup will be slower) |


#### Debugging

| Argument | Description |
| -----------------|-------------- |
| --debug | Enable debug logs for CLI |
| --debug-client | Enable debug logs for whirlpool-client |

Any problem with a remote CLI? Test it locally:
- Configure CLI manually: ```java -jar whirlpool-client-cli-xxx-run.jar --debug --init```
- Then start it with manual authentication: ```java -jar whirlpool-client-cli-xxx-run.jar --debug --authenticate```


#### Authenticate on startup
You can authenticate in several ways:

For security reasons, you should not store your passphrase anywhere. If you really need to automate authentication process, use this at your own risk:
```
export PP="mypassphrase"
echo $PP|java -jar whirlpool-client-cli-x-run.jar --authenticate
```
