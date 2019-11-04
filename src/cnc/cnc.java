/*
* TODO: Serial Communication
*  */
package cnc;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.fazecast.jSerialComm.*;

class cnc {
    private static String startPointCode;

    private static SerialPort portName;
    private  boolean run = false;


    public static void main(String [] args){
        SerialPort[] availablePorts = SerialPort.getCommPorts(); //get all COM-Ports on the used PC
        cnc cnc = new cnc();


        ArrayList<String> portList = new ArrayList<>(); //make a list of all available Ports
        for(SerialPort p: availablePorts)
        {
            portList.add(p.getSystemPortName());
        }
        System.out.println("Select a Port from this List: "); //let the user chose a port
        for(int i = 0; i<portList.size();i++)
        {
            String msg = i +": "+portList.get(i);
            System.out.println(msg);
        }
        int choice = 1;
        try {
            choice = System.in.read()-48; //ASCII-Int conversion
        } catch (IOException e) {
            e.printStackTrace();
        }
        portName  = SerialPort.getCommPort(availablePorts[choice].getSystemPortName()); //use the selected port
        portName.setBaudRate(115200); //GRBL default baud rate
        portName.setComPortTimeouts(65536,0,0);
        portName.openPort();


        if(portName.openPort()) {
            System.out.println("Selected Port opened!");
            InputStream in = portName.getInputStream();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                       while  (in.available() > 0) { //As long as there is data coming in, read it
                            String data = Character.toString((char) in.read()); //Save the incoming data in a variable
                            System.out.print(data);
                        }
                        cnc.run = true;

                    } catch (Exception ex) {
                        System.out.println("Error");
                    }
                }
            },10,50);

            for (int i = 0; i<5;i++)
            {
                cnc.createCode(2, 5); //Bounds for the Diameter point
            }


        }else{
            System.out.println("Error: Could not connect to Port");
        }

    }

         @SuppressWarnings("SameParameterValue")
         private void createCode(@SuppressWarnings("SameParameterValue") int min, int max){
        Random random = new Random();
        int Diameter = random.nextInt((max-min)+1)+min; //Creates a random diameter within our machine bounds
        System.out.println(Diameter);
             //Change to correct value
             int globalXMax = 10;
             int newXMax = globalXMax - Diameter;
             //Change to correct value
             int globalYMax = 10;
             int newYMax = globalYMax - Diameter;
        int startX = random.nextInt((newXMax- Diameter)+1) + Diameter;
        int startY = random.nextInt((newYMax- Diameter)+1) + Diameter;
        int Mx = 0;
        int My = 0;

        int randDir = random.nextInt(4);
        switch (randDir){
            case 0:
                Mx = startX - Diameter/2;
                My = 0;
                break;
            case 1:
                Mx = startX + Diameter/2;
                My = 0;
                break;
            case 2:
                Mx = 0;
                My = startY - Diameter/2;
                break;
            case 3:
                Mx = 0;
                My = startY + Diameter/2;
                break;
            default:
                System.out.println("ERROR, SHITS FUCKED");
        }
        startPointCode = "G01 " + "X" + startX + " Y" + startY;
             String circleCode = "G02 " + "X" + startX + " Y" + startY + " I" + Mx + " J" + My;
        System.out.println(startPointCode);
        System.out.println(circleCode);

        PrintWriter output = new PrintWriter(portName.getOutputStream());
       output.flush();

        Thread thread = new Thread(()->{


            try {
                Thread.sleep(100L); //wait a bit
            } catch (Exception var4) {
                System.out.println("Error");
            }

            output.println("F100"); //set the FeedRate
            output.println(startPointCode); //s
            output.println(circleCode);
            output.flush(); // Flush the port
            System.out.println("DATA SENT");
            try {
                Thread.sleep(100L); //wait a bit
            } catch (Exception var3) {
                System.out.println("Error");
            }



        });
        Timer timer2 = new Timer();
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(run){
                    //noinspection CatchMayIgnoreException
                    try{
                        thread.start();
                    }catch (Exception ex){

                    }


                    run = !run;
                }
            }
        },2000,100);


    }
}
