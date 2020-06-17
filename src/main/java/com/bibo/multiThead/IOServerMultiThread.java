package com.bibo.multiThead;

import org.apache.logging.log4j.core.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

//为每个请求创建一个线程
public class IOServerMultiThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOServerMultiThread.class);

    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        try {

            serverSocket = new ServerSocket();

            serverSocket.bind(new InetSocketAddress(2345));

        } catch (IOException ex) {

            LOGGER.error("Listen failed", ex);

            return;

        }

        try{

            while(true) {

                Socket socket = serverSocket.accept();

                new Thread( () -> {

                    try{

                        InputStream inputstream = socket.getInputStream();

                        LOGGER.info("Received message {}", IOUtils.toString(new InputStreamReader(inputstream)));

                    } catch (IOException ex) {

                        LOGGER.error("Read message failed", ex);

                    }

                }).start();

            }

        } catch(IOException ex) {

            try {

                serverSocket.close();

            } catch (IOException e) {

            }

            LOGGER.error("Accept connection failed", ex);

        }

    }
}
