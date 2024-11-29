del "src\olang.bundle.jar"
del /q "src\out"
java -jar ../build/libs/CompilerOOP-1.0-SNAPSHOT.jar src/%1.olang -bundle > nul 2>&1
java -jar ../build/libs/CompilerOOP-1.0-SNAPSHOT.jar src/%1.olang -bundle -jar
ren "src\%1.olang.bundle.jar" olang.bundle.jar
