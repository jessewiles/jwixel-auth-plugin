# jwixel-auth-plugin

This project produces a Elasitcsearch authentication plugin.

## Quickstart

Build and run the docker container.  It's very simple and is mostly intended as a 
starting point for building a custom Elasticsearch authentication plugin.

```bash
# in one terminal:
$ make stop ; make run
...
[2019-10-28T12:44:39,960][INFO ][o.e.p.PluginsService     ] [gYxtrWG] loaded plugin [jwixel-auth-plugin]
[2019-10-28T12:44:48,165][INFO ][o.e.d.DiscoveryModule    ] [gYxtrWG] using discovery type [zen] and host providers [settings]
[2019-10-28T12:44:49,437][INFO ][o.e.n.Node               ] [gYxtrWG] initialized
[2019-10-28T12:44:49,438][INFO ][o.e.n.Node               ] [gYxtrWG] starting ...
[2019-10-28T12:44:50,102][INFO ][o.e.t.TransportService   ] [gYxtrWG] publish_address {172.17.0.2:9300}, bound_addresses {0.0.0.0:9300}
[2019-10-28T12:44:50,137][INFO ][o.e.b.BootstrapChecks    ] [gYxtrWG] bound or publishing to a non-loopback address, enforcing bootstrap checks
[2019-10-28T12:44:53,325][INFO ][o.e.c.s.MasterService    ] [gYxtrWG] zen-disco-elected-as-master ([0] nodes joined), reason: new_master {gYxtrWG}{gYxtrWGgQNeR4YE5Duh9hg}{B1bMDxbHSHehTRyawgw_cA}{172.17.0.2}{172.17.0.2:9300}
[2019-10-28T12:44:53,339][INFO ][o.e.c.s.ClusterApplierService] [gYxtrWG] new_master {gYxtrWG}{gYxtrWGgQNeR4YE5Duh9hg}{B1bMDxbHSHehTRyawgw_cA}{172.17.0.2}{172.17.0.2:9300}, reason: apply cluster state (from master [master {gYxtrWG}{gYxtrWGgQNeR4YE5Duh9hg}{B1bMDxbHSHehTRyawgw_cA}{172.17.0.2}{172.17.0.2:9300} committed version [1] source [zen-disco-elected-as-master ([0] nodes joined)]])
[2019-10-28T12:44:53,406][INFO ][o.e.h.n.Netty4HttpServerTransport] [gYxtrWG] publish_address {172.17.0.2:9200}, bound_addresses {0.0.0.0:9200}
[2019-10-28T12:44:53,407][INFO ][o.e.n.Node               ] [gYxtrWG] started
...

-----

# in another terminal:
$ curl -u jwixeladmin:let-me-pass localhost:9200
{
  "name" : "gYxtrWG",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "nutkz6daSlyed-hz1bvEPg",
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
$ curl -u billy:will localhost:9200
Not authorized%
```

Initial values for user:pass and auth token are hard-coded into docker file.  Ultimately, this will be pushed into config which can be modified using a web UI.

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
