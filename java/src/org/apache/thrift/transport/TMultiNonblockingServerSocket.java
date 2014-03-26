/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.thrift.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around ServerSocketChannel
 */
public class TMultiNonblockingServerSocket extends TNonblockingServerTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(TNonblockingServerTransport.class.getName());

  /**
   * These two TNonblockingServerSocket objects will listen on two different ports.
   */
  private TNonblockingServerSocket serverSocket_original = null;
  private TNonblockingServerSocket serverSocket_replicated = null;

  private Selector selector = null;

  public TMultiNonblockingServerSocket(TNonblockingServerSocket original, TNonblockingServerSocket replicated) {
    serverSocket_original = original;
    serverSocket_replicated = replicated;
  }

  public void listen() throws TTransportException {
    serverSocket_original.listen();
    serverSocket_replicated.listen();
  }

  protected TNonblockingSocket acceptImpl() throws TTransportException {
    try {
        TNonblockingSocket socket_original = null;
        TNonblockingSocket socket_replicated = null;
        selector.select();
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        while(selectedKeys.hasNext()) {
	    SelectionKey key = selectedKeys.next();
            selectedKeys.remove();
	    if(key.isAcceptable()) {
	        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                if(ssc.equals(serverSocket_original.getServerSocketChannel())) {

                    System.out.println("serverSocket_original receives an incoming connection request!");

		    socket_original = (TNonblockingSocket)serverSocket_original.accept();
                    //selectedKeys.remove();
                    return socket_original;
                }
	        if(ssc.equals(serverSocket_replicated.getServerSocketChannel())) {

                    System.out.println("serverSocket_replicated receives an incoming connection request!");

		    socket_replicated = (TNonblockingSocket)serverSocket_replicated.accept();
                    //selectedKeys.remove();
                    return socket_replicated;
                }
	    } 
        }
	return null ;
    }catch (IOException ex) {
        throw new TTransportException(ex);
    }
    // catch (TTransportException ioe) {
    //    throw new TTransportException(ioe);
    //}  
    //return socket_original;
  }

  public void registerSelector(Selector selector) {
    
    serverSocket_original.registerSelector(selector);
    serverSocket_replicated.registerSelector(selector);
    this.selector = selector;
  }

  public void close() {
    serverSocket_original.close();
    serverSocket_replicated.close();
  }

  public void interrupt() {
    // The thread-safeness of this is dubious, but Java documentation suggests
    // that it is safe to do this from a different thread context
    close();
  }

}
