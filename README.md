Multicasting Chat Program
authors: Madalin Druta
        Antonio Nikolov
        Mark Whelan

This program was part of an college project. The purpose of the project was to design a multicast chat system over LAN using java that can simulate dropped packets.
The application has a custom message exchange protocol and packet format,
The sender of a message will resend the message that the user has typed if it has not received acknowledgments from all subscribed members to the chat group.
To simulate noise modify the Receiver.java file and recompile.

How to run:
from command line type "java -jar Multicasting.jar"
in the details form:
-type in your name.
-type an ip address to which the group of chat members is going to use. Agree with your friends on an address beforehand. Must be above 224.0.0.0
-type an agreed upon port. Should be above 6000.

Bugs
-two people cannot have the same user name. The transmission protocol used relies on unique names.
- If the user does not type in a name he is assigned one is not assigned by default
