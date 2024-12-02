# Compiler for O-lang

## Requirements:
* `Java 17` or higher

## Usage

1. Build JAR _(output location is `./build/libs/CompilerOOP-{version}.jar`)_
```shell
./gradlew build
```

2. Run CLI
```shell
java -jar CompilerOOP-{version}.jar <file_path> [options]
```

Available options _(can be used together)_:
* `-bundle` — compiles not only the provided file, but also the libraries
* `-jar` — wrap the output in a `.jar` file

## Pre-defined scripts

There are some scripts in `test` directory that will help you understand how to use this compiler. To use them,
use this workflow:

1. Add `.olang` files to `test/src`

2. Compile
```shell
compile.sh <file_name>.olang
```

3. Run _(will work only if there is class `Program` with empty constructor and method `run` without arguments)_ 
```shell
run.sh <file_name>.olang
```
