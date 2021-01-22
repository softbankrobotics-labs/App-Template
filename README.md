# Pepper Application Template

This is a general structure of an android application built for Pepper. More precisely this should help with understanding the interactions between your app some of the features of the QISDK, mostly regarding speech. It should provide a good base to start developing your android applications, the key points of this architecture being :
 - Only one activity, the different screens use fragments.
 - Each fragment has its own topic assigned to it
 - Topics are enabled and disabled when changing screens  
 - A topic contains some basic Qichat concepts, and is always enabled

## Compatibility

Tested running on pepper 1.8 and 1.8a, using NAOqi 2.9.5
