rm -f "src/olang.bundle.jar"
rm -rf "src/out"
java -jar ../build/libs/CompilerOOP-1.0-SNAPSHOT.jar "src/$1.olang" -bundle >/dev/null 2>&1
java -jar ../build/libs/CompilerOOP-1.0-SNAPSHOT.jar "src/$1.olang" -bundle -jar
mv "src/$1.olang.bundle.jar" olang.bundle.jar
