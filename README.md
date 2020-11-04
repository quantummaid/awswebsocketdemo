# AWS Websockets Demo

## Deploy server to AWS (create & update)

```bash
mvn clean package
export STACK_IDENTIFIER="awswebsocketdemo"
./deploy.sh
```

## Interact with the server


Use pre-deployed instance:

```bash
. ./scripts/endpoints.sh
```

**or** configure custom deployment:

```bash
export HTTP_ENDPOINT="https://XXXXXXXXX.execute-api.us-east-1.amazonaws.com/"
export WEBSOCKET_ENDPOINT="wss://XXXXXXXX.execute-api.us-east-1.amazonaws.com/awswebsocketdemo"
export CLIENT_ID="testclient"
```


### Connect CLI client

```bash
./democlient.sh $WEBSOCKET_ENDPOINT $CLIENT_ID 
```

### Trigger fake event
```bash
./scripts/trigger_event.sh
```

### Disconnect all clients
```bash
./scripts/disconnect_clients.sh
```
