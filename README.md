# jwixel-auth-plugin

This project produces a Elasitcsearch authentication plugin.

## Quickstart

Build and run the docker container.  It's very simple and is mostly intended as a 
starting point for building a custom Elasticsearch authentication plugin.

```bash
$ mvn clean package

$ docker build jwixel-io/jwixel-auth-plugin:0.1 .

$ docker run -d --name jwixel-auth-test -p 9200:9200 jwixel-io/jwixel-auth-plugin:0.1

$ curl localhost:9200
Not authorized%

$ curl -u billy:will localhost:9200
Not authorized%

$ curl -u jwixel:pass! localhost:9200
{
  "name" : "7mDgSTs",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "v1G4M-qFTbWuED_hZ8__Vw",
  "version" : {
    "number" : "6.6.2",
    "build_flavor" : "oss",
    "build_type" : "tar",
    "build_hash" : "3bd3e59",
    "build_date" : "2019-03-06T15:16:26.864148Z",
    "build_snapshot" : false,
    "lucene_version" : "7.6.0",
    "minimum_wire_compatibility_version" : "5.6.0",
    "minimum_index_compatibility_version" : "5.0.0"
  },
  "tagline" : "You Know, for Search"
}

$ docker logs jwixel-auth-test --tail 5
[2019-10-11T11:56:01,670][INFO ][o.e.n.Node               ] [0_YCYd2] started
[2019-10-11T11:56:01,696][INFO ][o.e.g.GatewayService     ] [0_YCYd2] recovered [0] indices into cluster_state
[2019-10-11T11:56:36,779][WARN ][i.j.e.a.BasicAuthHandler ] [0_YCYd2] No auth header...
[2019-10-11T11:57:04,143][WARN ][i.j.e.a.BasicAuthHandler ] [0_YCYd2] Unknown user: billy. Failing auth...
[2019-10-11T11:57:17,579][WARN ][i.j.e.a.BasicAuthHandler ] [0_YCYd2] You're a jwixel; do what you want...
```

The logic for handling requests is encapsulated here:
https://github.com/jwixel-io/jwixel-auth-plugin/blob/master/src/main/java/io/jwixel/esplugins/auth/BasicAuthHandler.java#L22-L58

## Building

```
# To build a package
$ mvn clean package
```

## Testing

```
# To run tests
$ mvn test
```
