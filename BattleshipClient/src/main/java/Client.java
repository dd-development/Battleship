import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.ArrayList;


public class Client extends Thread{
    boolean isMatched = false;
    Socket socketClient;
    ObjectOutputStream out;
    ObjectInputStream in;

    private Consumer<Serializable> callback;

    Client(Consumer<Serializable> call){
        callback = call;
    }

    public void run() {

        try {
            socketClient = new Socket("127.0.0.1",5555);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        }
        catch(Exception e) {}

        while (isMatched == false) {
            try {
                String inStr = (String) in.readObject();
                if (inStr.equals("MATCHED!")) {
                    isMatched = true;
                }
                else {
                    // CONTINUE
                }
            }
            catch(Exception e) {}
        }

        while(true) {
            try {
                Message inMsg = (Message) in.readObject();
                callback.accept(inMsg);
            }
            catch(Exception e) {}
        }
    }

    public void send(Object data) {

        try {
            out.writeObject(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
