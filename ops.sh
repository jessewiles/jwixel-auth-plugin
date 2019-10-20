if [ "$1" = "run" ]; then
	mvn clean package ;\
	docker build -t jwixel-io/jwixel-auth-plugin:latest . ;\
	docker run -d --name jwixel-auth-test -p 9200:9200 jwixel-io/jwixel-auth-plugin:latest ;\
	sleep 1;\
	docker logs jwixel-auth-test -f
elif [ "$1" = "stop" ]; then
	for item in `docker ps -a | grep jwix | awk '{print $1}'`; do
		docker kill $item; docker rm $item
	done
else
    echo "Usage: ops.sh [run|stop]"
fi
