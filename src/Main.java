import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
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
            IPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String line;
        DataInputStream is;
        PrintStream os;
        int randomNum2 = ThreadLocalRandom.current().nextInt(6000, 7000 + 1);
        String request = PROGRAM_NAME+Integer.toString(randomNum2);
        byte[] receiveData = new byte[26];
        byte[] sentData = new byte[26];
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
                    System.out.println("Got TCP connection");
                    //someone connected to me using TCP
                    BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                    while(clientSocket.isConnected()) {
                        String clientSentence = inFromClient.readLine();

                        if (!connectedToServer) {
                            System.out.println("Received: " + clientSentence);
                        } else { //send msg to server
                            char[] myNameChars = clientSentence.toCharArray();
                            myNameChars[4] = 'x';
                            clientSentence = String.valueOf(myNameChars);
                            outToServer.writeBytes(clientSentence);
                        }
                    }
                } catch (SocketTimeoutException e){
                    System.out.println("didnt get connection yet");
                }
                System.out.println("trying udp...");
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(receivePacket);
                    System.out.println("My Address: "+IPAddress.getHostAddress());
                    System.out.println("Client Address: "+receivePacket.getAddress().getHostAddress());
                    if (receivePacket.getAddress().getHostAddress().equals(IPAddress.getHostAddress()))
                        throw new SocketTimeoutException();

                    //System.out.println("msg received:" + new String(receivePacket.getData())+ "   length  "+ new String(receivePacket.getData()).length());
                    System.out.println("receved packet length:    "+receivePacket.getLength());

                    //receive request
                    if(receivePacket.getLength()==20){
                        System.out.println("request received from "+receivePacket.getAddress().getHostAddress());
                        //want to send offer
                        byte[] sendData = makeOffer(receivePacket,randomNum,IPAddress);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receivePacket.getAddress().getHostAddress()), 6000);
                        udpSocket.send(sendPacket);
                    }

                    //receive offer
                    if(receivePacket.getLength()==26){
                        System.out.println("offer received from "+receivePacket.getAddress().getHostAddress());

                        //want toconnect with TCP
                        byte[] rcvData = receivePacket.getData();
                        //int connectPort = receivePacket.getData();
                        byte[] ipArray = {rcvData[20], rcvData[21], rcvData[22], rcvData[23]};
                        byte[] port = {rcvData[24], rcvData[25]};

                        InetAddress connectIP = InetAddress.getByAddress(ipArray);
                        System.out.println(connectIP.getHostAddress());


                        int connectPort = (int) port[0]*100 + (int)port[1] ;
                        System.out.println("IP: "+connectIP + "  port: "+ connectPort);
                        Socket tx = new Socket(connectIP,connectPort); //connects to tcp server
                        connectedToServer=true;
                        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
                        while(tx.isConnected()) {
                            outToServer = new DataOutputStream(tx.getOutputStream());
                            String sentence = inFromUser.readLine();
                            outToServer.writeBytes(sentence + '\n');

                            try {
                                tryUDP(udpSocket, tcpSocket, randomNum);
                            } catch (SocketTimeoutException e) {

                            }
                            //try to receive request to find server
                        }
                    }

                    break;
                } catch (SocketTimeoutException e){
                    System.out.println("didnt get connection yet");
                    udpSocket.setBroadcast(true);
                    DatagramPacket sendPacket = new DatagramPacket(sentData, sentData.length, InetAddress.getByName("255.255.255.255"), 6000);
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


    private static void tryUDP(DatagramSocket udpSocket, ServerSocket tcpSocket, int randomNum) throws IOException, SocketTimeoutException{
        byte[] receiveData = new byte[26];
        byte[] sentData = new byte[26];

        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("trying udp...");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receivePacket);

        if (receivePacket.getAddress().getHostAddress().equals(IPAddress.getHostAddress()))
            throw new SocketTimeoutException();

        //if packet received.. send offer
        if(receivePacket.getLength()==20){
            System.out.println("request received from "+receivePacket.getAddress().getHostAddress());
            //want to send offer
            System.out.println(new String(receivePacket.getData()));

            byte[] offer = new byte[26];
            String receivedString = new String(receivePacket.getData());
            String receivedNum = receivedString.substring(17);
            String offerString = PROGRAM_NAME+receivedNum+IPAddress.getHostAddress()+randomNum;
            byte[] sentData2 = offerString.getBytes();


            System.out.println(new String(sentData2));

            DatagramPacket sendPacket = new DatagramPacket(sentData2, sentData2.length, InetAddress.getByName(receivePacket.getAddress().getHostAddress()), 6000);
            udpSocket.send(sendPacket);
        }
    }

    private static byte[] makeOffer(DatagramPacket receivePacket, int randomNum, InetAddress IPAddress){
        byte[] offer = new byte[26];
        String receivedString = new String(receivePacket.getData());
        String receivedNum = receivedString.substring(17);

        //String offerString = PROGRAM_NAME+receivedNum+IPAddress.getHostAddress()+randomNum;
        String offerString = PROGRAM_NAME;
        byte[] arr1 = offerString.getBytes();
        byte[] sentData2 = new byte[4];
        sentData2 = IPAddress.getAddress();
        byte[] receivednum = ByteBuffer.allocate(4).putInt(Integer.valueOf(receivedNum)).array();
        //sentData2 = offerString.getBytes();
        byte[] sendData = new byte[26];

        String left="",right="";
        int num = randomNum;
        right = num %10 + right;
        num = num/10;
        right = num %10 + right;
        num = num/10;
        left = num %10 + left;
        num = num/10;
        left = num %10 + left;

        int upper = Integer.valueOf(left);
        int lower = Integer.valueOf(right);

        System.arraycopy(arr1,0,sendData,0,16);
        System.arraycopy(receivednum,0,sendData,16,4);
        System.arraycopy(sentData2,0,sendData,20,4);
        //System.arraycopy(portnum,0,sendData,24,4);
        sendData[24] = (byte) upper;
        sendData[25] = (byte) lower;
        System.out.println("sending message of length:" + sendData.length);
        return sendData;
    }

}