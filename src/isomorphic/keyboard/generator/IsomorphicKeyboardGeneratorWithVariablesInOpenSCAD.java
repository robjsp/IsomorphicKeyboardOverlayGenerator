package isomorphic.keyboard.generator;
import java.util.*; 
import java.io.*;

public class IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD {
Scanner scan = new Scanner(System.in);
PrintWriter pw,pwKeyTop,pwValues,togetherPrint;
double  octaveWidth, periodWidth, underKeyWidth, keyTravelDistance, baseHeight, whiteKeyLength, blackKeyLength, edgeRadius=1.5,//measurements 
        genh, overhead, keyTopSide1, keyTopSide2, shift,//derived stuff
        theta, q, r, a, b, c, d, z,//bunch of triangle stuff
        generator, holeScale=0.5, stalkScale=0.48;//0.44 was too small once I rotated keybases in printing
int periodSteps, generatorSteps, desiredGamut, startingKey, range,//user decisions
    genForStep1, genForStep2;//derived
boolean isKeytop;


    public static void main(String[] args) {
       IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD IKG = new IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD();
       IKG.getUserInputAndDeriveConstants();
       IKG.generateFiles();
    }   
    
    public void generateFiles(){
        int currentPianoKey, currentGenerator;
        //currentGenerator = 0;//initialize at 0 for now at least, could get user input
        //boolean needFeet;//false by default//SHOULD I SET THIS  I DON'T REMEMBER
        
        File together = new File("/home/john/Desktop/OPENSCAD_DUMP/together.scad");//together is the big collection of keys and keytops that will show if everything worked correctly
        System.out.println(together.getParentFile().mkdirs()+"<-------This is whether the makedirs succeeded or not");
        try{togetherPrint = new PrintWriter(together,"UTF-8");}catch(Exception e){System.out.println(e);}
        togetherPrint.println("include<values.scad>;");
        togetherPrint.println("//$fa=40;");
        
        File values = new File("/home/john/Desktop/OPENSCAD_DUMP/values.scad");
        values.getParentFile().mkdirs();
        try{pwValues = new PrintWriter(values,"UTF-8");}catch(Exception e){System.out.println(e);}
        
        createValuesFile();
        pwValues.close();
        
        for(int i=0; i<range; i++){//iterate keys until range is reached.
            
            //could check for duplicate files, NOT FOR NOWWWW
            currentPianoKey=(i+startingKey)%12;//started on starting key, have moved i times, keys 12 steps apart are same underlying note
            currentGenerator=(i*genForStep1)%periodSteps;//find generator of current step, starts at zero?//TRIED THIS COME BACK HERE IF NOT GOODDawfawefawefawefawea
            //modulo periodSteps because they share the same topside of the key and I want to start at the lowest one so that I include it, because I only move upwards later one, I think
            int keytopsNeeded = (desiredGamut-currentGenerator-1)/periodSteps+1;//-1 then plus one because if desiredGamut-currentGenerator)=periodSteps, I want it to return 1? 
            System.out.println("currentGenerator:"+currentGenerator);
            System.out.println("keytopsNeeded:"+keytopsNeeded);
            
            try{
                File file2 = new File("/home/john/Desktop/OPENSCAD_DUMP/"+i+"_"+currentGenerator+".scad");
                file2.getParentFile().mkdirs();
                pw = new PrintWriter(file2,"UTF-8");

                togetherPrint.println("use<"+i+"_"+currentGenerator+".scad>;");
                
                togetherPrint.println("translate([-"+(i*octaveWidth/12)+",0,0");
                if(needFeet(currentPianoKey)){
                    togetherPrint.println("-baseHeight");
                }
                togetherPrint.println("])");
                togetherPrint.println("rotate([0,90,0])");
                togetherPrint.println(i+"_"+currentGenerator+"(false/*because it's wrong for now*/);");
                
                pw.println("use<keytop.scad>");
                pw.println("include<values.scad>");
                pw.println("rotate([0,90,0])"+i+"_"+currentGenerator+"();");
                pw.println("module " +i +"_"+currentGenerator+"(keytops=false){");
                
                createMainBase(currentGenerator,keytopsNeeded, i, currentPianoKey);
                createKeyStalks(currentGenerator,needFeet(currentPianoKey));
                //if(needFeet(currentPianoKey)){createFeet(currentGenerator, keytopsNeeded, currentPianoKey);}
                
                pw.println("}}");
                
                pw.close();
            }catch(IOException e){
            System.out.println(e);
            }
        }
        createKeytop();//after crazy for loop for bases, make keytops file ONCE OH YEAH ONCE
        togetherPrint.close();
    }
    
    public void createKeytop(){
        try{
        File file = new File("/home/john/Desktop/OPENSCAD_DUMP/keytop.scad");
        file.getParentFile().mkdirs();
        pwKeyTop = new PrintWriter(file,"UTF-8");
        
        
        pwKeyTop.println("include<values.scad>;");
        pwKeyTop.println("keytop();");
        pwKeyTop.println("");

        
        pwKeyTop.println("module keytopShape(){");//
        pwKeyTop.println("polyhedron(Points,Faces);");
        pwKeyTop.println("}");//
        
        pwKeyTop.println("module keytop(){");//make it a module so that together.scad can use it
        pwKeyTop.println("difference(){");
        pwKeyTop.println("keytopShape();");//make key
        

        pwKeyTop.println("translate([(b+c)/4,(d+a)/4,-0.1])");
        pwKeyTop.println("scale(holeScale,holeScale,holeScale)");
        pwKeyTop.println("keytopShape();");
        
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
        }catch(IOException e){
            System.out.println(e);
        }
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
        pw.println("difference(){union(){translate([0,"+(distance-0.5*underKeyWidth)+","+(-keyTravelDistance)+"]){");//combine two feet, subtract slice of B,C,E,or F
        pw.println("cube(["+underKeyWidth+","+(0.5*underKeyWidth)+","+keyTravelDistance+"],false);}");//1 foot
        pw.println("translate([0,"+underKeyWidth+","+(-keyTravelDistance)+"]){");
        pw.println("cube(["+underKeyWidth+","+(0.5*underKeyWidth)+","+keyTravelDistance+"],false);}}");//2 foot
        if(currentPianoKey==2||currentPianoKey==7){//B or E
            pw.println("rotate([0,"+(-Math.atan(0.5*underKeyWidth/keyTravelDistance)*180/Math.PI)+",0])");
            pw.println("translate(["+(-keyTravelDistance*2)+","+0.5+","+(-keyTravelDistance*2)+"])");
            pw.println("cube(["+(2*keyTravelDistance)+","+((genh*(currentGenerator+periodSteps*keytopsNeeded))+1)+","+(2*keyTravelDistance)+"]);");
        }
        if(currentPianoKey==3||currentPianoKey==8){//C or F
            pw.println("translate(["+underKeyWidth+","+0.5+",0])");
            pw.println("rotate([0,"+(Math.atan(0.5*underKeyWidth/keyTravelDistance)*180/Math.PI)+",0])");
            pw.println("translate([0,0,"+(-keyTravelDistance*2)+"])");
            pw.println("cube(["+(2*keyTravelDistance)+","+((genh*(currentGenerator+periodSteps*keytopsNeeded))+1)+","+(2*keyTravelDistance)+"]);");
        }
        pw.println("}");
    }
    
    public boolean needFeet(int currentPianoKey){
    return  currentPianoKey==0||//0 is A, if key is a white key...
            currentPianoKey==2||
            currentPianoKey==3||
            currentPianoKey==5||
            currentPianoKey==7||
            currentPianoKey==8||
            currentPianoKey==10; //then you need feet!
    }
    
    public void createKeyStalks(int currentGenerator, boolean needFeet){//fed in needFeet value because can't access currentPianoKey and I'm bad at programming
        pw.println("//Key Stalks:");
        for(int j = currentGenerator; j<desiredGamut; j+=periodSteps){//j changes generator to enharmonically eqauivalent values by being increased by periodSteps until greater than gamut
                    pw.println("translate([shift/4,(genh*"+j+"-(d+a)/2+overhead),1.5*baseHeight");
                    pw.println("]){");//
                    pw.println("linear_extrude(height=(keyTravelDistance*2+desiredGamut-"+j+"+0.5*baseHeight");
                    if(!needFeet){
                        pw.println("-baseHeight");
                    }
                    
                    pw.println(")){");
                    pw.println("scale((stalkScale+holeScale)/2,stalkScale)");//THIS ACTUALLY MAKES IT NOT QUITE CENTER I THINK BUT I'M NOT WORRIED ABOUT IT for now
                    //Also this scale logic won't work for sideways keys, or probably even some weirder tall keys. not worried about it for now?
                    //IIRC, I averaged stalkscale and holeScale because the smaller of them is affected... less?
                    pw.println("polygon(points=[");
                    pw.println("[Points[0][0],Points[0][1]],[Points[1][0],Points[1][1]],[Points[2][0],Points[2][1]],[Points[3][0],Points[3][1]],[Points[4][0],Points[4][1]],[Points[5][0],Points[5][1]],[Points[6][0],Points[6][1]]");
                    pw.println("]);");
                    pw.println("}");

                    pw.println("if(keytops)");
                    pw.println("translate([0,(-0.5*(a+d)),(keyTravelDistance*1.5+desiredGamut-"+j+"+40)])");//+10 FOR VISIBILITY ONLY
                    pw.println("keytop();");
                    
                    pw.println("}");
                }
    }
    
    
    public void createValuesFile(){
                pwValues.println("$fs=0.1;");
                pwValues.println("//Constants:");
                pwValues.println("edgeRadius="+edgeRadius+";");
                pwValues.println("underKeyWidth="+underKeyWidth+";");
                pwValues.println("baseHeight="+baseHeight+";");
                pwValues.println("genh="+genh+";");
                pwValues.println("a="+a+";");
                pwValues.println("b="+b+";");
                pwValues.println("c="+c+";");
                pwValues.println("d="+d+";");
                pwValues.println("overhead="+overhead+";");
                pwValues.println("keyTravelDistance="+keyTravelDistance+";");
                pwValues.println("desiredGamut="+desiredGamut+";");
                pwValues.println("shift="+shift+";");
                pwValues.println("holeScale="+holeScale+";");
                pwValues.println("stalkScale="+stalkScale+";");
                pwValues.println("");
                
                pwValues.println("Points = [");//points are derived from drawing, where shift widens the tips and squishes the middle to make it hexagonal
                pwValues.println("[shift,a,0],//0");
                pwValues.println("[(b-shift),0,0],//1");
                pwValues.println("[(b+shift),0,0],//2");
                pwValues.println("[(b+c-shift),d,0],//3");
                pwValues.println("[(c+shift),(a+d),0],//4");
                pwValues.println("[(c-shift),(a+d),0],//5");

                pwValues.println("[shift,a,keyTravelDistance],//6");
                pwValues.println("[(b-shift),0,keyTravelDistance],//7");
                pwValues.println("[(b+shift),0,keyTravelDistance],//8");
                pwValues.println("[(b+c-shift),d,keyTravelDistance],//9");
                pwValues.println("[(c+shift),(a+d),keyTravelDistance],//10");
                pwValues.println("[(c-shift),(a+d),keyTravelDistance],//11");//each point of polygon (for keytop) in terms of triangle bullshit, offset by shift to make hexagons instead of parallelograms
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
    
    public void createMainBase(int currentGenerator, int keytopsNeeded, int i, int currentPianoKey){
         double length;//length of main base
                length = genh*(currentGenerator+(keytopsNeeded-1)*periodSteps)+overhead;
                pw.println("length="+length+";");
                
                pw.println("rotate([0,-90,0]){");
                pw.println("//Main Base:");
                //pw.println("difference(){");
                if(length%baseHeight>0.5*baseHeight){
                pw.println("translate([0,length-0.125*baseHeight,0.5*baseHeight])");
                pw.println("cube([underKeyWidth,0.125*baseHeight,baseHeight]);");
                }
                pw.println("difference(){");
                pw.println("union(){");
                
              
                //if(needFeet(currentPianoKey))
                
                   // pw.println("difference(){");
                    pw.println("translate([0,0,baseHeight])");
                    pw.println("cube([underKeyWidth,length,baseHeight],false);");
                
                
                //pw.println("}");
                
                pw.println("cube([underKeyWidth,length,baseHeight],false);");//main base
                pw.println("}");
                if(needFeet(currentPianoKey)){
                    pw.println("translate([0,baseHeight,0])\n" +
                                "rotate([45,0,0])\n" +
                                "translate([-25,-50,0])\n" +
                                "cube([50,50,50]);");
                }
                else{
                    pw.println("translate([0,0,baseHeight])\n" +
                                "rotate([-45,0,0])\n" +
                                "translate([-25,-50,0])\n" +
                                "cube([50,50,50]);");
                }
                pw.println("translate([-.1,0.5*baseHeight,0.5*baseHeight-0.5*(3*(sqrt(2)-1))");
                if(needFeet(currentPianoKey)){
                    pw.println("+baseHeight");
                }
                pw.println("])"
                        + "union(){"
                        + "rotate([45,0,0])"
                        + "translate([])"
                        + "cube([underKeyWidth+0.2,3,3]);"
                        + "rotate([0,90,0])");
                //cylinder transform and rotate for metal round, 0.1 to make the extra 0.2 stick out and not be flush with base
                pw.println("cylinder((underKeyWidth+.2),r=3, true);}");//cylinder for metal round
                if(needFeet(currentPianoKey)){
                    pw.println("");
                }
                pw.println("translate([underKeyWidth/10,1.49,0.125*baseHeight");
                if(needFeet(currentPianoKey)){
                    pw.println("+baseHeight");
                }
                pw.println("])");
                pw.println("rotate([90,0,0])");
                pw.println("linear_extrude(height=1.5){");
                pw.println("text(\""+i+"\",size=baseHeight*0.75);");
                pw.println("}");
                pw.println("");
                

                pw.println("//Warp Cuts:");//This uses some stupid terrible awful logic to get the pattern I wanted. It works.
                    
                    int evenOdd = 0;
                    for(double soFar = 0; soFar < length-1.5*baseHeight; soFar += 0.5*baseHeight){

                            
                            pw.println("translate([-1");

                            pw.println(","+soFar+"+baseHeight+1");
                            pw.println(",0.5*baseHeight");
                            if(evenOdd%2==0)pw.println("-");
                            pw.println("-0.01");
                            pw.println("])");
                            if(evenOdd%2==0)pw.println("mirror([1,0,0])");
                            pw.println("rotate([0,");
                            if(evenOdd%2==0)pw.println("-");
                            pw.println("90,0])");
                            pw.println("linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*baseHeight,0],[0.5*baseHeight,0.5*baseHeight");
                            if(evenOdd%2==1 && soFar+0.5*baseHeight>= length-1.5*baseHeight){
                                pw.println("*4");
                            }    
                            pw.println("],[0");
                            if(evenOdd%2==1 && soFar+0.5*baseHeight>= length-1.5*baseHeight){
                                pw.println("-baseHeight");
                            }        
                            pw.println(",0.5*0.5*baseHeight");
                            if(evenOdd%2==1 && soFar+0.5*baseHeight>= length-1.5*baseHeight){
                                pw.println("*4");
                            }        
                            pw.println("]]);");
                            if(evenOdd%2==0 && soFar+0.5*baseHeight>= length-1.5*baseHeight){
                                evenOdd++;
                                soFar+=0.5*baseHeight;
                                pw.println("translate([-1");

                                pw.println(","+soFar+"+baseHeight+1");
                                pw.println(",0.5*baseHeight");
                                if(evenOdd%2==0)pw.println("-");
                                pw.println("-0.01");
                                pw.println("])");
                                if(evenOdd%2==0)pw.println("mirror([1,0,0])");
                                pw.println("rotate([0,");
                                if(evenOdd%2==0)pw.println("-");
                                pw.println("90,0])");
                                pw.println("linear_extrude(height=underKeyWidth+2)");
                                pw.println("polygon(points=[[0.5*baseHeight,0],[0.5*baseHeight,2*baseHeight");
                               
                                pw.println("],[-baseHeight");
                                    
                                pw.println(",baseHeight");
                                     
                                pw.println("]]);");
                            }  

                        evenOdd++;
                    }
                    
                    int evenOdd2 = 0;
                    for(double soFar = 0; soFar < length-1.5*baseHeight; soFar += 0.5*baseHeight){

                            
                            pw.println("translate([-1");

                            pw.println(","+soFar+"+baseHeight+1");
                            //if(evenOdd2%2==0 && soFar+2*baseHeight>length)pw.println("-0.25*baseHeight");
                            pw.println(",1.5*baseHeight");
                            if(evenOdd2%2==1)pw.println("-");
                            pw.println("-0.01");
                           // if(evenOdd2%2==0 && soFar+2*baseHeight>length)pw.println("-0.25*baseHeight");
                            pw.println("])");
                            if(evenOdd2%2==1)pw.println("mirror([1,0,0])");
                            pw.println("rotate([0,");
                            if(evenOdd2%2==1)pw.println("-");
                            pw.println("90,0])");
                            pw.println("linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*baseHeight,0],[0.5*baseHeight,0.5*baseHeight");
                            
                            pw.println("],[0");
                              
                            pw.println(",0.5*0.5*baseHeight");
                                 
                            pw.println("]]);");

                        evenOdd2++;
                    }
                
                //!!!!!!!!!!!!!!!!!!
                pw.println("}");
                
                System.out.println("");
    }
    

    
    public void getUserInputAndDeriveConstants(){
        
        System.out.println("Number of tuning steps per period:");
         periodSteps = 12;//scan.nextInt();
         
         System.out.println("Steps to generator:");
         generatorSteps = 5;//scan.nextInt();
         
         System.out.println(checkCoprime(periodSteps,generatorSteps));
         
         System.out.println("Desired Gamut:");
         desiredGamut = 24;//scan.nextInt();
         
         System.out.println("Desired Range:");
         range = 12;//scan.nextInt();
         
         System.out.println("Starting Piano Key (0-11, A=0,Bb=1,etc.):");
         startingKey = 0;//scan.nextInt();
         
         System.out.println("Octave Width:");
         octaveWidth = 165;//scan.nextDouble();
         
         periodWidth = octaveWidth/12*periodSteps;
         
         underKeyWidth=octaveWidth/12.0-0.15;//-0.15 mm for fit, 0.1 was too little 10/21/2018
         
         System.out.println("Key Travel Distance:");
         keyTravelDistance = 13;//scan.nextDouble();
         
         baseHeight = 13*0.75;//13 was keyTravel Distance but it's really more about fitting the 3/16" metal round that being a percentage of the keyTravelDistance
         
         System.out.println("White Key Length:");
         whiteKeyLength = 139;//scan.nextDouble();
         
         genh=whiteKeyLength/desiredGamut;
         
         System.out.println("Black Key Length");
         blackKeyLength = 88;//scan.nextDouble();
         
         determineGensForAdjacentIntervals();//find out what generator values get you to 1 step in the tuning
         System.out.println("genForStep1&2:  "+genForStep1 +" " +genForStep2);
         
         z=genh*genForStep1*genForStep2;//height of big triangle made by some number of each adjacent interval, so happens that gFS2 is also number of step1's and vice versa????
         
         keyTopSide1= (Math.pow((Math.pow(z,2)+Math.pow(genForStep2/periodSteps*periodWidth,2))/Math.pow(genForStep2,2),0.5));//lenght of sides of key BEFORE HEXAGONIZATION, genForStep2 is actually number of steps to the other generator in the tuning
         keyTopSide2= (Math.pow((Math.pow(z,2)+Math.pow(genForStep1/periodSteps*periodWidth,2))/Math.pow(genForStep1,2),0.5));
         System.out.println("keyTopSide1&2:  "+keyTopSide1 + " " +keyTopSide2);
         
         theta=Math.atan(z/((double)genForStep2/(double)periodSteps*periodWidth));//useful angles for finding out a,b,c,d, genForStep2 is stepsToX
         q    =Math.atan(z/((double)genForStep1/(double)periodSteps*periodWidth));
         
         
         System.out.println("theta:"+(theta*180/Math.PI));
         System.out.println("q:"+(q*180/Math.PI));
         
         System.out.println("z:"+z);
         
         System.out.println((z/((double)genForStep2/periodSteps*periodWidth)));
         System.out.println((z/((double)genForStep1/periodSteps*periodWidth)));
         System.out.println(periodWidth);
         System.out.println(octaveWidth);
         
         d=(Math.cos(0.5*Math.PI-q)*keyTopSide2);//Switched a and d because I'm building the keyboard "left to right"
         b=(Math.sin(0.5*Math.PI-q)*keyTopSide2)*0.95;
         c=(b);//b used to be = Math.sin(0.5*Math.PI-theta)*keyTopSide1;
         a=(d*genForStep1/genForStep2);//was Math.cos(0.5*Math.PI-theta)*keyTopSide1;
         //needed to only use one side (keyTopSide2) in derivation either because i made an assumption and can't use both derivations, or maybe a mistake with one side
         
         System.out.println("a:"+a+" b:"+b+" c:"+c+" d:"+d);//Maybe assumming something is bigger than something else like... dunno
         
         System.out.println(genForStep1);
         System.out.println(genForStep2);
         System.out.println( (double)genForStep1/(double)genForStep2);
         System.out.println((double)genForStep2/(double)genForStep1);
         System.out.println((double)a/(double)d);
         System.out.println((double)d/(double)a);//Should the ratio between a and d be the same as the ratio between gFS1 and gFS2???:??????
         //should b and c be equal?
         
         overhead=a+d;
         
         shift=(((b+c)/2-Math.abs(b-c))/2)/2;
    }
    
    public void determineGensForAdjacentIntervals(){//iterate through stacked generators until you arrive at 1 step in the tuning, which are going to be our adjacent intervals in order to keep keys tall and skinny
        
        boolean foundGen = false;
        int currentStepsAboveTonic = generatorSteps;//current number of 
        int generatorCounter = 1;
        while(foundGen == false /*&& i<periodSteps*/){
            
            while(currentStepsAboveTonic>periodSteps-1){currentStepsAboveTonic-=periodSteps;}
            
            System.out.println("determineGensForAdjacentIntervals, generatorCounter:"+generatorCounter+" currentStepsAboveTonic:"+currentStepsAboveTonic +"" +"");
            
            
            if(currentStepsAboveTonic==1){foundGen=true; genForStep1=generatorCounter; genForStep2=periodSteps-generatorCounter; System.out.println("FOUND GEN !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}
            if(generatorCounter>periodSteps){System.out.println("I DON'T THINK THAT THOSE TWO NUMBERS ARE COPRIME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}
            
            
            
            currentStepsAboveTonic+=generatorSteps;
            generatorCounter++;
        }
    }
    
    public boolean checkCoprime(int per, int gen){//make sure user input of generator generates entire gamut of periodSteps
        int current=gen;
        boolean areCoprime = true;
        for(int i = 0; i<per-1; i++){
        if(current==per){areCoprime=false;}
        current += gen;
        if(current>per){current -= per;}
        }
        return areCoprime;
    }  
}


/*
    public void warpWedges(int currentGenerator){
        
        pw.println("translate([-0.01,36,baseHeight+0.01])");
        pw.println("rotate([-90,0,-90])");
        pw.println("linear_extrude(height=underKeyWidth+0.02)");
        pw.println("polygon(points=[");
        pw.println("[0,0],[baseHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*baseHeight/"+Math.tan(0.25*Math.PI)+",0.5*baseHeight]]);");
        pw.println("");
        
        pw.println("translate([-0.01,42,-0.01])");
        pw.println("rotate([90,0,90])");
        pw.println("linear_extrude(height=underKeyWidth+0.02)");
        pw.println("polygon(points=[");
        pw.println("[0,0],[baseHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*baseHeight/"+Math.tan(0.25*Math.PI)+",0.5*baseHeight]]);");
        pw.println("");
        
        for(int j = currentGenerator; j<desiredGamut; j+=periodSteps){//j changes generator to enharmonically eqauivalent values by being increased by periodSteps until greater than gamut
            pw.println("translate([-0.01,(genh*"+j+"-(d+a)/2+overhead),baseHeight+0.01])");
            pw.println("rotate([-90,0,-90])");
            pw.println("linear_extrude(height=underKeyWidth+0.02)");
            pw.println("polygon(points=[");
            pw.println("[0,0],[baseHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*baseHeight/"+Math.tan(0.25*Math.PI)+",0.5*baseHeight]]);");
            pw.println("");

            pw.println("translate([-0.01,(genh*"+j+"+0.5*(a+d)+baseHeight),-0.01])");
            pw.println("rotate([90,0,90])");
            pw.println("linear_extrude(height=underKeyWidth+0.02)");
            pw.println("polygon(points=[");
            pw.println("[0,0],[baseHeight/"+Math.tan(0.25*Math.PI)+",0],[0.5*baseHeight/"+Math.tan(0.25*Math.PI)+",0.5*baseHeight]]);");
            pw.println("");
        }
    }


if(!needFeet(currentPianoKey)){
                        for(double soFar = 0; soFar < length-25-baseHeight; soFar += baseHeight+2){
                            pw.println("translate([-1,"+soFar+"+25,1/3*baseHeight-3])");
                            pw.println("rotate([0,90,0])");
                            pw.println("linear_extrude(height=underKeyWidth+2)");
                            pw.println("polygon(points=[[0.5*baseHeight-1,0],[baseHeight-2,0.5*baseHeight-1],[0.5*baseHeight-1,baseHeight-2],[0,0.5*baseHeight-1]]);");
                        }
                    }
*/