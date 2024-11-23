package com.Nikkolas_Minton_Personal_Project.StreamPunk.AtomicBuffers.AtomicVarIoC;

import java.util.concurrent.atomic.AtomicReference;

public interface AtomicVarContainerInterface {

     boolean checkChildThreadExists(
            String threadInstanceUUID
    );

    void addChildThread(
            String threadInstanceUUID
    );

     void deleteChildThread(
            String threadInstanceUUID
    );

    void addAtomicVarToChildThread(
            String threadInstanceUUID,
            String atomicVarId,
            AtomicReference<?> atomicReference
    );

    void deleteAtomicVarFromChildThread(
            String threadInstanceUUID,
            String atomicVarId
    );

    AtomicReference<?> getAtomicVarFromChildThread(
            String threadInstanceUUID,
            String atomicVarId
    );

    boolean checkAtomicVarForChildThreadExists(
            String threadInstanceUUID,
            String atomicVarId
    );
}
