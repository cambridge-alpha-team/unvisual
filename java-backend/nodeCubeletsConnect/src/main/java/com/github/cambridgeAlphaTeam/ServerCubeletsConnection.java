package com.github.cambridgeAlphaTeam;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.InputStream;
import java.io.IOException;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

/**
 * This is the TCP cubelet connection class.
 * It opens a port and listens there for cubelets library to send data.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class ServerCubeletsConnection implements CubeletsConnection,
  Runnable
{ private int[] cubeletValues;
  private ServerSocket listenSocket;
  private static final Logger logger = Logger.getLogger(
                                         ServerCubeletsConnection.class);
  static ObjectMapper mapper = new ObjectMapper();

  public ServerCubeletsConnection(int port) throws IOException
  { /* One for each face of the Bluetooth cube */
    cubeletValues = new int[6];

    /* We want failed listenin socket creations to throw an exception */
    listenSocket =  new ServerSocket(port);
  }

  @Override
  public void run()
  { while (true)
    { final Socket s;
      try
      { s = listenSocket.accept();
      }
      catch (IOException e)
      { logger.error(e);
        return;
      }

      /* socketHandlingThread to handle the connection {{{ */
      Thread socketHandlingThread = new Thread()
      { @Override
        public void run()
        { InputStream is;
          try
          { is = s.getInputStream();
          }
          catch (IOException e)
          { logger.error(e);
            return;
          }

          try
          { Map<Integer, Integer> readValue = mapper.readValue(is,
            new TypeReference<Map<Integer, Integer>>() { });
            setCubeletValues(readValue);
          }
          catch (IOException e)
          { logger.error(e);
          }

          try
          { s.close();
          }
          catch (IOException e)
          { logger.error(e);
          }
        }
      };
      socketHandlingThread.setDaemon(true);
      socketHandlingThread.start();
      /* }}} */
    }
  }

  public synchronized int[] getCubeletValues()
  { return cubeletValues;
  }

  public synchronized void setCubeletValues(Map<Integer, Integer>
      cubeletsMap)
  { logger.debug("" + cubeletValues + "\t" + cubeletsMap);
    SortedSet<Integer> keys = new TreeSet<Integer>(cubeletsMap.keySet());
    int i = 0;
    for (Integer key : keys)
    { if (i < 6)
      { Integer value = cubeletsMap.get(key);
        if (value != null)
        { cubeletValues[i] = value;
        }
        i++;
      }
      else
      { break;
      }
    }
  }
}
