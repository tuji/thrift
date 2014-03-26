import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TMultiSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class NonblockingClient {
 
    private void invoke() {
        TTransport transport;
        try {
            TSocket original = new TSocket("localhost", 7911);
            TSocket replicated = new TSocket("localhost", 8080);
            transport = new TFramedTransport(new TMultiSocket(original, replicated));
	    transport.open();
	    
            TProtocol protocol = new TBinaryProtocol(transport);
 
            ArithmeticService.Client client = new ArithmeticService.Client(protocol);
            
            long addResult = client.add(10, 20);
            System.out.println("Add result: " + addResult);
            long multiplyResult = client.multiply(10, 20);
            System.out.println("Multiply result: " + multiplyResult);
	    long addResult1 = client.add(10, 20);
            System.out.println("Add result: " + addResult1);
            long multiplyResult1 = client.multiply(10, 20);
            System.out.println("Multiply result: " + multiplyResult1);

            transport.close();
        } 
          catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        NonblockingClient c = new NonblockingClient();
        c.invoke();
    }
 
}
