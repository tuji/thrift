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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Socket implementation of the TTransport interface. RepFlow version.
 *
 */
public class TMultiSocket extends TTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TMultiSocket.class.getName());

  /**
   * "original" is used for the original flow, and "replicated" is used for the replicated flow.
   */
  private TSocket original = null;
  private TSocket replicated = null;

  /**
   * variables that record the number of bytes read by each TSocket object
   */
  private int read_original = 0;  //number of bytes read by original TSocket object
  private int read_replicated = 0;  //number of bytes read by replicated TSocket object
  private int read_max = 0;  //max bytes  

  /**
   * Constructor that takes an already created socket.
   *
   * @param socket Already created socket object
   * @throws TTransportException if there is an error setting up the streams
   */
  public TMultiSocket(TSocket original, TSocket replicated) {
    this.original = original;
    this.replicated = replicated;
  }

  /**
   * Sets the timeout of the two TSocket objects
   *
   * @param timeout Milliseconds timeout
   */
  public void setTimeout(int timeout) {
    original.setTimeout(timeout);
    replicated.setTimeout(timeout);
  }

  /**
   * Checks whether both of the TSocket objects are connected.
   */
  public boolean isOpen() {
    return original.isOpen() && replicated.isOpen();
  }

  /**
   * Connects both of the two TSocket objects.
   */
  public void open() throws TTransportException {
    //checks whether both of the two TSocket objects are open
    if (isOpen()) {
      throw new TTransportException(TTransportException.ALREADY_OPEN, "Socket already connected.");
    }

    original.open();
    replicated.open();
  }

  /**
   * Closes the socket.
   */
  public void close() {
    original.close();
    replicated.close();
  }

  /**
   * read data into the buffer
   */
  public int read(byte[] buf, int off, int len) throws TTransportException {
    //buffers that store the data temporarily
    byte[] originalBuf = new byte[buf.length];
    byte[] replicatedBuf = new byte[buf.length];
    
    //variables that records the number of bytes read one time
    int num_original = 0; 
    int num_replicated = 0;

    //performs read operation for both TSocket objects
    num_original = original.read(originalBuf, 0, len);
    num_replicated = replicated.read(replicatedBuf, 0, len);

    //update the total number of bytes read from original flow and replicated flow
    read_original += num_original;
    read_replicated += num_replicated;

    //if the number of bytes read from original flow is larger than that from replicated flow
    if(read_original >= read_replicated && read_original > read_max) {
        //the number of new bytes
        int new_bytes = read_original - read_max;
        //the index of first new byte
        int offset = num_original - new_bytes;
        //copy the new bytes to the buf
        for(int i = 0; i < new_bytes; i++) 
	    buf[off + i] = originalBuf[offset + i];
        //update the max number
        read_max = read_original;
        return new_bytes;
    } else if(read_replicated > read_original && read_replicated > read_max) {
        //the number of new bytes
        int new_bytes = read_replicated - read_max;
        //the index of first new byte
        int offset = num_replicated - new_bytes;
        //copy the new bytes to the buf
        for(int i = 0; i < new_bytes; i++) 
	    buf[off + i] = replicatedBuf[offset + i];
        //update the max number
        read_max = read_replicated;
        return new_bytes;
    } else 
	return 0;
  }

  /**
   * 
   */
  public void write(byte[] buf, int off, int len) throws TTransportException {
    original.write(buf, off, len);
    replicated.write(buf, off, len);
  }

  /**
   * Flushes the underlying output stream if not null.
   */
  public void flush() throws TTransportException {
    original.flush();
    replicated.flush();
  }
}
