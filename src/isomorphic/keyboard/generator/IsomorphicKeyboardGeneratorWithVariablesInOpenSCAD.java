package isomorphic.keyboard.generator;

import java.util.*;
import java.io.*;

public class IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD {

    Scanner scan = new Scanner(System.in);
    PrintWriter pw, pwKeyTop, pwValues, togetherPrint;
    double metalRoundRadius, octaveWidth, periodWidth, underKeyWidth, blackKeyHeight, whiteKeyLength, blackKeyLength, edgeRadius = 1.5,//measurements 
            genh, overhead, keyTopSide1, keyTopSide2, shift,//derived stuff
            theta, q, r, a, b, c, d, z,//bunch of triangle stuff
            generator, holeScaleX, holeScaleY, stalkScaleX, stalkScaleY;//0.44 was too small once I rotated keybases in printing
    int periodSteps, generatorSteps, desiredGamut, startingKey, range, genForLargeStep, genForSmallStep, stepsForLarge, stepsForSmall, genForStep1, genForStep1b;
    boolean isKeytop, verticalFlip, neededAbsoluteValue=false;

    ArrayList<Integer> mosSizes = new ArrayList<>();
    ArrayList<mosScale> mosTracker = new ArrayList<>();

    public static void main(String[] args) {
        IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD IKG = new IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD();
        IKG.getUserInputAndDeriveConstants();
        IKG.generateFiles();
    }

    public void generateFiles() {
        int currentPianoKey, currentGenerator;
        //currentGenerator = 0;//initialize at 0 for now at least, could get user input
        //boolean isWhiteKey;//false by default//SHOULD I SET THIS  I DON'T REMEMBER

        File together = new File("/home/john/Desktop/OPENSCAD_DUMP/together.scad");//together is the big collection of keys and keytops that will show if everything worked correctly
        System.out.println(together.getParentFile().mkdirs() + "<-------This is whether the makedirs succeeded or not");
        try {
            togetherPrint = new PrintWriter(together, "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
        }
        togetherPrint.println("include<values.scad>;");
        togetherPrint.println("//$fa=40;");

        File values = new File("/home/john/Desktop/OPENSCAD_DUMP/values.scad");
        values.getParentFile().mkdirs();
        try {
            pwValues = new PrintWriter(values, "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
        }

        createValuesFile();
        pwValues.close();

        for (int i = 0; i < range; i++) {//iterate keys until range is reached.

            //could check for duplicate files, NOT FOR NOWWWW
            currentPianoKey = (i + startingKey) % 12;//started on starting key, have moved i times, keys 12 steps apart are same underlying note
            if (verticalFlip) {
                currentGenerator = (i * genForStep1b) % periodSteps;
            } else {
                currentGenerator = (i * genForStep1) % periodSteps;
            }
            //modulo periodSteps because they share the same topside of the key and I want to start at the lowest one so that I include it, because I only move upwards later one, I think
            int keytopsNeeded = (desiredGamut - currentGenerator - 1) / periodSteps + 1;//-1 then plus one because if desiredGamut-currentGenerator)=periodSteps, I want it to return 1? 

            try {
                File file2 = new File("/home/john/Desktop/OPENSCAD_DUMP/" + i + "_" + currentGenerator + ".scad");
                file2.getParentFile().mkdirs();
                pw = new PrintWriter(file2, "UTF-8");

                togetherPrint.println("use<" + i + "_" + currentGenerator + ".scad>;");

                togetherPrint.println("translate([-" + (i * octaveWidth / 12) + ",0,0");

                togetherPrint.println("])");
                //togetherPrint.println("rotate([0,90,0])");
                togetherPrint.println(i + "_" + currentGenerator + "(false);");

                pw.println("use<keytop.scad>");
                pw.println("include<values.scad>");
                pw.println(i + "_" + currentGenerator + "();");
                pw.println("module " + i + "_" + currentGenerator + "(keytops=false){");

                createMainBase(currentGenerator, keytopsNeeded, i, currentPianoKey);

                createKeyStalks(currentGenerator, keytopsNeeded, currentPianoKey);

                pw.println("}");

                thinCuts(currentGenerator);

                pw.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        createKeytop();//after crazy for loop for bases, make keytops file ONCE OH YEAH ONCE
        togetherPrint.close();
    }

    public void thinCuts(int currentGenerator) {
        pw.println("//Thin Cuts:");
        pw.println("translate([0,0,-1]){");
        pw.println("linear_extrude(height=blackKeyHeight+" + metalRoundRadius + "+8+2)");
        pw.println("polygon(points=[[-0.1,10],[-0.1,length+0.1],[underKeyWidth/3,length+0.1]]);");
        pw.println("linear_extrude(height=blackKeyHeight+" + metalRoundRadius + "+8+2)");
        pw.println("polygon(points=[[underKeyWidth+0.1,10],[underKeyWidth+0.1,length+0.1],[underKeyWidth/3*2,length+0.1]]);");
        pw.println("\n}");

        pw.println("anglePoints=["
                + "[-underKeyWidth/3,10,blackKeyHeight+" + metalRoundRadius + "+8+1],"
                + "[0,10,blackKeyHeight+" + metalRoundRadius + "+8+1],"
                + "[underKeyWidth/3,length+0.1,blackKeyHeight+" + metalRoundRadius + "+8+1],"
                + "[0,length+0.1,blackKeyHeight+" + metalRoundRadius + "+8+1],"
                + "[0-underKeyWidth/3-underKeyWidth/3,10,blackKeyHeight+" + metalRoundRadius + "+8+1+20+20-(" + ((double) (currentGenerator + periodSteps) / (double) desiredGamut) * 20 + ")],"
                + "[0-underKeyWidth/3,10,blackKeyHeight+" + metalRoundRadius + "+8+1+20+20-(" + ((double) (currentGenerator + periodSteps) / (double) desiredGamut) * 20 + ")],"
                + "[underKeyWidth/3-underKeyWidth/3,length+0.1,blackKeyHeight+" + metalRoundRadius + "+8+1+20+20-(" + ((double) (currentGenerator + periodSteps) / (double) desiredGamut) * 20 + ")],"
                + "[0-underKeyWidth/3,length+0.1,blackKeyHeight+" + metalRoundRadius + "+8+1+20+20-(" + ((double) (currentGenerator + periodSteps) / (double) desiredGamut) * 20 + ")]"
                + "];");

        pw.println("angleFaces=["
                + "[0,1,2,3],"
                + "[0,4,5,1],"
                + "[0,3,7,4],"
                + "[3,2,6,7],"
                + "[1,5,6,2],"
                + "[7,6,5,4]"
                + "];");

        pw.println("translate([0,0,-0.001])polyhedron(anglePoints,angleFaces);");

        pw.println("translate([underKeyWidth,0,-0.001])mirror([1,0,0])polyhedron(anglePoints,angleFaces);");

        pw.println("}\n}");
    }

    public void createKeytop() {
        try {
            File file = new File("/home/john/Desktop/OPENSCAD_DUMP/keytop.scad");
            file.getParentFile().mkdirs();
            pwKeyTop = new PrintWriter(file, "UTF-8");

            pwKeyTop.println("include<values.scad>;");
            pwKeyTop.println("keytop();");
            pwKeyTop.println("");

            pwKeyTop.println("module keytopShape(){");//
            pwKeyTop.println("polyhedron(Points,Faces);");
            pwKeyTop.println("}");//

            pwKeyTop.println("module keytop(){");//make it a module so that together.scad can use it
            pwKeyTop.println("difference(){");
            pwKeyTop.println("keytopShape();");//make key

            pwKeyTop.println("translate([1/2*(b+c)-(1/2*(b+c)*holeScaleX),1/2*(d+a)-(1/2*(d+a)*holeScaleY),-0.1])");
            pwKeyTop.println("scale([holeScaleX,holeScaleY,0.5])");
            pwKeyTop.println("#keytopShape();");

            pwKeyTop.println("edge(6,11,1,-1);");//edges rounded by subtracting module below
            pwKeyTop.println("edge(6,7,1,1);");
            pwKeyTop.println("edge(7,8,1,1);");
            pwKeyTop.println("edge(8,9,1,1);");
            pwKeyTop.println("edge(9,10,1,-1,-1);");
            pwKeyTop.println("edge(10,11,1,-1);");
            pwKeyTop.println("}");

            pwKeyTop.println("module edge (point1,point2,d1,d2,d3=1){");//annoying combination of cylinders and cubes to round edges
            pwKeyTop.println("difference(){");
            pwKeyTop.println("translate([Points[point1][0],Points[point1][1],Points[point1][2]])");
            pwKeyTop.println("rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])");
            pwKeyTop.println("translate([0,0,d3*30])");
            pwKeyTop.println("rotate(45,0,0)");
            pwKeyTop.println("cube([pow(2*(edgeRadius*edgeRadius),1/2),pow(2*(edgeRadius*edgeRadius),1/2),90],true);");
            pwKeyTop.println("translate([Points[point1][0],Points[point1][1],Points[point1][2]])");
            pwKeyTop.println("rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])");
            pwKeyTop.println("translate([d1*edgeRadius,d2*edgeRadius,d3*30])");
            pwKeyTop.println("cylinder(90,edgeRadius,edgeRadius, true);");//don't need to fill in much here since it's already got the points

            pwKeyTop.println("}");
            pwKeyTop.println("}");

            pwKeyTop.println("");

            pwKeyTop.println("}");//DIFFERENCE END
            pwKeyTop.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public boolean isWhiteKey(int currentPianoKey) {
        return currentPianoKey == 0
                ||//0 is A, if key is a white key...
                currentPianoKey == 2
                || currentPianoKey == 3
                || currentPianoKey == 5
                || currentPianoKey == 7
                || currentPianoKey == 8
                || currentPianoKey == 10;
    }

    public void createKeyStalks(int currentGeneratorIn, int keytopsNeeded, int currentPianoKeyIn) {
        pw.println("//Key Stalks:");
        for (int j = 0; j < keytopsNeeded; j++) {//j changes generator to enharmonically eqauivalent values by being increasing by periodSteps until greater than gamut

            if (isWhiteKey(currentPianoKeyIn)) {
                pw.println("translate([underKeyWidth/2-(b+c)*stalkScaleX/2,genh*" + currentGeneratorIn + "+overhead,0.75*(blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4)]){");
            } else {
                pw.println("translate([underKeyWidth/2-(b+c)*stalkScaleX/2,genh*" + currentGeneratorIn + "+overhead,blackKeyHeight+0.75*(metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4)]){");
            }
            if (isWhiteKey(currentPianoKeyIn)) {
                pw.println("linear_extrude(height=(20+20-(" + ((double) currentGeneratorIn / (double) desiredGamut) * 20 + ")+0.25*(blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))){");
            } else {
                pw.println("linear_extrude(height=(20+20-(" + ((double) currentGeneratorIn / (double) desiredGamut) * 20 + ")+0.25*(metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))){");
            }

            pw.println("scale([stalkScaleX,stalkScaleY])");
            pw.println("polygon(points=[");
            pw.println("[Points[0][0],Points[0][1]],[Points[1][0],Points[1][1]],[Points[2][0],Points[2][1]],[Points[3][0],Points[3][1]],[Points[4][0],Points[4][1]],[Points[5][0],Points[5][1]],[Points[6][0],Points[6][1]]");
            pw.println("]);");
            pw.println("}");

            pw.println("if(keytops)");
            pw.println("translate([-0.25*(b+c),(-0.25*(a+d)),30+20-" + ((double) currentGeneratorIn / (double) desiredGamut) * 20 + "])");//
            pw.println("keytop();");

            pw.println("}");
            currentGeneratorIn += periodSteps;
        }
    }

    public void createValuesFile() {
        pwValues.println("$fs=0.1;");
        pwValues.println("//Constants:");
        pwValues.println("edgeRadius=" + edgeRadius + ";");
        pwValues.println("underKeyWidth=" + underKeyWidth + ";");
        pwValues.println("blackKeyHeight=" + blackKeyHeight + ";");
        pwValues.println("genh=" + genh + ";");
        pwValues.println("a=" + a + ";");
        pwValues.println("b=" + b + ";");
        pwValues.println("c=" + c + ";");
        pwValues.println("d=" + d + ";");
        pwValues.println("overhead=" + overhead + ";");
        pwValues.println("desiredGamut=" + desiredGamut + ";");
        pwValues.println("shift=" + shift + ";");
        pwValues.println("holeScaleX=" + holeScaleX + ";");
        pwValues.println("holeScaleY=" + holeScaleY + ";");
        pwValues.println("stalkScaleX=" + stalkScaleX + ";");
        pwValues.println("stalkScaleY=" + stalkScaleY + ";");
        pwValues.println("metalRoundRadius=" + metalRoundRadius + ";");
        pwValues.println("");

        pwValues.println("Points = [");//points are derived from drawing, where shift widens the tips and squishes the middle to make it hexagonal
        pwValues.println("[shift,a,0],//0");
        pwValues.println("[(b-shift),0,0],//1");
        pwValues.println("[(b+shift),0,0],//2");
        pwValues.println("[(b+c-shift),d,0],//3");
        pwValues.println("[(c+shift),(a+d),0],//4");
        pwValues.println("[(c-shift),(a+d),0],//5");

        pwValues.println("[shift,a,10],//6");
        pwValues.println("[(b-shift),0,10],//7");
        pwValues.println("[(b+shift),0,10],//8");
        pwValues.println("[(b+c-shift),d,10],//9");
        pwValues.println("[(c+shift),(a+d),10],//10");
        pwValues.println("[(c-shift),(a+d),10],//11");//each point of polygon (for keytop) in terms of triangle bullshit, offset by shift to make hexagons instead of parallelograms
        pwValues.println("];");

        pwValues.println("Faces = [");
        pwValues.println("[0,1,2,3,4,5],");
        pwValues.println("[1,0,6,7],");
        pwValues.println("[2,1,7,8],");
        pwValues.println("[3,2,8,9],");
        pwValues.println("[4,3,9,10],");
        pwValues.println("[5,4,10,11],");
        pwValues.println("[0,5,11,6],");
        pwValues.println("[11,10,9,8,7,6] ");
        pwValues.println("];");//faces for polygon in counter clockwise points looking from the inside of the key
        pwValues.println("");
    }

    public void createMainBase(int currentGenerator, int keytopsNeeded, int i, int currentPianoKey) {
        double length;//length of main base
        length = genh * (currentGenerator + (keytopsNeeded - 1) * periodSteps) + overhead + stalkScaleY * (a + d);//-genh*currentGenerator%periodSteps;
        pw.println("length=" + length + ";");
        pw.println("difference(){"
                + "\nunion(){");
        pw.println("");
        pw.println("//Main Base:");

        if (!isWhiteKey(currentPianoKey)) {
            pw.println("translate([0,0,blackKeyHeight])");
        }
        //raise up black key for easier combination in togetherPrint

        pw.println("difference(){");
        pw.println("union(){");

        if (isWhiteKey(currentPianoKey)) {//first little chunk to hold the metal round, independent of the rest of the main base
            pw.println("cube([underKeyWidth,metalRoundRadius*2+4,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        } else {
            pw.println("cube([underKeyWidth,metalRoundRadius*2+4,metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");

        }

        pw.println("translate([0,metalRoundRadius*2+4,0])");

        if (isWhiteKey(currentPianoKey)) {//main section, white key is taller
            pw.println("cube([underKeyWidth,length-(metalRoundRadius*2+4),blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        } else {
            pw.println("cube([underKeyWidth,length-(metalRoundRadius*2+4),metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");

        }

        pw.println("}");

        if (isWhiteKey(currentPianoKey)) {//THIS IS STUPID, SHOULD REWRITE 
            pw.println("translate([0,blackKeyHeight,0])\n"
                    + "rotate([45,0,0])\n"
                    + "translate([-25,-50,0])\n"
                    + "cube([50,50,50]);");
        }

        if (isWhiteKey(currentPianoKey)) {
            pw.println("translate([-.1,metalRoundRadius+2,blackKeyHeight+metalRoundRadius+2])");
        } else {
            pw.println("translate([-.1,metalRoundRadius+2,metalRoundRadius+2])");
        }

        pw.println("union(){"
                + "rotate([45,0,0])"
                + "cube([underKeyWidth+0.2,metalRoundRadius+0.5,metalRoundRadius+0.5]);"
                + "rotate([0,90,0])");
        //cylinder transform and rotate for metal round, 0.1 to make the extra 0.2 stick out and not be flush with base
        pw.println("cylinder((underKeyWidth+.2),r=metalRoundRadius+0.5, true);}");//cylinder for metal round

        if (isWhiteKey(currentPianoKey)) {
            pw.println("translate([underKeyWidth/10,1.49,0.125*blackKeyHeight+blackKeyHeight])");
        } else {
            pw.println("translate([underKeyWidth/10,1.49,0.125*(metalRoundRadius*2+8)])");//+8 because of 4 around hole I think I dunno who cares
        }

        pw.println("rotate([90,0,0])");
        pw.println("linear_extrude(height=1.5){");
        pw.println("text(\"" + i + "\",size=(metalRoundRadius*2+4)*0.75);");
        pw.println("}");

        pw.println("//Warp Cuts:");

        if (isWhiteKey(currentPianoKey)) {
            warpCuts(blackKeyHeight + metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 1, true);
            warpCuts(blackKeyHeight + metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 2, true);
            warpCuts(blackKeyHeight + metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 3, true);
        } else {
            warpCuts(metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 1, false);
            warpCuts(metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 2, false);
            warpCuts(metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 3, false);
        }

        pw.println("}");

    }

    public void warpCuts(double warpHeight, double length, int row, boolean whiteKey) {
        pw.println("warpHeight=" + warpHeight + ";");
        double extraForRowOne = 0;
        if (row == 1 && whiteKey) {
            extraForRowOne = blackKeyHeight;
        }

        for (double soFar = 0; soFar < length - (metalRoundRadius * 2 + 4) - 0.25 * warpHeight - extraForRowOne; soFar += warpHeight) {
            if (row == 2) {
                pw.println("translate([0,0.5*warpHeight,0])");
            }
            pw.println("translate([-1,length-" + soFar + "," + row + "*warpHeight*0.5-warpHeight*0.25])");
            pw.println("rotate([0,90,0])");
            pw.println("linear_extrude(height=underKeyWidth+2)");
            pw.println("polygon(points=[[0,0],[0.25*warpHeight,0.25*warpHeight],[0.5*warpHeight,0],[0.25*warpHeight,-0.25*warpHeight]]);");
        }
    }

    public void getUserInputAndDeriveConstants() {

        System.out.println("Number of tuning steps per period:");
        periodSteps = 19;//scan.nextInt();

        System.out.println("Steps to generator:");
        generatorSteps = 8;//scan.nextInt();

        determineMOS();

        System.out.println(checkCoprime(periodSteps, generatorSteps));

        System.out.println("Desired Gamut:");
        desiredGamut = 2 * periodSteps;//scan.nextInt();

        System.out.println("Desired Range:");
        range = periodSteps;//scan.nextInt();

        System.out.println("Starting Piano Key (0-11, A=0,Bb=1,etc.):");
        startingKey = 5;//scan.nextInt();

        System.out.println("Octave Width:");
        octaveWidth = 146;//scan.nextDouble();165

        System.out.println("Metal Round Radius:");
        metalRoundRadius = 2.5;//scan.nextDouble();

        periodWidth = octaveWidth / 12 * periodSteps;

        underKeyWidth = octaveWidth / 12.0 - 0.46875;//0.5 was pretty good, trying 0.375 TOO BIG TRYING 0.4375//after thinning tips of keys I can widen these up a little bit//-0.15 mm for fit, 0.1 was too little 10/21/2018//0.25mm 25th Jan 2019//0.5 then 0.75//0.75 works, previous octaves need difference to 0.75 greater than .75

        System.out.println("Key Travel Distance:");
        blackKeyHeight = 12;//scan.nextDouble();

        System.out.println("White Key Length:");
        whiteKeyLength = 135;//139;//scan.nextDouble();

        genh = whiteKeyLength / desiredGamut;

        System.out.println("Black Key Length");
        blackKeyLength = 88;//scan.nextDouble();

        System.out.println("Please pick an MOS scale to connect by selecting the large step size. Your options are:");

        for (int i = 0; i < mosTracker.size(); i++) {
            System.out.println("\nSmall Step Size:" + mosTracker.get(i).smallSize + " Quantity:" + mosTracker.get(i).smallSteps);
            System.out.println("Large Step Size:" + mosTracker.get(i).largeSize + " Quantity" + mosTracker.get(i).largeSteps);
        }

        System.out.println("Flip layout vertically?:");
        verticalFlip = true;

        stepsForLarge = 3;//scan.nextDouble();
        int chosenMosScaleIndex = 0;

        for (int i = 0; i < mosTracker.size(); i++) {
            if (mosTracker.get(i).largeSize == stepsForLarge) {
                chosenMosScaleIndex = i;
                stepsForSmall = mosTracker.get(chosenMosScaleIndex).smallSize;
                System.out.println("stepsForSmall:" + stepsForSmall);
                break;
            }
        }

        determineGens(stepsForLarge);//find out what generator values get you to large and small step, as well as 1 step in whole tuning

        System.out.println("stepsForSmall:" + stepsForSmall);
        System.out.println("stepsForLarge:" + stepsForLarge);
        System.out.println("periodWidth:" + periodWidth);

        if (!(!verticalFlip || neededAbsoluteValue) || (!verticalFlip && neededAbsoluteValue)) {//is there a way to check whether a step size UP goes up in generators or down? USE WHETHER NEEDED ABSOLUTE VALUE TO DETERMINE IF STEP SIZE IS UP GENERATORS OR DOWN
            
                d = genForSmallStep * genh - 0.5;
                c = stepsForSmall / (periodSteps * 1.0) * periodWidth - 1;
                b = stepsForLarge / (periodSteps * 1.0) * periodWidth - 1;
                a = genForLargeStep * genh - 0.5;
            
        }else{
                a = genForSmallStep * genh - 0.5;
                b = stepsForSmall / (periodSteps * 1.0) * periodWidth - 1;
                c = stepsForLarge / (periodSteps * 1.0) * periodWidth - 1;
                d = genForLargeStep * genh - 0.5;     
        }


        System.out.println("a:" + a + " b:" + b + " c:" + c + " d:" + d);

        overhead = metalRoundRadius * 2 + 4 + (a + d) * stalkScaleY * 0.5;

        shift = (b+c)/8.0;//(((b + c) / 2 - Math.abs(b - c)) / 2) / 2;//(b+c)/8.0;
        
        holeScaleX = Math.min(underKeyWidth / (b + c - shift*2),0.5);
        holeScaleY = 0.5;

        stalkScaleX = ((b + c) * holeScaleX - 1.25) / (b + c);
        stalkScaleY = ((a + d) * holeScaleY - 1.25) / (a + d);
    }

    public void determineMOS() {
        ArrayList<Integer> scaleSteps = new ArrayList<>();
        //ArrayList<HashMap<Integer,Integer>> stepSizes;
        StepSizeTracker stepSizes;
        //ArrayList<ArrayList<HashMap<Integer,Integer>>> stepSizesGrouped = new ArrayList<ArrayList<HashMap<Integer,Integer>>>();

        scaleSteps.add(0);
        scaleSteps.add(periodSteps);

        mosSizes.add(1);
        mosSizes.add(2);
        mosSizes.add(3);

        int numberLess = 1;
        //steps.add(periodSteps);
        for (int currentGenerator = 1; currentGenerator < periodSteps; currentGenerator++) {
            stepSizes = new StepSizeTracker();
            scaleSteps.add((currentGenerator * generatorSteps) % periodSteps);
            Collections.sort(scaleSteps);
            System.out.println("\n\nScale size" + (scaleSteps.size() - 1));
            System.out.println("scaleSteps:" + scaleSteps);
            System.out.println("current Generator:" + currentGenerator + " just added:" + (currentGenerator * generatorSteps) % periodSteps);
            HashMap<Integer, Integer> tempStepHash;

            for (int h = 1; h < scaleSteps.size() - numberLess; h++) {//need to keep track of h in stepSizes to check for how many times it shows up
                //System.out.println("In 1");
                tempStepHash = new HashMap<>();
                for (int i = h; i < scaleSteps.size() + h - numberLess; i++) {//works so far first time through where h=1 and size is 3
                    //System.out.println("h:"+h +"  i:"+i);
                    //System.out.println("In 2");
                    if (i > scaleSteps.size() - 1) {
                        //System.out.println("In 3");   
                        stepSizes.addStep(h, scaleSteps.get(i - (scaleSteps.size() - 1)) + periodSteps - scaleSteps.get(i - h));
                    } else {
                        //System.out.println("In 3");
                        stepSizes.addStep(h, scaleSteps.get(i) - scaleSteps.get(i - h));
                    }
                }
            }

            boolean isMOS = true;
            for (int i = 0; i < stepSizes.stepBags.size(); i++) {
                System.out.println("Checkign isMOS");
                if (stepSizes.stepBags.get(i).size() > 2) {
                    isMOS = false;
                    System.out.println("in false MOS");
                    for (int j = 0; j < stepSizes.stepBags.get(i).size(); j++) {
                        System.out.println(stepSizes.stepBags.get(i).get(j).size + " " + stepSizes.stepBags.get(i).get(j).amount);
                    }
                }
            }

            if (isMOS && isValidMOS(scaleSteps.size() - 1, mosSizes)) {
                System.out.println("ADDING MOS");
                mosSizes.add(scaleSteps.size() - 1);
                if (stepSizes.stepBags.get(0).size() == 2) {
                    mosTracker.add(new mosScale(
                            scaleSteps.size() - 1,
                            stepSizes.stepBags.get(0).get(0).size,
                            stepSizes.stepBags.get(0).get(0).amount,
                            stepSizes.stepBags.get(0).get(1).size,
                            stepSizes.stepBags.get(0).get(1).amount));
                } else {
                    mosTracker.add(new mosScale(
                            scaleSteps.size() - 1,
                            stepSizes.stepBags.get(0).get(0).size,
                            stepSizes.stepBags.get(0).get(0).amount));
                }

            }

        }

        //print out mos results here?
    }

    public boolean isValidMOS(int test, ArrayList<Integer> mosSizes) {
        for (int i = 0; i < mosSizes.size(); i++) {
            for (int j = i; j < mosSizes.size(); j++) {
                if (test == mosSizes.get(j) + mosSizes.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void determineGens(int largeStepSize) {//iterate through stacked generators until you arrive at large step, use to derive small step

        boolean foundLargeGen = false, foundSmallGen = false, foundGenForStep1 = false;
        int currentStepsAboveTonic = generatorSteps;//current number of 
        int generatorCounter = 1;
        int tempSteps;
        while ((foundLargeGen && foundSmallGen && foundGenForStep1) == false /*&& i<periodSteps*/) {

            while (currentStepsAboveTonic > periodSteps - 1) {
                currentStepsAboveTonic -= periodSteps;
            }

            if (currentStepsAboveTonic == stepsForLarge) {
                foundLargeGen = true;
                genForLargeStep = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
                if(generatorCounter > Math.abs(periodSteps - generatorCounter)){
                    neededAbsoluteValue=true;
                }
            }

            if (currentStepsAboveTonic == stepsForSmall) {
                foundSmallGen = true;
                genForSmallStep = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
            }

            if (currentStepsAboveTonic == 1) {
                foundGenForStep1 = true;
                genForStep1 = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
                genForStep1b = Math.abs(genForStep1 - periodSteps);
            }

            if (generatorCounter > periodSteps) {
                System.out.println("I DON'T THINK THAT THOSE TWO NUMBERS ARE COPRIME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;
            }

            currentStepsAboveTonic += generatorSteps;
            generatorCounter++;
        }
        if (largeStepSize == 1) {
            genForLargeStep = Math.abs(genForSmallStep - periodSteps);
            neededAbsoluteValue=false;
        }
        System.out.println("GEN FOR SMALL" + genForSmallStep);
        System.out.println("GEN FOR LARGE" + genForLargeStep);
    }

    public boolean checkCoprime(int per, int gen) {//make sure user input of generator generates entire gamut of periodSteps
        int current = gen;
        boolean areCoprime = true;
        for (int i = 0; i < per - 1; i++) {
            if (current == per) {
                areCoprime = false;
            }
            current += gen;
            if (current > per) {
                current -= per;
            }
        }
        return areCoprime;
    }
}

class mosScale {

    int scaleSize;
    int smallSteps;
    int smallSize;
    int largeSteps;
    int largeSize;

    public mosScale(int scaleSizeIn, int size1In, int steps1In, int size2In, int steps2In) {
        scaleSize = scaleSizeIn;
        if (size1In < size2In) {
            smallSteps = steps1In;
            smallSize = size1In;
            largeSteps = steps2In;
            largeSize = size2In;
        } else {
            smallSteps = steps2In;
            smallSize = size2In;
            largeSteps = steps1In;
            largeSize = size1In;
        }
        System.out.println("small size:" + smallSize);
        System.out.println("large size:" + largeSize);
    }

    public mosScale(int scaleSizeIn, int smallSizeIn, int smallStepsIn) {
        scaleSize = scaleSizeIn;
        smallSteps = smallStepsIn;
        smallSize = smallSizeIn;
        largeSteps = smallStepsIn - scaleSize;
        largeSize = smallSize;
    }

}

class StepSizeTracker {

    ArrayList<ArrayList<StepBag>> stepBags;
    boolean alreadyHere;

    StepSizeTracker() {
        stepBags = new ArrayList<>();
    }

    void addStep(Integer intervalClass, Integer stepSize) {
        if (stepBags.size() < intervalClass) {
            stepBags.add(new ArrayList<StepBag>());
        }
        alreadyHere = false;
        for (int i = 0; i < stepBags.get(intervalClass - 1).size(); i++) {
            if (stepSize == stepBags.get(intervalClass - 1).get(i).size) {
                stepBags.get(intervalClass - 1).get(i).amount++;
                alreadyHere = true;
            }
        }
        if (alreadyHere == false) {
            stepBags.get(intervalClass - 1).add(new StepBag(stepSize, 1));
        }
    }
}

class StepBag {

    int size;
    int amount;

    StepBag(int sizeIn, int amountIn) {
        size = sizeIn;
        amount = amountIn;
    }
}

/*
         d=(Math.cos(0.5*Math.PI-q)*keyTopSide2);//Switched a and d because I'm building the keyboard "left to right"
         b=(Math.sin(0.5*Math.PI-q)*keyTopSide2);
         c=(b);//b used to be = Math.sin(0.5*Math.PI-theta)*keyTopSide1;
         a=(d*genForStep1/genForStep2);//was Math.cos(0.5*Math.PI-theta)*keyTopSide1;
 //z=genh*genForStep1*genForStep2;//height of big triangle made by some number of each adjacent interval, so happens that gFS2 is also number of step1's and vice versa????
         
         //keyTopSide1= (Math.pow((Math.pow(z,2)+Math.pow(genForStep2/periodSteps*periodWidth,2))/Math.pow(genForStep2,2),0.5));//lenght of sides of key BEFORE HEXAGONIZATION, genForStep2 is actually number of steps to the other generator in the tuning
        // keyTopSide2= (Math.pow((Math.pow(z,2)+Math.pow(genForStep1/periodSteps*periodWidth,2))/Math.pow(genForStep1,2),0.5));
         //System.out.println("keyTopSide1&2:  "+keyTopSide1 + " " +keyTopSide2);
         
        // theta=Math.atan(z/((double)genForStep2/(double)periodSteps*periodWidth));//useful angles for finding out a,b,c,d, genForStep2 is stepsToX
        // q    =Math.atan(z/((double)genForStep1/(double)periodSteps*periodWidth));
         
         
         //System.out.println("theta:"+(theta*180/Math.PI));
        // System.out.println("q:"+(q*180/Math.PI));
         
         //System.out.println("z:"+z);
         
         //System.out.println((z/((double)genForStep2/periodSteps*periodWidth)));
        // System.out.println((z/((double)genForStep1/periodSteps*periodWidth)));
        // System.out.println(periodWidth);
         //System.out.println(octaveWidth);


    public void warpWedges(int currentGenerator){
        
        pw.println("translate([-0.01,36,blackKeyHeight+0.01])");
        pw.println("rotate([-90,0,-90])");
        pw.println("linear_extrude(height=underKeyWidth+0.02)");
        pw.println("polygon(points=[");
        pw.println("[0,0],[blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0.5*blackKeyHeight]]);");
        pw.println("");
        
        pw.println("translate([-0.01,42,-0.01])");
        pw.println("rotate([90,0,90])");
        pw.println("linear_extrude(height=underKeyWidth+0.02)");
        pw.println("polygon(points=[");
        pw.println("[0,0],[blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0.5*blackKeyHeight]]);");
        pw.println("");
        
        for(int j = currentGenerator; j<desiredGamut; j+=periodSteps){//j changes generator to enharmonically eqauivalent values by being increased by periodSteps until greater than gamut
            pw.println("translate([-0.01,(genh*"+j+"-(d+a)/2+overhead),blackKeyHeight+0.01])");
            pw.println("rotate([-90,0,-90])");
            pw.println("linear_extrude(height=underKeyWidth+0.02)");
            pw.println("polygon(points=[");
            pw.println("[0,0],[blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0.5*blackKeyHeight]]);");
            pw.println("");

            pw.println("translate([-0.01,(genh*"+j+"+0.5*(a+d)+blackKeyHeight),-0.01])");
            pw.println("rotate([90,0,90])");
            pw.println("linear_extrude(height=underKeyWidth+0.02)");
            pw.println("polygon(points=[");
            pw.println("[0,0],[blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*blackKeyHeight/"+Math.tan(0.25*Math.PI)+",0.5*blackKeyHeight]]);");
            pw.println("");
        }
    }


if(!isWhiteKey(currentPianoKey)){
                        for(double soFar = 0; soFar < length-25-blackKeyHeight; soFar += blackKeyHeight+2){
                            pw.println("translate([-1,"+soFar+"+25,1/3*blackKeyHeight-3])");
                            pw.println("rotate([0,90,0])");
                            pw.println("linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*blackKeyHeight-1,0],[blackKeyHeight-2,0.5*blackKeyHeight-1],[0.5*blackKeyHeight-1,blackKeyHeight-2],[0,0.5*blackKeyHeight-1]]);");
                        }
                    }


int evenOdd = 0;
                    for(double soFar = 0; soFar < length-1.5*blackKeyHeight; soFar += 0.5*blackKeyHeight){

                            
                            pw.println("translate([-1");

                            pw.println(","+soFar+"+blackKeyHeight+1");
                            pw.println(",0.5*blackKeyHeight");
                            if(evenOdd%2==0)pw.println("-");
                            pw.println("-0.01");
                            pw.println("])");
                            if(evenOdd%2==0)pw.println("mirror([1,0,0])");
                            pw.println("rotate([0,");
                            if(evenOdd%2==0)pw.println("-");
                            pw.println("90,0])");
                            pw.println("linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*blackKeyHeight,0],[0.5*blackKeyHeight,0.5*blackKeyHeight");
                            if(evenOdd%2==1 && soFar+0.5*blackKeyHeight>= length-1.5*blackKeyHeight){
                                pw.println("*4");
                            }    
                            pw.println("],[0");
                            if(evenOdd%2==1 && soFar+0.5*blackKeyHeight>= length-1.5*blackKeyHeight){
                                pw.println("-blackKeyHeight");
                            }        
                            pw.println(",0.5*0.5*blackKeyHeight");
                            if(evenOdd%2==1 && soFar+0.5*blackKeyHeight>= length-1.5*blackKeyHeight){
                                pw.println("*4");
                            }        
                            pw.println("]]);");
                            if(evenOdd%2==0 && soFar+0.5*blackKeyHeight>= length-1.5*blackKeyHeight){
                                evenOdd++;
                                soFar+=0.5*blackKeyHeight;
                                pw.println("translate([-1");

                                pw.println(","+soFar+"+blackKeyHeight+1");
                                pw.println(",0.5*blackKeyHeight");
                                if(evenOdd%2==0)pw.println("-");
                                pw.println("-0.01");
                                pw.println("])");
                                if(evenOdd%2==0)pw.println("mirror([1,0,0])");
                                pw.println("rotate([0,");
                                if(evenOdd%2==0)pw.println("-");
                                pw.println("90,0])");
                                pw.println("linear_extrude(height=underKeyWidth+2)");
                                pw.println("polygon(points=[[0.5*blackKeyHeight,0],[0.5*blackKeyHeight,2*blackKeyHeight");
                               
                                pw.println("],[-blackKeyHeight");
                                    
                                pw.println(",blackKeyHeight");
                                     
                                pw.println("]]);");
                            }  

                        evenOdd++;
                    }
                    
                    int evenOdd2 = 0;
                    for(double soFar = 0; soFar < length-1.5*blackKeyHeight; soFar += 0.5*blackKeyHeight){

                            
                            pw.println("translate([-1");

                            pw.println(","+soFar+"+blackKeyHeight+1");
                            //if(evenOdd2%2==0 && soFar+2*blackKeyHeight>length)pw.println("-0.25*blackKeyHeight");
                            pw.println(",1.5*blackKeyHeight");
                            if(evenOdd2%2==1)pw.println("-");
                            pw.println("-0.01");
                           // if(evenOdd2%2==0 && soFar+2*blackKeyHeight>length)pw.println("-0.25*blackKeyHeight");
                            pw.println("])");
                            if(evenOdd2%2==1)pw.println("mirror([1,0,0])");
                            pw.println("rotate([0,");
                            if(evenOdd2%2==1)pw.println("-");
                            pw.println("90,0])");
                            pw.println("#linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*blackKeyHeight,0],[0.5*blackKeyHeight,0.5*blackKeyHeight");
                            
                            pw.println("],[0");
                              
                            pw.println(",0.5*0.5*blackKeyHeight");
                                 
                            pw.println("]]);");

                        evenOdd2++;
                    }


if(length%blackKeyHeight>0.5*blackKeyHeight){
pw.println("translate([0,length-0.125*blackKeyHeight,0.5*blackKeyHeight])");
pw.println("#cube([underKeyWidth,0.125*blackKeyHeight,blackKeyHeight]);");
}

public void createFeet(int currentGenerator, int keytopsNeeded, int currentPianoKey){
        
        pw.println("//Feet:");
        
        double distance;
        if(genh*(currentGenerator+(keytopsNeeded-1)*periodSteps)+overhead<whiteKeyLength){
            distance = genh*(currentGenerator+(keytopsNeeded-1)*periodSteps)+overhead;//distance to front foot
        }
        else{
            distance=whiteKeyLength;
        }
        pw.println("difference(){union(){translate([0,"+(distance-0.5*underKeyWidth)+","+(-blackKeyHeight)+"]){");//combine two feet, subtract slice of B,C,E,or F
        pw.println("cube(["+underKeyWidth+","+(0.5*underKeyWidth)+","+blackKeyHeight+"],false);}");//1 foot
        pw.println("translate([0,"+underKeyWidth+","+(-blackKeyHeight)+"]){");
        pw.println("cube(["+underKeyWidth+","+(0.5*underKeyWidth)+","+blackKeyHeight+"],false);}}");//2 foot
        if(currentPianoKey==2||currentPianoKey==7){//B or E
            pw.println("rotate([0,"+(-Math.atan(0.5*underKeyWidth/blackKeyHeight)*180/Math.PI)+",0])");
            pw.println("translate(["+(-blackKeyHeight*2)+","+0.5+","+(-blackKeyHeight*2)+"])");
            pw.println("cube(["+(2*blackKeyHeight)+","+((genh*(currentGenerator+periodSteps*keytopsNeeded))+1)+","+(2*blackKeyHeight)+"]);");
        }
        if(currentPianoKey==3||currentPianoKey==8){//C or F
            pw.println("translate(["+underKeyWidth+","+0.5+",0])");
            pw.println("rotate([0,"+(Math.atan(0.5*underKeyWidth/blackKeyHeight)*180/Math.PI)+",0])");
            pw.println("translate([0,0,"+(-blackKeyHeight*2)+"])");
            pw.println("cube(["+(2*blackKeyHeight)+","+((genh*(currentGenerator+periodSteps*keytopsNeeded))+1)+","+(2*blackKeyHeight)+"]);");
        }
        pw.println("}");
    }
 */
