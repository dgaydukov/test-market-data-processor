### Design

* I used approach of conflating queue (so for each instrument only latest update is shown)
* I used 2 jdk classes to implement such a queue (cause there is no conflating queue in jdk)
* map is used as conflator, which ensures that we always get latest update
* Both classes are lock-free & multi-threaded. So you can call `onMessage` from one thread, and populate the queue & map, and
start publisher in another thread. Since we are using lock-free constructs, we don't waste precious time for locks, and since 
both classes do support multi-threading, we don't lose any data & always get latest updates from one thread in another.