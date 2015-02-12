To compile and test:
```sh
cat <<EOF > target/run-test.sh
#!/bin/sh
VAL=0
while true; do
  cat <<EOF
{ "95934": null, "96031": ${VAL},  "96147": 0, "96302": null }
EOF
  VAL=$(((${VAL}+1)%256))
  sleep 1s
done
EOF
chmod +x target/run-tests.sh
./target/run-tests.sh &

mvn package &&
mvn exec:java -Dexec.mainClass="com.github.cambridgeAlphaTeam.Main" \
              -Dexec.args="target/run-test.sh"
```

To just compile, do `mvn package`.

You probably want to create a CubeletConnection (or rather, an
implementing class instance) and call getCubeletValues() on it.
See the com.github.cambridgeAlphaTeam.Main class.