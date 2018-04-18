# How to copy a deployment from an existing setup

Store the existing infrastructure:
```
cp ~/.grails/food4me-webservice.config .
pg_dump -h localhost -u food4me | gzip > food4me.sql.gz
```

Create postgres config user
```
createuser -P food4me
createdb food4me
zcat food4me.sql.gz | psql food4me
```

Initialize Grails config
```
mkdir -f ~/.grails
cp food4me-webservice.config ~/.grails
```
