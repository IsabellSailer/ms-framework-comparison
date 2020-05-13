

## Install moleculer



``` 
$ npm i moleculer --save
```

### for treatments service

```
npm install moleculer-db moleculer-db-adapter-mongo --save
```

```
export SERVICES=treatments,api && npm start
```


### for appointments service

```
 npm install moleculer-db-adapter-sequelize sequelize --save
 npm install pg pg-hstore --save
```

```
export SERVICES=appointments,api && npm start
```

### for confirmation service

```
export SERVICES=confirmation && npm start
```