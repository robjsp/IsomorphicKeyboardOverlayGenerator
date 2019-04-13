$fs=0.1;
//Constants:
edgeRadius=1.5;
underKeyWidth=13.6;
baseHeight=9.75;
genh=5.791666666666667;
a=27.423996629776394;
b=12.370392724366756;
c=12.370392724366756;
d=38.393595281686956;
overhead=65.81759191146335;
keyTravelDistance=13.0;
desiredGamut=24;
shift=3.092598181091689;
holeScale=0.5;
stalkScale=0.48;

Points = [
[shift,a,0],//0
[(b-shift),0,0],//1
[(b+shift),0,0],//2
[(b+c-shift),d,0],//3
[(c+shift),(a+d),0],//4
[(c-shift),(a+d),0],//5
[shift,a,keyTravelDistance],//6
[(b-shift),0,keyTravelDistance],//7
[(b+shift),0,keyTravelDistance],//8
[(b+c-shift),d,keyTravelDistance],//9
[(c+shift),(a+d),keyTravelDistance],//10
[(c-shift),(a+d),keyTravelDistance],//11
];
Faces = [
[0,1,2,3,4,5],
[1,0,6,7],
[2,1,7,8],
[3,2,8,9],
[4,3,9,10],
[5,4,10,11],
[0,5,11,6],
[11,10,9,8,7,6] 
];

