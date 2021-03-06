# AtomicFU

The idiomatic way to use atomic operations in Kotlin. 

* Code it like `AtomicReference/Int/Long`, but run it in production like `AtomicReference/Int/LongFieldUpdater`. 
* Use Kotlin-specific extensions (e.g. inline `updateAndGet` and `getAndUpdate` functions).
* Compile-time dependency only (no runtime dependencies).
* Post-compilation bytecode transformer that declares all the relevant field updaters for you. 

## Example

Let us declare a `top` variable for a lock-free stack implementation:

```kotlin
import kotlinx.atomicfu.atomic // import top-level atomic function from kotlinx.atomicfu

private val top = atomic<Node?>(null) // must be declared as private val with initializer
```

Use `top.value` to perform volatile reads and writes:

```kotlin
fun isEmpty() = top.value == null  // volatile read
fun clear() { top.value = null }   // volatile write
``` 

Use `compareAndSet` function directly:

```kotlin
if (top.compareAndSet(expect, update)) ... 
```

Use higher-level looping primitives (inline extensions), for example:

```kotlin
top.loop { cur ->   // while(true) loop that volatile-reads current value 
   ...
}
```

Use high-level `update`, `updateAndGet`, and `getAndUpdate`, 
when possible, for idiomatic lock-free code, for example:

```kotlin
fun push(v: Value) = top.update { cur -> Node(v, cur) }
fun pop(): Value? = top.getAndUpdate { cur -> cur?.next } ?.value
```

Declare atomic integers and longs using type inference:

```kotlin
val myInt = atomic(0)    // note: integer initial value
val myLong = atomic(0L)  // note: long initial value   
```

Integer and long atomics provide all the usual `getAndIncrement`, `incrementAndGet`, `getAndAdd`, `addAndGet`, and etc
operations. They can be also atomically modified via `+=` and `-=` operators.

## Dos and Don'ts

* Always declare atomic variables as `private val`.
* Do not leak references on atomic variable themselves. 
* Only simple operations on atomic variables _directly_ are supported.
* Do not introduce complex data flow in parameters to atomic variable operations, 
  i.e. `top.value = complex_expression` and `top.compareAndSet(cur, complex_expression)` are not supported 
  (more specifically, `complex_expression` should not have branches in its compiled representation).
* Use the following pattern if you need to expose the value of atomic property to the public:

```kotlin
private val _foo = atomic<T>(initial)  // private atomic, name it with leading underscore
public var foo: T                      // public val/var
    get() = _foo.value
    set(value) { _foo.value = value }
```  
   

## Maven build setup

Declare AtomicFU version:

```xml
<properties>
     <atomicfu.version>0.1</atomicfu.version>
</properties> 
```

Add Bintray JCenter repository:

```xml
<repositories>
    <repository>
        <id>central</id>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```

Declare _provided_ dependency on the AtomicFU library 
(the users of the resulting artifact will not have a dependency on AtomicFU library):

```xml
<dependencies>
    <dependency>
        <groupId>org.jetbrains.kotlinx</groupId>
        <artifactId>atomicfu</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Configure build steps:

```xml
<build>
    <plugins>
        <!-- compile Kotlin files to staging directory -->
        <plugin>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-plugin</artifactId>
            <version>${kotlin.version}</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                    <configuration>
                        <output>${project.build.directory}/classes-atomicfu</output>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <!-- transform classes with AtomicFU plugin -->
        <plugin>
            <groupId>org.jetbrains.kotlinx</groupId>
            <artifactId>atomicfu-maven-plugin</artifactId>
            <version>${atomicfu.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>transform</goal>
                    </goals>
                    <configuration>
                        <input>${project.build.directory}/classes-atomicfu</input>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```






