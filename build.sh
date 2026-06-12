rm -rf build
mkdir build

find src -name "*.java" > sources.txt

javac \
-cp lib/servlet-api.jar \
-d build \
@sources.txt

rm sources.txt
jar -cvf Framework.jar -C build .