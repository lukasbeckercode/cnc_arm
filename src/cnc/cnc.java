/*
* TODO: Serial Communication
*  */
package cnc;

import java.util.Random;

public class cnc {
    private static int GlobalXMax = 10; //Change to correct value
    private static int GlobalYMax = 10; //Change to correct value
    private static String startPointCode;
    private static String circleCode;
    public static void main(String [] args){

        createCode(2,5); //Bounds for the Diameter point
    }

    static private void createCode(int min, int max){
        Random random = new Random();
        int Diameter = random.nextInt((max-min)+1)+min; //Creates a random diameter within our machine bounds
        System.out.println(Diameter);
        int newXMin = Diameter;
        int newYMin = Diameter;
        int newXMax = GlobalXMax - Diameter;
        int newYMax = GlobalYMax - Diameter;
        int startX = random.nextInt((newXMax-newXMin)+1) + newXMin;
        int startY = random.nextInt((newYMax-newYMin)+1) + newYMin;
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
        circleCode = "G02 "+ "X" + startX + " Y" + startY + " I" + Mx + " J" + My;
        System.out.println(startPointCode);
        System.out.println(circleCode);
    }
}
