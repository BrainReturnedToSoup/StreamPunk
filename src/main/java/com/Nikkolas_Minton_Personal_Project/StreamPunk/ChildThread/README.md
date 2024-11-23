Will feature a class for instantiating child threads, which will be invoked by the 
event loop thread. This class accepts the 'AtomicVarIoC' class reference as the dependency
in its constructor, which will then attempt to generate a unique UUID, instantiate
a thread, and have that thread initialize some atomic variables. These atomic variables
are then saved to the IoC container, using the UUID generated as the child thread key.

In this stage, the event loop should have already submitted the atomic variables that it will use to 
send messages to the child threads, which each instantiation will pull these vars from the IoC, and make a local
'communication buffer' so that the child thread can see what the event loop is sending as a general message
to all child threads. However, the message is targetted when the event loop defines the child thread target UUID
over a specific atomic var. 

The thing that is returned as part of this instantiation, is a 'communication buffer' object instance that includes
a top level member UUID, but also a list of atomic references used by the particular child thread to send messages
to the event loop. Hence, no atomic variable is used uni-directionally, but rather a communication 'loop' is made
which depends on propelling the FSMs on either side of the communication in some way. 
 
