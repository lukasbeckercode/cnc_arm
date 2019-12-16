/*
*
*  */
package cnc;

//IMPORTS--------------------
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.fazecast.jSerialComm.*;

class cnc {

    //GLOBAL VARIABLES -----------------------------------------------------------

    private static SerialPort portName;
    private  boolean run = false;
    private static final File configFile = new File("config.txt");
    private final Timer timer2 = new Timer();
   private int getCycles;
//----------------------------------------------------------------------------------------------------------------------
    public static void main(String [] args) throws FileNotFoundException {
        //LOCAL VARIABLES
        SerialPort[] availablePorts = SerialPort.getCommPorts(); //get all COM-Ports on the used PC
        cnc cnc = new cnc();
        config config = new config();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
//----------------------------------------------------------------------------------------------------------------------
        //READ THE CONFIG FILE & CHECK IF DEFAULT VALUES ARE USED
        try {
            String readString = bufferedReader.readLine();
            String defStr = readString.substring(readString.lastIndexOf("---")+3);
            String cyclesRaw = bufferedReader.readLine();

            config.def = defStr.equals("true");
            config.cycles =Integer.parseInt( cyclesRaw.substring(cyclesRaw.lastIndexOf("---")+3));
        } catch (IOException e) {
            e.printStackTrace();
        }
//----------------------------------------------------------------------------------------------------------------------
//NO DEFAULT VALUES; ASK FOR COM PORT
    if(!config.def) {
        ArrayList<String> portList = new ArrayList<>(); //make a list of all available Ports
        for (SerialPort p : availablePorts) {
            portList.add(p.getSystemPortName());
        }
        System.out.println("Select a Port from this List: "); //let the user chose a port
        for (int i = 0; i < portList.size(); i++) {
            String msg = i + ": " + portList.get(i);
            System.out.println(msg);
        }
        int choice;
        try {
            choice = System.in.read() - 48; //ASCII-Int conversion
           config.port =  availablePorts[choice].getSystemPortName();
        } catch (IOException e) {
            e.printStackTrace();
        }
//----------------------------------------------------------------------------------------------------------------------
        //DEFAULT VALUES AS USED IN CONFIG.TXT
    } else {
        try {
            String portStrRaw = bufferedReader.readLine();
            config.port = portStrRaw.substring(portStrRaw.lastIndexOf("---")+3);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------------------------------------------------------------------------
        //ASSIGN PORT VALUES
        portName = SerialPort.getCommPort(config.port); //use the selected port
        portName.setBaudRate(115200); //GRBL default baud rate
        portName.setComPortTimeouts(65536,0,0);
        portName.openPort();

//----------------------------------------------------------------------------------------------------------------------
      //INITIALIZING GRBL
        if(portName.openPort()) {
            System.out.println("Selected Port opened!");
            InputStream in = portName.getInputStream();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        //READ INCOMING DATA
                        /*
                        * This step is necessary to determine whether or not GBRL is ready
                        */
                       while  (in.available() > 0) { //As long as there is data coming in, read it
                            String data = Character.toString((char) in.read()); //Save the incoming data in a variable
                            System.out.print(data);
                        }
                       //CONFIRM GRBL IS READY
                        cnc.run = true;

                    } catch (Exception ex) {
                        System.out.println("Error"); // No Connection
                    }
                }
            },10,50);
//----------------------------------------------------------------------------------------------------------------------
            //RUN THE CNC CYCLES

                cnc.getCycles = config.cycles;
                cnc.createCode(50, 70); //Bounds for the Diameter point TODO:CHANGE TO CORRECT VALUES


        }else{
            System.out.println("Error: Could not connect to Port");
        }

    }
//----------------------------------------------------------------------------------------------------------------------
    //THIS METHOD CREATES RANDOM CIRCLES
         @SuppressWarnings("SameParameterValue")
         private void createCode(@SuppressWarnings("SameParameterValue") int min, int max) {
             PrintWriter output = new PrintWriter(portName.getOutputStream());
             for (int i = -1; i < getCycles; i++) {
                 //LOCAL VARIABLES
                 Random random = new Random();
                 int Diameter = random.nextInt((max - min) + 1) + min; //Creates a random diameter within our machine bounds
                 System.out.println(Diameter); //FOR DEBUGGING
                 //TODO:Change to correct value
                 int globalXMax = 260; //Physical X Border
                 int newXMax = globalXMax - Diameter; //Virtual Border

                 int globalYMax = 630; //Physical Y Border
                 int newYMax = globalYMax - Diameter; //Virtual Border
                 int startX = random.nextInt((newXMax - Diameter) + 1) + Diameter; //Starting point within Borders
                 int startY = random.nextInt((newYMax - Diameter) + 1) + Diameter; //Starting point within Borders
                 //Point, around which the machine draws an Arc
                 int Mx = 0;
                 int My = 0;

                 int randDir = random.nextInt(4); //This determines the Direction in which the circle will be drawn
//----------------------------------------------------------------------------------------------------------------------
                 switch (randDir) {
                     case 0: //DOWN
                         Mx = -Diameter / 2;
                         My = 0;
                         break;
                     case 1: //UP
                         Mx = Diameter / 2;
                         My = 0;
                         break;
                     case 2: //LEFT
                         Mx = 0;
                         My = -Diameter / 2;
                         break;
                     case 3: //RIGHT
                         Mx = 0;
                         My = Diameter / 2;
                         break;
                     default:
                         System.out.println("ERROR, SHITS FUCKED");
                 }
//----------------------------------------------------------------------------------------------------------------------
//CREATE THE G-CODE
                 String startPointCode = "G01 " + "X" + startX + " Y" + startY;
                 String circleCode = "G02 " + "X" + startX + " Y" + startY + " I" + Mx + " J" + My;
                 //DEBUGGING-----------------------------------------------------------------------------------------------------
                 System.out.println(startPointCode);
                 System.out.println(circleCode);
                 //--------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------------
                 //INITIALIZE SENDING THE G-CODE

                 output.flush();
//----------------------------------------------------------------------------------------------------------------------
                 Thread thread = new Thread(() -> {
                     try {
                         Thread.sleep(100L); //wait a bit
                     } catch (Exception var4) {
                         System.out.println("Error");
                     }

                     output.print("F4000 \n"); //set the FeedRate
                     output.print(startPointCode + '\n'); //send the Start Point
                     output.print(circleCode + '\n'); //Send the Circle Code

                     //DEBUGGING-----------------------------------------------------------------------------------------------------
                     System.out.println(startPointCode);
                     System.out.println(circleCode);
                     //--------------------------------------------------------------------------------------------------------------


                     output.flush(); // Flush the port

                     System.out.println("DATA SENT");
                     try {
                         Thread.sleep(100L); //wait a bit
                     } catch (Exception var3) {
                         System.out.println("Error");
                     }
                 });
//----------------------------------------------------------------------------------------------------------------------
//THIS PART DELAYS THE SENDING
                 /*
                  * This is necessary, because GRBL needs some time to process the G-Code it´s sent
                  */

                 timer2.scheduleAtFixedRate(new TimerTask() {
                     @Override
                     public void run() {
                         if (run) {
                             //noinspection CatchMayIgnoreException
                             try {
                                 thread.start();
                             } catch (Exception ex) {

                             }
                             run = !run;

                         }
                     }
                 }, 2000, 2000);

             }

         }
}