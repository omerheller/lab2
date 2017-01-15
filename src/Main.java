import java.io.*;
import java.math.BigInteger;
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
            IPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String line;
        DataInputStream is;
        PrintStream os;
        int randomNum2 = ThreadLocalRandom.current().nextInt(6000, 7000 + 1);
        String request = PROGRAM_NAME;
        byte[] programName = request.getBytes();
        byte[] receiveData = new byte[26];
        byte[] requestArray = new byte[20];
        boolean connectedToServer = false;
        DataOutputStream outToServer = null;
        int randomNum4 = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        String binaryString = Integer.toBinaryString(randomNum4);
        while (binaryString.length()<32){
            binaryString = 0 + binaryString;
        }
        byte[] bval = new BigInteger(binaryString, 2).toByteArray();
        System.arraycopy(programName,0,requestArray,0,16);
        System.arraycopy(bval,0,requestArray,16,4);
        //sentData = request.getBytes();
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
                System.out.println("trying TCP...");
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
                            int randomNum3 = ThreadLocalRandom.current().nextInt(0, myNameChars.length + 1);
                            myNameChars[randomNum3] = 'x';
                            clientSentence = String.valueOf(myNameChars);
                            outToServer.writeBytes(clientSentence);
                        }
                    }
                } catch (SocketTimeoutException e){
                    System.out.println("-----didnt get connection yet");
                }
                System.out.println("Trying UDP...");
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(receivePacket);
                    //System.out.println("My Address: "+IPAddress.getHostAddress());
                    //System.out.println("Client Address: "+receivePacket.getAddress().getHostAddress());
                    if (receivePacket.getAddress().getHostAddress().equals(IPAddress.getHostAddress())) {
                        System.out.println("Got packet from self. Ignored.");
                        throw new SocketTimeoutException();
                    }
                    //System.out.println("msg received:" + new String(receivePacket.getData())+ "   length  "+ new String(receivePacket.getData()).length());
                    System.out.println("Received packet length:    "+receivePacket.getLength());

                    //receive request -> send offer
                    if(receivePacket.getLength()==20){
                        System.out.println("Request received from "+receivePacket.getAddress().getHostAddress());
                        //want to send offer
                        byte[] sendData = makeOffer(receivePacket,randomNum,IPAddress);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receivePacket.getAddress().getHostAddress()), 6000);
                        udpSocket.send(sendPacket);
                    }

                    //receive offer -> connect with TCP with the info in the receivePacket
                    if(receivePacket.getLength()==26) {
                        System.out.println("Offer received from " + receivePacket.getAddress().getHostAddress());

                        //want toconnect with TCP
                        byte[] rcvData = receivePacket.getData();
                        byte[] requestNum = {rcvData[16], rcvData[17], rcvData[18], rcvData[19]};
                        if (!((requestNum[0] == bval[0]) &&
                                (requestNum[1] == bval[1]) &&
                                (requestNum[2] == bval[2]) &&
                                (requestNum[3] == bval[3]))) {
                            throw new SocketTimeoutException();
                        }
                            //int connectPort = receivePacket.getData();
                            byte[] ipArray = {rcvData[20], rcvData[21], rcvData[22], rcvData[23]};
                            byte[] port = {rcvData[24], rcvData[25]};

                            InetAddress connectIP = InetAddress.getByAddress(ipArray);


                            int connectPort = (int) port[0] * 100 + (int) port[1];
                            System.out.println("IP: " + connectIP + "  port: " + connectPort);
                            Socket tx = new Socket(connectIP, connectPort); //connects to tcp server
                            connectedToServer = true;
                            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                            while (tx.isConnected()) {
                                outToServer = new DataOutputStream(tx.getOutputStream());
                                String sentence = inFromUser.readLine();
                                outToServer.writeBytes(sentence + '\n');

                                try {
                                    tryUDP(udpSocket, tcpSocket, randomNum);
                                    //System.out.println("Listening TCP...");
                                    clientSocket = tcpSocket.accept();
                                    System.out.println("Got TCP connection");
                                    while (clientSocket.isConnected() && tx.isConnected()) {
                                        BufferedReader inFromClient =
                                                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                        DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                                        while (clientSocket.isConnected()) {
                                            String clientSentence = inFromClient.readLine();

                                            if (!connectedToServer) {
                                                //System.out.println("Received: " + clientSentence);
                                            } else { //send msg to server
                                                System.out.println("Received: " + clientSentence);
                                                //char[] myNameChars = clientSentence.toCharArray();
                                                //myNameChars[0] = 'x';
                                                //clientSentence = String.valueOf(myNameChars);
                                                char[] myNameChars = clientSentence.toCharArray();
                                                if (myNameChars.length > 0) {
                                                    int randomNum3 = ThreadLocalRandom.current().nextInt(0, myNameChars.length);
                                                    myNameChars[randomNum3] = 'x';
                                                    clientSentence = String.valueOf(myNameChars);
                                                }
                                                System.out.println("Sending " + clientSentence);
                                                outToServer = new DataOutputStream(tx.getOutputStream());
                                                outToServer.writeBytes(clientSentence + '\n');
                                            }
                                        }

                                    }
                                } catch (SocketTimeoutException e) {

                                }
                                //try to receive request to find server

                            }

                    }
                } catch (SocketTimeoutException e){
                    System.out.println("didnt get connection yet");
                    udpSocket.setBroadcast(true);
                    DatagramPacket sendPacket = new DatagramPacket(requestArray, requestArray.length, InetAddress.getByName("255.255.255.255"), 6000);
                    udpSocket.send(sendPacket);
                }
            }
// As long as we receive data, echo that data back to the client.
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     *
     * This function tries to connect to UDP socket. If connection succeeds and receives a request, then create an offer message. Else through a sockettimeout exception
     *
     * @param udpSocket
     * @param tcpSocket
     * @param randomNum
     * @throws IOException
     * @throws SocketTimeoutException
     */
    private static void tryUDP(DatagramSocket udpSocket, ServerSocket tcpSocket, int randomNum) throws IOException, SocketTimeoutException{
        byte[] receiveData = new byte[26];
        byte[] sentData = new byte[26];

        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("Trying UDP...");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receivePacket);

        if (receivePacket.getAddress().getHostAddress().equals(IPAddress.getHostAddress()))
            throw new SocketTimeoutException();

        //if packet received.. send offer
        if(receivePacket.getLength()==20){
            System.out.println("Request received from "+receivePacket.getAddress().getHostAddress());
            //want to send offer
            byte[] sendData = makeOffer(receivePacket,randomNum,IPAddress);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receivePacket.getAddress().getHostAddress()), 6000);
            udpSocket.send(sendPacket);
        }
    }

    /**
     *
     *This function builds the offer and returns the message
     *
     * @param receivePacket
     * @param IPAddress
     * @return the message being sent to the connection
     */
    private static byte[] makeOffer(DatagramPacket receivePacket, int port, InetAddress IPAddress){
        String offerString = PROGRAM_NAME;
        byte[] programNameArray = offerString.getBytes();
        byte[] ipArray = new byte[4];
        ipArray = IPAddress.getAddress();
        byte[] receivedDataArray = receivePacket.getData();
        byte[] receivednum = {receivedDataArray[16],receivedDataArray[17],receivedDataArray[18],
                receivedDataArray[19]};
        byte[] sendData = new byte[26];

        String left="",right="";
        int num = port;
        right = num %10 + right;
        num = num/10;
        right = num %10 + right;
        num = num/10;
        left = num %10 + left;
        num = num/10;
        left = num %10 + left;

        int upper = Integer.valueOf(left);
        int lower = Integer.valueOf(right);

        System.arraycopy(programNameArray,0,sendData,0,16);
        System.arraycopy(receivednum,0,sendData,16,4);
        System.arraycopy(ipArray,0,sendData,20,4);
        sendData[24] = (byte) upper;
        sendData[25] = (byte) lower;
        System.out.println("Sending message of length:" + sendData.length);
        return sendData;
    }

}