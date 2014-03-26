import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

public class BlockingServer {
 
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
            TServerSocket serverTransport = new TServerSocket(7911);
 
            ArithmeticService.Processor processor = new ArithmeticService.Processor(new ArithmeticServiceImpl());
 
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                    processor(processor));
            System.out.println("Starting server on port 7911 ...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        BlockingServer srv = new BlockingServer();
        srv.start();
    }
}
