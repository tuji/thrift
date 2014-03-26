import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TMultiNonblockingSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.io.IOException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;

public class AsyncClient_Replicated {
 
    private void invoke() {
        try {
            ArithmeticService.AsyncClient client = new ArithmeticService.
                    AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(),
                                new TNonblockingSocket("localhost", 7911));
 
            client.add(200, 400, new AddMethodCallback());
	   
           
            client = new ArithmeticService.
                    AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(),
                                new TNonblockingSocket("localhost", 7911));
            client.multiply(20, 50, new MultiplyMethodCallback());
            
 	    //while(true) {}
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        AsyncClient_Replicated c = new AsyncClient_Replicated();
        c.invoke();
 
    }
 
    class AddMethodCallback
            implements AsyncMethodCallback<ArithmeticService.AsyncClient.add_call> {
 
        public void onComplete(ArithmeticService.AsyncClient.add_call add_call) {
            try {
                long result = add_call.getResult();
                System.out.println("Add from server: " + result);
            } catch (TException e) {
                e.printStackTrace();
            }
        }
 
        public void onError(Exception e) {
            System.out.println("Error : ");
            e.printStackTrace();
        }
 
    }
 
    class MultiplyMethodCallback
            implements AsyncMethodCallback<ArithmeticService.AsyncClient.multiply_call> {
 
        public void onComplete(ArithmeticService.AsyncClient.multiply_call multiply_call) {
            try {
                long result = multiply_call.getResult();
                System.out.println("Multiply from server: " + result);
            } catch (TException e) {
                e.printStackTrace();
            }
        }
 
        public void onError(Exception e) {
            System.out.println("Error : ");
            e.printStackTrace();
        }
 
    }
 
}
