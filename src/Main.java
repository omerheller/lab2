import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static final String PROGRAM_NAME = "Networking170000";
    public static void main(String args[]) {
// declaration section:
// declare a server socket and a client socket for the server
// declare an input and an output stream
        int randomNum = 0;
        ServerSocket tcpSocket = null;
        DatagramSocket udpSocket = null;
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String line;
        DataInputStream is;
        PrintStream os;
        int randomNum2 = ThreadLocalRandom.current().nextInt(6000, 7000 + 1);
        String request = PROGRAM_NAME+Integer.toString(randomNum2);
        byte[] receiveData = new byte[20];
        byte[] sentData = new byte[20];
        boolean connectedToServer = false;
        DataOutputStream outToServer = null;



        sentData = request.getBytes();
        Socket clientSocket = null;
        try {
            randomNum = ThreadLocalRandom.current().nextInt(6000, 7000 + 1);
            tcpSocket = new ServerSocket(randomNum);
            tcpSocket.setSoTimeout(3000);
            udpSocket = new DatagramSocket(6000);
            udpSocket.setSoTimeout(3000);
        }
        catch (IOException e) {
            System.out.println(e);
        }
// Create a socket object from the ServerSocket to listen and accept
// connections.
// Open input and output streams
        try {
            while(true) {
                System.out.println("trying tcp...");
                try {
                    clientSocket = tcpSocket.accept();
                    //someone connected to me using TCP
                    BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                    String clientSentence = inFromClient.readLine();

                    if(!connectedToServer){
                        System.out.println("Received: " + clientSentence);
                    }
                    else{ //send msg to server
                        outToServer.writeBytes(clientSentence);
                    }







                    break;
                } catch (SocketTimeoutException e){
                    System.out.println("didnt get connection yet");
                }
                System.out.println("trying udp...");
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(receivePacket);
                    if (receivePacket.getAddress().getHostAddress().equals(IPAddress.getHostAddress()))
                        throw new SocketTimeoutException();

                    System.out.println(IPAddress.getHostAddress());
                    //receive request
                    if(receivePacket.getLength()==20){
                        System.out.print("request received from "+receivePacket.getAddress().getHostAddress());
                        //want to send offer
                        byte[] offer = new byte[26];
                        String receivedString = new String(receivePacket.getData());
                        String receivedNum = receivedString.substring(17);
                        String offerString = PROGRAM_NAME+receivedNum+IPAddress.getHostAddress()+randomNum;
                        byte[] sentData2 = offerString.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sentData2, sentData2.length, IPAddress, 6000);
                        udpSocket.send(sendPacket);
                    }

                    //receive offer
                    if(receivePacket.getLength()==26){
                        System.out.print("offer received from "+receivePacket.getAddress().getHostAddress());

                        //want toconnect with TCP
                        String receivePacketString = new String(receivePacket.getData());
                        InetAddress connectIP = InetAddress.getByName(receivePacketString.substring(20,23));
                        int connectPort = Integer.getInteger(receivePacketString.substring(24));
                        Socket tx = new Socket(connectIP,connectPort); //connects to tcp server
                        connectedToServer=true;
                        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
                        outToServer = new DataOutputStream(tx.getOutputStream());
                        String sentence = inFromUser.readLine();
                        outToServer.writeBytes(sentence + '\n');

                    }



                    break;
                } catch (SocketTimeoutException e){
                    System.out.println("didnt get connection yet");
                    DatagramPacket sendPacket = new DatagramPacket(sentData, sentData.length, IPAddress, 6000);
                    udpSocket.send(sendPacket);
                }
            }
// As long as we receive data, echo that data back to the client.
            while (true) {
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}