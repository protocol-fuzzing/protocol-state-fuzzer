package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


// Wrapper around the java Socket so we have clear segmentation of inputs and outputs
public class SocketWrapper {
    protected Socket sock;
    protected PrintWriter sockout;
    protected BufferedReader sockin;


    public SocketWrapper(String sutIP, int sutPort) {
        try {
            sock = new Socket(sutIP, sutPort);
            sockout = new PrintWriter(sock.getOutputStream(), true);
            sockin = new BufferedReader(new InputStreamReader(
                    sock.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketWrapper(Socket sock) {
        try {
            this.sock = sock;
            sockout = new PrintWriter(sock.getOutputStream(), true);
            sockin = new BufferedReader(new InputStreamReader(
                    sock.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void writeOutput(String input) {
        if (sockout != null) {
            sockout.println(input);
            sockout.flush();
        }
    }

    public String readInput() {
        String output = null;
        try {
            output = sockin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public boolean isClosed() {
        return sock.isClosed();
    }

    public void close() {
        try {
            if (sock != null) {
                sock.close();
                sockout.close();
                sockin.close();
            }
        } catch (IOException ex) {

        }
    }
}
