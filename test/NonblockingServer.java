import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TMultiNonblockingServerSocket;

public class NonblockingServer {
 
    class ArithmeticServiceImpl implements ArithmeticService.Iface {
 
        public long add(int num1, int num2) throws TException {
            return num1 + num2;
        }
 
        public long multiply(int num1, int num2) throws TException {
            return num1 * num2;
        }
 
    }
    private void start() {
        try {
            TNonblockingServerSocket original = new TNonblockingServerSocket(7911);
            TNonblockingServerSocket replicated = new TNonblockingServerSocket(8080);
            TNonblockingServerTransport serverTransport = new TMultiNonblockingServerSocket(original, replicated);
            ArithmeticService.Processor processor = new ArithmeticService.Processor(new ArithmeticServiceImpl());
 
            TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport).processor(processor));
	    System.out.println("Starting server on port 7911 and 8080...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        NonblockingServer srv = new NonblockingServer();
        srv.start();
    }
}
