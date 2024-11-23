package com.Nikkolas_Minton_Personal_Project.StreamPunk.AtomicBuffers.AtomicVarIoC;

import com.Nikkolas_Minton_Personal_Project.StreamPunk.AtomicBuffers.AtomicVarIoC.Errors.AtomicVarContainer_ConcurrencyException;
import com.Nikkolas_Minton_Personal_Project.StreamPunk.AtomicBuffers.AtomicVarIoC.Errors.AtomicVarContainer_DoesNotExistException;
import com.Nikkolas_Minton_Personal_Project.StreamPunk.AtomicBuffers.AtomicVarIoC.Errors.AtomicVarContainer_ShouldNotExistException;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;



// generally meant to be the intermediary between individual child threads
// and the event loop thread when considering passing atomic vars between
// the two. Makes it easy to just pass the class reference itself as
// a constructor dependency where I need it, and generally track how
// atomic vars are allocated and potentially deallocated for whatever reason.

// this works as an instantiation rather than as something static, because ultimately
// the truly concurrent portions are handled as per the concurrent hash map at the top
// level, along with a logger that is concurrency-safe.
public class AtomicVarContainer implements AtomicVarContainerInterface {

    /*

    -This container should be accessed with static members, as to allow no problem in access via the methods themselves.

    -Each potentially concurrently accessed member has a 'concurrentAccessCounter' as well as a 'pendingStateChange'
     flag, so that future 'gets' for a specific resource do not cause contention for the setter.

    * */

    /*
        -ioc container

        -outermost hash map stores:
            [Child Process UUID]: ConcurrentHashMap<String, AtomicReference<?>>()

        -within each of these inner concurrent hash maps follows:
            [atomic var static ID]: Atomic variable reference;

        -this IoC container overall makes it easier to declare the specific atomic vars first, create the
         concurrent hash map with the vars, and then link them to a specific process ID.

        -this ultimately allows the event loop to use an initial 'child thread initializer' that returns the UUID
         for that child thread *NOT A PROCESS*.

        -The event loop will take the returned UUIDs, assuming that the child threads allocated what was needed already.

        -This mechanism is also important overall, since it allows the event loop to dynamically reallocate child threads
         in the middle of the runtime.
     */

    // 'volatile' keyword ensures that any operation on the top level reference stored in the variable member 'iocContainer'
    // is pulled from L3 cache or higher. This should be manageable since the nature
    // of this container is mainly for instantiation of child threads and passing important information between
    // the event loop and new child threads.
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicReference<?>>>
            iocContainer;

    // logger (still need to figure out how that API will look when invoked concurrent-safe within these static methods)
    private Reference<?> logger;

    // mainly for testing since this is otherwise static.
    // I can't really make it *not static* since these APIs are shared between threads
    // unless I use a complex wrapper class but, I don't feel like doing allat.

    public AtomicVarContainer(
            ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicReference<?>>> container,
            Reference<?> logger
    ) {
        this.iocContainer = container;
        this.logger = logger;
    }

    public boolean checkChildThreadExists(
            String threadInstanceUUID
    ) {
        return this.iocContainer.contains(threadInstanceUUID);
    }

    public void addChildThread(
            String threadInstanceUUID
    ) {
        if (this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, do not overwrite if true
            throw new AtomicVarContainer_ShouldNotExistException("");
        }

        this.iocContainer.put(threadInstanceUUID, new ConcurrentHashMap<String, AtomicReference<?>>());
    }
    public void deleteChildThread(
            String threadInstanceUUID
    ) {
        if (!this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        this.iocContainer.remove(threadInstanceUUID);
    }

    public void addAtomicVarToChildThread(
            String threadInstanceUUID,
            String atomicVarId,
            AtomicReference<?> atomicReference
    ) {
        if (!this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        ConcurrentHashMap<String, AtomicReference<?>> atomicVars = this.iocContainer.getOrDefault(threadInstanceUUID, null);

        if (atomicVars == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'iocContainer'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        if (atomicVars.contains(atomicVarId)) {
            // throw an error, it shouldn't exist
            throw new AtomicVarContainer_ShouldNotExistException("");
        }

        atomicVars.put(atomicVarId, atomicReference);
    }

    public void deleteAtomicVarFromChildThread(
        String threadInstanceUUID,
        String atomicVarId
    ) {
        if (!this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        ConcurrentHashMap<String, AtomicReference<?>> atomicVars = this.iocContainer.getOrDefault(threadInstanceUUID, null);

        if (atomicVars == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'iocContainer'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        if (!atomicVars.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        AtomicReference<?> var = atomicVars.getOrDefault(atomicVarId, null);

        if (var == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'atomicVars'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        atomicVars.remove(atomicVarId); // no need to re-save. fetching from a hash map is a reference anyway.
    }

    public AtomicReference<?> getAtomicVarFromChildThread(
            String threadInstanceUUID,
            String atomicVarId
    ) {
        if (!this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        ConcurrentHashMap<String, AtomicReference<?>> atomicVars = this.iocContainer.getOrDefault(threadInstanceUUID, null);

        if (atomicVars == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'iocContainer'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        if (!atomicVars.contains(atomicVarId)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        AtomicReference<?> var = atomicVars.getOrDefault(atomicVarId, null);

        if (var == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'atomicVars'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        return var;
    }

    public boolean checkAtomicVarForChildThreadExists(
            String threadInstanceUUID,
            String atomicVarId
    ) {
        if (!this.iocContainer.contains(threadInstanceUUID)) {
            // throw an error, it should exist
            throw new AtomicVarContainer_DoesNotExistException("");
        }

        ConcurrentHashMap<String, AtomicReference<?>> atomicVars = this.iocContainer.getOrDefault(threadInstanceUUID, null);

        if (atomicVars == null) {
            // throw an error, concurrency error when considering member deletions at
            // the level of 'iocContainer'
            throw new AtomicVarContainer_ConcurrencyException("");
        }

        return atomicVars.contains(atomicVarId);
    }
}
