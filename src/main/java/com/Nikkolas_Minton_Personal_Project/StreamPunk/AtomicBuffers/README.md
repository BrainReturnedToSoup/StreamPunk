    Stores a bunch of helper classes that aren't necessarily 
    'buffers' in the traditional sense, but the child threads and the event loop
    instances will use these mediums for communication. 
    
    The gimmick in these cases, is that the means of communication is via
    common atomic variable declarations. However, these atomic vars are shared references
    encapsulated in thread-scope-specific instantiations, so no need to worry about race 
    conditions on the class wrappers themselves.
    
        thisPackage.AtomicVarIoC
    
            - The purpose of this package is that it acts as simple IoC container for
              holding specific atomic var references so that they can be retrieved on
              an ID that both the event loop and the child threads may know, which this IoC
              container can be made to handle the race condition since it will use a threadsafe
              hashmap.